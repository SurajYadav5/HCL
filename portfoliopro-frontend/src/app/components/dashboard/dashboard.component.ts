import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PortfolioService } from '../../services/portfolio.service';
import { MarketDataService } from '../../services/market-data.service';
import { TradingService } from '../../services/trading.service';
import { WatchlistService } from '../../services/watchlist.service';
import { PortfolioSummary, StockQuote, Order, PlaceOrderRequest } from '../../models/models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  summary: PortfolioSummary = {
    totalInvested: 0,
    totalCurrentValue: 0,
    totalUnrealizedPnl: 0,
    totalUnrealizedPnlPct: 0,
    totalRealizedPnl: 0,
    cashBalance: 100000,
    totalPortfolioValue: 100000,
    totalPositions: 0,
    totalTrades: 0,
    holdings: []
  };

  marketQuotes: StockQuote[] = [];
  topGainers: StockQuote[] = [];
  topLosers: StockQuote[] = [];
  recentOrders: Order[] = [];
  loading: boolean = true;

  // Quick Trade Widget state
  quickSymbol: string = 'AAPL';
  quickSide: 'BUY' | 'SELL' = 'BUY';
  quickQty: number = 10;
  quickPrice: number = 189.84;
  quickOrderSuccess: string = '';
  quickOrderError: string = '';
  quickSubmitting: boolean = false;

  private subs = new Subscription();

  constructor(
    private portfolioService: PortfolioService,
    private marketDataService: MarketDataService,
    private tradingService: TradingService,
    private watchlistService: WatchlistService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAllData();

    // Auto refresh market quotes every 15 seconds
    this.subs.add(
      interval(15000).subscribe(() => {
        this.refreshMarketQuotes();
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadAllData(): void {
    this.loading = true;

    // Load portfolio
    this.portfolioService.getPortfolio().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.summary = res.data;
        }
      },
      error: () => {}
    });

    // Load market overview
    this.marketDataService.getMarketOverview().subscribe({
      next: res => {
        this.loading = false;
        if (res.success && res.data) {
          this.marketQuotes = res.data;
          this.computeGainersLosers(res.data);
          this.updateQuickPrice();
        }
      },
      error: () => {
        this.loading = false;
        this.setSampleMarketData();
      }
    });

    // Load recent orders
    this.tradingService.getOrderHistory(0, 5).subscribe({
      next: res => {
        if (res.success && res.data && res.data.content) {
          this.recentOrders = res.data.content;
        }
      },
      error: () => {}
    });
  }

  refreshMarketQuotes(): void {
    this.marketDataService.getMarketOverview().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.marketQuotes = res.data;
          this.computeGainersLosers(res.data);
        }
      }
    });
  }

  computeGainersLosers(quotes: StockQuote[]): void {
    const sorted = [...quotes].sort((a, b) => b.changePercent - a.changePercent);
    this.topGainers = sorted.slice(0, 3);
    this.topLosers = [...sorted].reverse().slice(0, 3);
  }

  setSampleMarketData(): void {
    this.marketQuotes = [
      { symbol: 'AAPL', companyName: 'Apple Inc.', price: 189.84, open: 188.5, high: 190.2, low: 187.9, previousClose: 187.68, change: 2.16, changePercent: 1.15, volume: 54200000, lastUpdated: new Date().toISOString() },
      { symbol: 'MSFT', companyName: 'Microsoft Corp.', price: 428.74, open: 425.0, high: 430.5, low: 424.1, previousClose: 425.22, change: 3.52, changePercent: 0.83, volume: 22100000, lastUpdated: new Date().toISOString() },
      { symbol: 'NVDA', companyName: 'NVIDIA Corp.', price: 128.50, open: 125.1, high: 130.0, low: 124.8, previousClose: 125.60, change: 2.90, changePercent: 2.31, volume: 78900000, lastUpdated: new Date().toISOString() },
      { symbol: 'GOOGL', companyName: 'Alphabet Inc.', price: 177.30, open: 178.0, high: 179.2, low: 176.5, previousClose: 178.40, change: -1.10, changePercent: -0.62, volume: 18400000, lastUpdated: new Date().toISOString() },
      { symbol: 'AMZN', companyName: 'Amazon.com Inc.', price: 186.40, open: 184.2, high: 187.5, low: 183.8, previousClose: 183.92, change: 2.48, changePercent: 1.35, volume: 34100000, lastUpdated: new Date().toISOString() },
      { symbol: 'TSLA', companyName: 'Tesla Inc.', price: 254.30, open: 260.0, high: 262.5, low: 252.1, previousClose: 259.80, change: -5.50, changePercent: -2.12, volume: 68300000, lastUpdated: new Date().toISOString() }
    ];
    this.computeGainersLosers(this.marketQuotes);
    this.updateQuickPrice();
  }

  updateQuickPrice(): void {
    const q = this.marketQuotes.find(m => m.symbol.toUpperCase() === this.quickSymbol.toUpperCase());
    if (q) {
      this.quickPrice = q.price;
    }
  }

  onQuickSymbolChange(): void {
    this.updateQuickPrice();
  }

  executeQuickTrade(): void {
    this.quickOrderSuccess = '';
    this.quickOrderError = '';

    if (!this.quickQty || this.quickQty <= 0) {
      this.quickOrderError = 'Please specify a valid quantity.';
      return;
    }

    const estCost = this.quickQty * this.quickPrice;
    if (this.quickSide === 'BUY' && estCost > this.summary.cashBalance) {
      this.quickOrderError = `Insufficient cash balance. Required: $${estCost.toFixed(2)}, Available: $${this.summary.cashBalance.toFixed(2)}`;
      return;
    }

    this.quickSubmitting = true;
    const req: PlaceOrderRequest = {
      symbol: this.quickSymbol.toUpperCase(),
      side: this.quickSide,
      type: 'MARKET',
      quantity: this.quickQty
    };

    this.tradingService.placeOrder(req).subscribe({
      next: res => {
        this.quickSubmitting = false;
        if (res.success) {
          this.quickOrderSuccess = `Executed ${this.quickSide} ${this.quickQty} shares of ${this.quickSymbol} at $${res.data.executedPrice?.toFixed(2) || this.quickPrice.toFixed(2)}`;
          // Refresh summary and orders
          this.loadAllData();
        } else {
          this.quickOrderError = res.message || 'Trade execution failed.';
        }
      },
      error: err => {
        this.quickSubmitting = false;
        this.quickOrderError = err.error?.message || 'Trade failed to execute.';
      }
    });
  }

  tradeStock(symbol: string, side: 'BUY' | 'SELL' = 'BUY'): void {
    this.router.navigate(['/trade'], { queryParams: { symbol, side } });
  }

  viewStockDetails(symbol: string): void {
    this.router.navigate(['/market'], { queryParams: { symbol } });
  }
}
