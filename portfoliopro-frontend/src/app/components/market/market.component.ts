import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MarketDataService } from '../../services/market-data.service';
import { WatchlistService } from '../../services/watchlist.service';
import { OHLCVDataPoint, StockQuote, StockSearchResult } from '../../models/models';
import { Subject, Subscription, debounceTime, distinctUntilChanged, switchMap, of, interval } from 'rxjs';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './market.component.html',
  styleUrls: ['./market.component.css']
})
export class MarketComponent implements OnInit, OnDestroy {
  quotes: StockQuote[] = [];
  selectedQuote: StockQuote | null = null;
  historyData: OHLCVDataPoint[] = [];
  activeTimeframe: '1D' | '1W' | '1M' | '1Y' = '1M';
  loading: boolean = true;
  loadingChart: boolean = false;
  watchlistSymbols: Set<string> = new Set();

  searchQuery: string = '';
  searchResults: StockSearchResult[] = [];
  isSearching: boolean = false;

  toastMessage: string = '';
  toastType: 'success' | 'error' = 'success';

  private searchSubject = new Subject<string>();
  private subs = new Subscription();

  constructor(
    private marketDataService: MarketDataService,
    private watchlistService: WatchlistService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadWatchlist();
    this.loadMarketData();

    // Check for symbol in query params
    this.subs.add(
      this.route.queryParams.subscribe(params => {
        if (params['symbol']) {
          this.selectSymbol(params['symbol']);
        }
      })
    );

    // Live refresh every 20s
    this.subs.add(
      interval(20000).subscribe(() => {
        this.refreshQuotes();
      })
    );

    // Search debounce
    this.subs.add(
      this.searchSubject.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(q => {
          if (!q || q.trim().length === 0) return of({ success: true, message: '', data: [] });
          this.isSearching = true;
          return this.marketDataService.searchStocks(q.trim());
        })
      ).subscribe({
        next: res => {
          this.searchResults = res.data || [];
          this.isSearching = false;
        },
        error: () => {
          this.searchResults = [];
          this.isSearching = false;
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadWatchlist(): void {
    this.watchlistService.getWatchlist().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.watchlistSymbols = new Set(res.data.map(i => i.symbol.toUpperCase()));
        }
      }
    });
  }

  loadMarketData(): void {
    this.loading = true;
    this.marketDataService.getMarketOverview().subscribe({
      next: res => {
        this.loading = false;
        if (res.success && res.data && res.data.length > 0) {
          this.quotes = res.data;
          if (!this.selectedQuote) {
            this.selectStock(this.quotes[0]);
          }
        } else {
          this.setFallbackData();
        }
      },
      error: () => {
        this.loading = false;
        this.setFallbackData();
      }
    });
  }

  refreshQuotes(): void {
    this.marketDataService.getMarketOverview().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.quotes = res.data;
          if (this.selectedQuote) {
            const updated = res.data.find(q => q.symbol === this.selectedQuote?.symbol);
            if (updated) this.selectedQuote = updated;
          }
        }
      }
    });
  }

  setFallbackData(): void {
    this.quotes = [
      { symbol: 'AAPL', companyName: 'Apple Inc.', price: 189.84, open: 188.5, high: 190.2, low: 187.9, previousClose: 187.68, change: 2.16, changePercent: 1.15, volume: 54200000, marketCap: 2950000000000, peRatio: 31.4, week52High: 199.62, week52Low: 164.08, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'MSFT', companyName: 'Microsoft Corp.', price: 428.74, open: 425.0, high: 430.5, low: 424.1, previousClose: 425.22, change: 3.52, changePercent: 0.83, volume: 22100000, marketCap: 3180000000000, peRatio: 36.8, week52High: 468.35, week52Low: 309.45, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'NVDA', companyName: 'NVIDIA Corp.', price: 128.50, open: 125.1, high: 130.0, low: 124.8, previousClose: 125.60, change: 2.90, changePercent: 2.31, volume: 78900000, marketCap: 3150000000000, peRatio: 68.2, week52High: 140.76, week52Low: 40.50, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'GOOGL', companyName: 'Alphabet Inc.', price: 177.30, open: 178.0, high: 179.2, low: 176.5, previousClose: 178.40, change: -1.10, changePercent: -0.62, volume: 18400000, marketCap: 2210000000000, peRatio: 25.1, week52High: 191.75, week52Low: 120.21, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'AMZN', companyName: 'Amazon.com Inc.', price: 186.40, open: 184.2, high: 187.5, low: 183.8, previousClose: 183.92, change: 2.48, changePercent: 1.35, volume: 34100000, marketCap: 1940000000000, peRatio: 42.5, week52High: 201.20, week52Low: 118.35, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'TSLA', companyName: 'Tesla Inc.', price: 254.30, open: 260.0, high: 262.5, low: 252.1, previousClose: 259.80, change: -5.50, changePercent: -2.12, volume: 68300000, marketCap: 810000000000, peRatio: 64.9, week52High: 271.00, week52Low: 138.80, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'META', companyName: 'Meta Platforms Inc.', price: 504.60, open: 498.0, high: 506.8, low: 496.2, previousClose: 499.50, change: 5.10, changePercent: 1.02, volume: 14200000, marketCap: 1280000000000, peRatio: 26.4, week52High: 542.81, week52Low: 279.40, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'JPM', companyName: 'JPMorgan Chase & Co.', price: 215.40, open: 214.0, high: 216.5, low: 213.2, previousClose: 214.20, change: 1.20, changePercent: 0.56, volume: 8900000, marketCap: 615000000000, peRatio: 12.1, week52High: 225.48, week52Low: 140.33, exchange: 'NYSE', lastUpdated: new Date().toISOString() }
    ];
    if (!this.selectedQuote && this.quotes.length > 0) {
      this.selectStock(this.quotes[0]);
    }
  }

  selectSymbol(symbol: string): void {
    const found = this.quotes.find(q => q.symbol.toUpperCase() === symbol.toUpperCase());
    if (found) {
      this.selectStock(found);
    } else {
      this.marketDataService.getQuote(symbol).subscribe({
        next: res => {
          if (res.success && res.data) {
            this.selectStock(res.data);
          }
        }
      });
    }
  }

  selectStock(quote: StockQuote): void {
    this.selectedQuote = quote;
    this.loadChartHistory(quote.symbol);
  }

  loadChartHistory(symbol: string): void {
    this.loadingChart = true;
    this.marketDataService.getHistory(symbol).subscribe({
      next: res => {
        this.loadingChart = false;
        if (res.success && res.data && res.data.length > 0) {
          this.historyData = res.data;
        } else {
          this.generateMockHistory(this.selectedQuote?.price || 150);
        }
      },
      error: () => {
        this.loadingChart = false;
        this.generateMockHistory(this.selectedQuote?.price || 150);
      }
    });
  }

  generateMockHistory(basePrice: number): void {
    const points: OHLCVDataPoint[] = [];
    const count = 30;
    let curr = basePrice * 0.92;

    for (let i = count; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const change = (Math.random() - 0.48) * (basePrice * 0.03);
      curr = Math.max(10, curr + change);
      const high = curr + Math.random() * (basePrice * 0.015);
      const low = curr - Math.random() * (basePrice * 0.015);

      points.push({
        date: d.toISOString().split('T')[0],
        open: Number((curr - change * 0.5).toFixed(2)),
        high: Number(high.toFixed(2)),
        low: Number(low.toFixed(2)),
        close: Number(curr.toFixed(2)),
        volume: Math.floor(10000000 + Math.random() * 30000000)
      });
    }
    this.historyData = points;
  }

  setTimeframe(tf: '1D' | '1W' | '1M' | '1Y'): void {
    this.activeTimeframe = tf;
    if (this.selectedQuote) {
      this.generateMockHistory(this.selectedQuote.price);
    }
  }

  toggleWatchlist(quote: StockQuote): void {
    const sym = quote.symbol.toUpperCase();
    if (this.watchlistSymbols.has(sym)) {
      this.watchlistService.removeFromWatchlist(sym).subscribe({
        next: () => {
          this.watchlistSymbols.delete(sym);
          this.showToast(`${sym} removed from watchlist`, 'success');
        }
      });
    } else {
      this.watchlistService.addToWatchlist(sym, quote.companyName).subscribe({
        next: () => {
          this.watchlistSymbols.add(sym);
          this.showToast(`${sym} added to watchlist`, 'success');
        }
      });
    }
  }

  isInWatchlist(symbol: string): boolean {
    return this.watchlistSymbols.has(symbol.toUpperCase());
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
  }

  selectSearchResult(result: StockSearchResult): void {
    this.searchQuery = '';
    this.searchResults = [];
    this.selectSymbol(result.symbol);
  }

  openTrade(side: 'BUY' | 'SELL'): void {
    if (this.selectedQuote) {
      this.router.navigate(['/trade'], { 
        queryParams: { symbol: this.selectedQuote.symbol, side } 
      });
    }
  }

  showToast(msg: string, type: 'success' | 'error'): void {
    this.toastMessage = msg;
    this.toastType = type;
    setTimeout(() => { this.toastMessage = ''; }, 3000);
  }

  // SVG Chart path calculation
  getChartPath(): string {
    if (!this.historyData || this.historyData.length === 0) return '';
    const points = this.historyData;
    const min = Math.min(...points.map(p => p.low));
    const max = Math.max(...points.map(p => p.high));
    const range = max - min || 1;

    const width = 600;
    const height = 220;
    const step = width / (points.length - 1);

    return points.map((p, i) => {
      const x = i * step;
      const y = height - ((p.close - min) / range) * (height - 30) - 15;
      return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
    }).join(' ');
  }

  getAreaChartPath(): string {
    const linePath = this.getChartPath();
    if (!linePath) return '';
    return `${linePath} L 600 220 L 0 220 Z`;
  }
}
