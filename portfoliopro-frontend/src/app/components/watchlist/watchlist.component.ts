import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { WatchlistService } from '../../services/watchlist.service';
import { MarketDataService } from '../../services/market-data.service';
import { WatchlistItem, StockSearchResult } from '../../models/models';
import { Subject, Subscription, debounceTime, distinctUntilChanged, switchMap, of, interval } from 'rxjs';

@Component({
  selector: 'app-watchlist',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './watchlist.component.html',
  styleUrls: ['./watchlist.component.css']
})
export class WatchlistComponent implements OnInit, OnDestroy {
  watchlist: WatchlistItem[] = [];
  loading: boolean = true;

  // Add stock search
  addQuery: string = '';
  searchResults: StockSearchResult[] = [];
  isSearching: boolean = false;
  toastMessage: string = '';

  private searchSubject = new Subject<string>();
  private subs = new Subscription();

  constructor(
    private watchlistService: WatchlistService,
    private marketDataService: MarketDataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadWatchlist();

    // Auto refresh quotes every 15s
    this.subs.add(
      interval(15000).subscribe(() => {
        this.loadWatchlist(false);
      })
    );

    // Search debounce for adding
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

  loadWatchlist(showSpinner: boolean = true): void {
    if (showSpinner) this.loading = true;
    this.watchlistService.getWatchlist().subscribe({
      next: res => {
        this.loading = false;
        if (res.success && res.data) {
          this.watchlist = res.data;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  onAddInput(): void {
    this.searchSubject.next(this.addQuery);
  }

  addStock(symbol: string, companyName?: string): void {
    this.addQuery = '';
    this.searchResults = [];
    this.watchlistService.addToWatchlist(symbol, companyName).subscribe({
      next: () => {
        this.showToast(`${symbol.toUpperCase()} added to watchlist!`);
        this.loadWatchlist(false);
      }
    });
  }

  removeStock(symbol: string, event: Event): void {
    event.stopPropagation();
    this.watchlistService.removeFromWatchlist(symbol).subscribe({
      next: () => {
        this.watchlist = this.watchlist.filter(i => i.symbol.toUpperCase() !== symbol.toUpperCase());
        this.showToast(`${symbol.toUpperCase()} removed from watchlist.`);
      }
    });
  }

  tradeStock(symbol: string, side: 'BUY' | 'SELL', event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/trade'], { queryParams: { symbol, side } });
  }

  viewDetails(symbol: string): void {
    this.router.navigate(['/market'], { queryParams: { symbol } });
  }

  showToast(msg: string): void {
    this.toastMessage = msg;
    setTimeout(() => { this.toastMessage = ''; }, 3000);
  }

  get averageChange(): number {
    if (this.watchlist.length === 0) return 0;
    const total = this.watchlist.reduce((sum, item) => sum + (item.changePercent || 0), 0);
    return total / this.watchlist.length;
  }
}
