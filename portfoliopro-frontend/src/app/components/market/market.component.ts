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
  Math = Math;

  // Search & Filter
  filterSearch: string = '';
  selectedSector: string = 'ALL';
  sectors: string[] = [
    'ALL',
    'Technology',
    'Financials',
    'Healthcare',
    'Consumer Cyclical',
    'Consumer Defensive',
    'Energy',
    'Industrials',
    'Communication Services',
    'Utilities',
    'Real Estate'
  ];

  sortBy: 'symbol' | 'price' | 'change' | 'volume' = 'symbol';
  sortOrder: 'asc' | 'desc' = 'asc';

  // Global search autocomplete
  searchQuery: string = '';
  searchResults: StockSearchResult[] = [];
  isSearching: boolean = false;

  // Timeframe-specific metrics
  timeframeChange: number = 0;
  timeframeChangePercent: number = 0;
  timeframeHigh: number = 0;
  timeframeLow: number = 0;

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
    this.loadSectors();

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

    // Search debounce for global symbol lookup
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

  loadSectors(): void {
    this.marketDataService.getSectors().subscribe({
      next: res => {
        if (res.success && res.data && res.data.length > 0) {
          this.sectors = ['ALL', ...res.data];
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
      { symbol: 'AAPL', companyName: 'Apple Inc.', sector: 'Technology', price: 189.84, open: 188.5, high: 190.2, low: 187.9, previousClose: 187.68, change: 2.16, changePercent: 1.15, volume: 54200000, marketCap: 2950000000000, peRatio: 31.4, week52High: 199.62, week52Low: 164.08, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'MSFT', companyName: 'Microsoft Corp.', sector: 'Technology', price: 428.74, open: 425.0, high: 430.5, low: 424.1, previousClose: 425.22, change: 3.52, changePercent: 0.83, volume: 22100000, marketCap: 3180000000000, peRatio: 36.8, week52High: 468.35, week52Low: 309.45, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'NVDA', companyName: 'NVIDIA Corp.', sector: 'Technology', price: 128.50, open: 125.1, high: 130.0, low: 124.8, previousClose: 125.60, change: 2.90, changePercent: 2.31, volume: 78900000, marketCap: 3150000000000, peRatio: 68.2, week52High: 140.76, week52Low: 40.50, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'GOOGL', companyName: 'Alphabet Inc.', sector: 'Communication Services', price: 177.30, open: 178.0, high: 179.2, low: 176.5, previousClose: 178.40, change: -1.10, changePercent: -0.62, volume: 18400000, marketCap: 2210000000000, peRatio: 25.1, week52High: 191.75, week52Low: 120.21, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'AMZN', companyName: 'Amazon.com Inc.', sector: 'Consumer Cyclical', price: 186.40, open: 184.2, high: 187.5, low: 183.8, previousClose: 183.92, change: 2.48, changePercent: 1.35, volume: 34100000, marketCap: 1940000000000, peRatio: 42.5, week52High: 201.20, week52Low: 118.35, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'TSLA', companyName: 'Tesla Inc.', sector: 'Consumer Cyclical', price: 254.30, open: 260.0, high: 262.5, low: 252.1, previousClose: 259.80, change: -5.50, changePercent: -2.12, volume: 68300000, marketCap: 810000000000, peRatio: 64.9, week52High: 271.00, week52Low: 138.80, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'META', companyName: 'Meta Platforms Inc.', sector: 'Communication Services', price: 504.60, open: 498.0, high: 506.8, low: 496.2, previousClose: 499.50, change: 5.10, changePercent: 1.02, volume: 14200000, marketCap: 1280000000000, peRatio: 26.4, week52High: 542.81, week52Low: 279.40, exchange: 'NASDAQ', lastUpdated: new Date().toISOString() },
      { symbol: 'JPM', companyName: 'JPMorgan Chase & Co.', sector: 'Financials', price: 215.40, open: 214.0, high: 216.5, low: 213.2, previousClose: 214.20, change: 1.20, changePercent: 0.56, volume: 8900000, marketCap: 615000000000, peRatio: 12.1, week52High: 225.48, week52Low: 140.33, exchange: 'NYSE', lastUpdated: new Date().toISOString() }
    ];
    if (!this.selectedQuote && this.quotes.length > 0) {
      this.selectStock(this.quotes[0]);
    }
  }

  // Filtered & Sorted quotes
  get filteredQuotes(): StockQuote[] {
    let list = this.quotes;

    // Filter by sector
    if (this.selectedSector && this.selectedSector !== 'ALL') {
      list = list.filter(q => (q.sector || '').toLowerCase() === this.selectedSector.toLowerCase());
    }

    // Filter by search query
    if (this.filterSearch.trim()) {
      const q = this.filterSearch.trim().toLowerCase();
      list = list.filter(s =>
        s.symbol.toLowerCase().includes(q) ||
        s.companyName.toLowerCase().includes(q) ||
        (s.sector && s.sector.toLowerCase().includes(q))
      );
    }

    // Sort
    return list.slice().sort((a, b) => {
      let diff = 0;
      if (this.sortBy === 'symbol') diff = a.symbol.localeCompare(b.symbol);
      else if (this.sortBy === 'price') diff = a.price - b.price;
      else if (this.sortBy === 'change') diff = a.changePercent - b.changePercent;
      else if (this.sortBy === 'volume') diff = (a.volume || 0) - (b.volume || 0);
      return this.sortOrder === 'desc' ? -diff : diff;
    });
  }

  getSectorCount(sec: string): number {
    if (sec === 'ALL') return this.quotes.length;
    return this.quotes.filter(q => (q.sector || '').toLowerCase() === sec.toLowerCase()).length;
  }

  setSector(sec: string): void {
    this.selectedSector = sec;
    const currentList = this.filteredQuotes;
    if (currentList.length > 0 && (!this.selectedQuote || !currentList.find(q => q.symbol === this.selectedQuote?.symbol))) {
      this.selectStock(currentList[0]);
    }
  }

  setSort(by: 'symbol' | 'price' | 'change' | 'volume'): void {
    if (this.sortBy === by) {
      this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc';
    } else {
      this.sortBy = by;
      this.sortOrder = by === 'symbol' ? 'asc' : 'desc';
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
        if (res.success && res.data && res.data.length > 0 && this.activeTimeframe === '1M') {
          this.historyData = res.data;
          this.calculateTimeframeMetrics();
        } else {
          this.generateTimeframeHistory(this.activeTimeframe);
        }
      },
      error: () => {
        this.loadingChart = false;
        this.generateTimeframeHistory(this.activeTimeframe);
      }
    });
  }

  setTimeframe(tf: '1D' | '1W' | '1M' | '1Y'): void {
    this.activeTimeframe = tf;
    this.generateTimeframeHistory(tf);
  }

  generateTimeframeHistory(tf: '1D' | '1W' | '1M' | '1Y'): void {
    if (!this.selectedQuote) return;
    const basePrice = this.selectedQuote.price;
    const points: OHLCVDataPoint[] = [];

    if (tf === '1D') {
      // 16 intraday points (approx every 25 mins from market open 9:30 AM to 4:00 PM)
      const pointCount = 16;
      const openPrice = this.selectedQuote.open || basePrice * 0.995;
      let curr = openPrice;
      const now = new Date();
      now.setHours(9, 30, 0, 0);

      for (let i = 0; i < pointCount; i++) {
        const timeStr = new Date(now.getTime() + i * 25 * 60000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        const pctStep = (basePrice - openPrice) / pointCount;
        const wiggle = (Math.random() - 0.48) * (basePrice * 0.004);
        curr = i === pointCount - 1 ? basePrice : Math.max(1, curr + pctStep + wiggle);
        const high = curr + Math.random() * (basePrice * 0.002);
        const low = curr - Math.random() * (basePrice * 0.002);

        points.push({
          date: timeStr,
          open: Number(curr.toFixed(2)),
          high: Number(high.toFixed(2)),
          low: Number(low.toFixed(2)),
          close: Number(curr.toFixed(2)),
          volume: Math.floor(200000 + Math.random() * 800000)
        });
      }
    } else if (tf === '1W') {
      // 7 daily points
      const count = 7;
      let curr = basePrice * (1 - (this.selectedQuote.changePercent * 0.01 * 2));
      for (let i = count - 1; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        const wiggle = (Math.random() - 0.47) * (basePrice * 0.012);
        curr = i === 0 ? basePrice : Math.max(1, curr + wiggle);
        const high = curr + Math.random() * (basePrice * 0.008);
        const low = curr - Math.random() * (basePrice * 0.008);

        points.push({
          date: d.toLocaleDateString([], { month: 'short', day: 'numeric' }),
          open: Number(curr.toFixed(2)),
          high: Number(high.toFixed(2)),
          low: Number(low.toFixed(2)),
          close: Number(curr.toFixed(2)),
          volume: Math.floor(10000000 + Math.random() * 20000000)
        });
      }
    } else if (tf === '1M') {
      // 30 daily points
      const count = 30;
      let curr = basePrice * 0.94;
      for (let i = count - 1; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i);
        const step = (basePrice - (basePrice * 0.94)) / count;
        const wiggle = (Math.random() - 0.48) * (basePrice * 0.02);
        curr = i === 0 ? basePrice : Math.max(1, curr + step + wiggle);
        const high = curr + Math.random() * (basePrice * 0.015);
        const low = curr - Math.random() * (basePrice * 0.015);

        points.push({
          date: d.toLocaleDateString([], { month: 'short', day: 'numeric' }),
          open: Number(curr.toFixed(2)),
          high: Number(high.toFixed(2)),
          low: Number(low.toFixed(2)),
          close: Number(curr.toFixed(2)),
          volume: Math.floor(15000000 + Math.random() * 30000000)
        });
      }
    } else {
      // 1Y: 52 weekly points
      const count = 52;
      const low52 = this.selectedQuote.week52Low || basePrice * 0.75;
      const high52 = this.selectedQuote.week52High || basePrice * 1.25;
      let curr = low52 * 1.05;

      for (let i = count - 1; i >= 0; i--) {
        const d = new Date();
        d.setDate(d.getDate() - i * 7);
        const progress = 1 - (i / count);
        const target = low52 + (basePrice - low52) * progress;
        const wiggle = (Math.random() - 0.48) * (basePrice * 0.04);
        curr = i === 0 ? basePrice : Math.min(high52, Math.max(low52, target + wiggle));
        const high = Math.min(high52, curr + Math.random() * (basePrice * 0.025));
        const low = Math.max(low52, curr - Math.random() * (basePrice * 0.025));

        points.push({
          date: d.toLocaleDateString([], { month: 'short', year: '2-digit' }),
          open: Number(curr.toFixed(2)),
          high: Number(high.toFixed(2)),
          low: Number(low.toFixed(2)),
          close: Number(curr.toFixed(2)),
          volume: Math.floor(40000000 + Math.random() * 60000000)
        });
      }
    }

    this.historyData = points;
    this.calculateTimeframeMetrics();
  }

  calculateTimeframeMetrics(): void {
    if (!this.historyData || this.historyData.length === 0) return;
    const first = this.historyData[0].close;
    const last = this.historyData[this.historyData.length - 1].close;
    this.timeframeChange = last - first;
    this.timeframeChangePercent = (this.timeframeChange / (first || 1)) * 100;
    this.timeframeHigh = Math.max(...this.historyData.map(p => p.high));
    this.timeframeLow = Math.min(...this.historyData.map(p => p.low));
  }

  get52WeekRangePct(): number {
    if (!this.selectedQuote) return 50;
    const low = this.selectedQuote.week52Low || this.selectedQuote.low * 0.85;
    const high = this.selectedQuote.week52High || this.selectedQuote.high * 1.15;
    const price = this.selectedQuote.price;
    if (high === low) return 50;
    const pct = ((price - low) / (high - low)) * 100;
    return Math.min(100, Math.max(0, pct));
  }

  formatLargeNumber(num?: number): string {
    if (!num) return 'N/A';
    if (num >= 1e12) return (num / 1e12).toFixed(2) + 'T';
    if (num >= 1e9) return (num / 1e9).toFixed(2) + 'B';
    if (num >= 1e6) return (num / 1e6).toFixed(2) + 'M';
    return num.toLocaleString();
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

    const width = 640;
    const height = 230;
    const step = width / (points.length - 1);

    return points.map((p, i) => {
      const x = i * step;
      const y = height - ((p.close - min) / range) * (height - 40) - 20;
      return `${i === 0 ? 'M' : 'L'} ${x.toFixed(1)} ${y.toFixed(1)}`;
    }).join(' ');
  }

  getAreaChartPath(): string {
    const linePath = this.getChartPath();
    if (!linePath) return '';
    return `${linePath} L 640 230 L 0 230 Z`;
  }
}
