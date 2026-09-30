import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { TradingService } from '../../services/trading.service';
import { MarketDataService } from '../../services/market-data.service';
import { PortfolioService } from '../../services/portfolio.service';
import { Order, OrderSide, OrderType, PlaceOrderRequest, PortfolioItem, PortfolioSummary, StockQuote } from '../../models/models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-trade',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './trade.component.html',
  styleUrls: ['./trade.component.css']
})
export class TradeComponent implements OnInit, OnDestroy {
  // Order Form State
  symbol: string = 'AAPL';
  side: OrderSide = 'BUY';
  type: OrderType = 'MARKET';
  quantity: number = 10;
  limitPrice: number | null = null;

  // Market & Account State
  currentQuote: StockQuote | null = null;
  portfolio: PortfolioSummary | null = null;
  currentHolding: PortfolioItem | null = null;

  // Execution State
  isSubmitting: boolean = false;
  executionError: string = '';
  lastExecutedOrder: Order | null = null;
  showReceiptModal: boolean = false;

  popularTickers = ['AAPL', 'NVDA', 'MSFT', 'AMZN', 'GOOGL', 'TSLA', 'META', 'JPM'];

  private subs = new Subscription();

  constructor(
    private tradingService: TradingService,
    private marketDataService: MarketDataService,
    private portfolioService: PortfolioService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPortfolio();

    // Read query params if any
    this.subs.add(
      this.route.queryParams.subscribe(params => {
        if (params['symbol']) {
          this.symbol = params['symbol'].toUpperCase();
        }
        if (params['side'] && (params['side'] === 'BUY' || params['side'] === 'SELL')) {
          this.side = params['side'];
        }
        this.fetchQuote(this.symbol);
      })
    );

    // Auto refresh current quote every 10 seconds
    this.subs.add(
      interval(10000).subscribe(() => {
        if (this.symbol) {
          this.fetchQuote(this.symbol, false);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadPortfolio(): void {
    this.portfolioService.getPortfolio().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.portfolio = res.data;
          this.updateHoldingForSymbol();
        }
      }
    });
  }

  fetchQuote(sym: string, resetLimit: boolean = true): void {
    this.marketDataService.getQuote(sym).subscribe({
      next: res => {
        if (res.success && res.data) {
          this.currentQuote = res.data;
          if (resetLimit && this.type === 'LIMIT' && !this.limitPrice) {
            this.limitPrice = res.data.price;
          }
          this.updateHoldingForSymbol();
        }
      },
      error: () => {
        // Fallback default quote
        this.currentQuote = {
          symbol: sym,
          companyName: sym + ' Inc.',
          price: 189.84,
          open: 188.5,
          high: 190.2,
          low: 187.9,
          previousClose: 187.68,
          change: 2.16,
          changePercent: 1.15,
          volume: 54000000,
          lastUpdated: new Date().toISOString()
        };
        this.updateHoldingForSymbol();
      }
    });
  }

  updateHoldingForSymbol(): void {
    if (!this.portfolio || !this.portfolio.holdings) {
      this.currentHolding = null;
      return;
    }
    const found = this.portfolio.holdings.find(h => h.symbol.toUpperCase() === this.symbol.toUpperCase());
    this.currentHolding = found || null;
  }

  selectSymbol(sym: string): void {
    this.symbol = sym.toUpperCase();
    this.fetchQuote(this.symbol);
  }

  setSide(s: OrderSide): void {
    this.side = s;
  }

  setType(t: OrderType): void {
    this.type = t;
    if (t === 'LIMIT' && !this.limitPrice && this.currentQuote) {
      this.limitPrice = this.currentQuote.price;
    }
  }

  setPercentQty(pct: number): void {
    if (this.side === 'BUY') {
      const price = this.effectivePrice;
      const cash = this.portfolio?.cashBalance || 100000;
      if (price > 0) {
        const affordable = Math.floor((cash * pct) / price);
        this.quantity = Math.max(1, affordable);
      }
    } else {
      const shares = this.currentHolding?.quantity || 0;
      this.quantity = Math.max(1, Math.floor(shares * pct));
    }
  }

  get effectivePrice(): number {
    if (this.type === 'LIMIT' && this.limitPrice && this.limitPrice > 0) {
      return this.limitPrice;
    }
    return this.currentQuote?.price || 0;
  }

  get estimatedTotal(): number {
    return this.quantity * this.effectivePrice;
  }

  get canSubmit(): boolean {
    if (!this.symbol || this.quantity <= 0) return false;
    if (this.type === 'LIMIT' && (!this.limitPrice || this.limitPrice <= 0)) return false;
    if (this.side === 'BUY') {
      const cash = this.portfolio?.cashBalance || 0;
      return this.estimatedTotal <= cash;
    } else {
      const shares = this.currentHolding?.quantity || 0;
      return this.quantity <= shares;
    }
  }

  submitOrder(): void {
    this.executionError = '';

    if (this.side === 'BUY' && this.estimatedTotal > (this.portfolio?.cashBalance || 0)) {
      this.executionError = `Insufficient cash funds. Available: $${this.portfolio?.cashBalance.toFixed(2)}, Needed: $${this.estimatedTotal.toFixed(2)}`;
      return;
    }

    if (this.side === 'SELL' && (!this.currentHolding || this.quantity > this.currentHolding.quantity)) {
      this.executionError = `You own ${this.currentHolding?.quantity || 0} shares of ${this.symbol}. Cannot sell ${this.quantity} shares.`;
      return;
    }

    this.isSubmitting = true;

    const request: PlaceOrderRequest = {
      symbol: this.symbol.toUpperCase(),
      side: this.side,
      type: this.type,
      quantity: this.quantity,
      limitPrice: this.type === 'LIMIT' ? (this.limitPrice || undefined) : undefined
    };

    this.tradingService.placeOrder(request).subscribe({
      next: res => {
        this.isSubmitting = false;
        if (res.success && res.data) {
          this.lastExecutedOrder = res.data;
          this.showReceiptModal = true;
          this.loadPortfolio();
        } else {
          this.executionError = res.message || 'Order failed to execute.';
        }
      },
      error: err => {
        this.isSubmitting = false;
        this.executionError = err.error?.message || 'Failed to place order. Server rejected transaction.';
      }
    });
  }

  closeReceipt(): void {
    this.showReceiptModal = false;
  }
}
