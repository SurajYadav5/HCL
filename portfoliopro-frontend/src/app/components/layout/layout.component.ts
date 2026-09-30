import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { MarketDataService } from '../../services/market-data.service';
import { PortfolioService } from '../../services/portfolio.service';
import { StockQuote, StockSearchResult, User } from '../../models/models';
import { Subject, Subscription, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.css']
})
export class LayoutComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  cashBalance: number = 100000;
  searchQuery: string = '';
  searchResults: StockSearchResult[] = [];
  isSearching: boolean = false;
  showSearchModal: boolean = false;
  sidebarCollapsed: boolean = false;

  tickerQuotes: StockQuote[] = [];
  private searchSubject = new Subject<string>();
  private subs = new Subscription();

  constructor(
    public authService: AuthService,
    private marketDataService: MarketDataService,
    private portfolioService: PortfolioService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.authService.currentUser$.subscribe(user => {
        this.currentUser = user;
        if (user) {
          this.cashBalance = user.cashBalance || 100000;
          this.loadPortfolioBalance();
        }
      })
    );

    this.loadMarketOverview();

    // Debounced search
    this.subs.add(
      this.searchSubject.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(q => {
          if (!q || q.trim().length === 0) {
            return of({ success: true, message: '', data: [] });
          }
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

  loadPortfolioBalance(): void {
    this.portfolioService.getPortfolio().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.cashBalance = res.data.cashBalance;
        }
      }
    });
  }

  loadMarketOverview(): void {
    this.marketDataService.getMarketOverview().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.tickerQuotes = res.data;
        }
      },
      error: () => {
        // Fallback default ticker items
        this.tickerQuotes = [
          { symbol: 'AAPL', companyName: 'Apple Inc.', price: 189.84, open: 188.5, high: 190.2, low: 187.9, previousClose: 187.68, change: 2.16, changePercent: 1.15, volume: 54200000, lastUpdated: new Date().toISOString() },
          { symbol: 'MSFT', companyName: 'Microsoft Corp.', price: 428.74, open: 425.0, high: 430.5, low: 424.1, previousClose: 425.22, change: 3.52, changePercent: 0.83, volume: 22100000, lastUpdated: new Date().toISOString() },
          { symbol: 'NVDA', companyName: 'NVIDIA Corp.', price: 128.50, open: 125.1, high: 130.0, low: 124.8, previousClose: 125.60, change: 2.90, changePercent: 2.31, volume: 78900000, lastUpdated: new Date().toISOString() },
          { symbol: 'GOOGL', companyName: 'Alphabet Inc.', price: 177.30, open: 178.0, high: 179.2, low: 176.5, previousClose: 178.40, change: -1.10, changePercent: -0.62, volume: 18400000, lastUpdated: new Date().toISOString() },
          { symbol: 'AMZN', companyName: 'Amazon.com Inc.', price: 186.40, open: 184.2, high: 187.5, low: 183.8, previousClose: 183.92, change: 2.48, changePercent: 1.35, volume: 34100000, lastUpdated: new Date().toISOString() },
          { symbol: 'TSLA', companyName: 'Tesla Inc.', price: 254.30, open: 260.0, high: 262.5, low: 252.1, previousClose: 259.80, change: -5.50, changePercent: -2.12, volume: 68300000, lastUpdated: new Date().toISOString() }
        ];
      }
    });
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchQuery);
  }

  selectStock(symbol: string): void {
    this.showSearchModal = false;
    this.searchQuery = '';
    this.searchResults = [];
    this.router.navigate(['/market'], { queryParams: { symbol } });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout(): void {
    this.authService.logout();
  }
}
