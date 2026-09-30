import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PortfolioService } from '../../services/portfolio.service';
import { PortfolioItem, PortfolioSummary } from '../../models/models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit, OnDestroy {
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

  loading: boolean = true;
  filterQuery: string = '';
  sortBy: 'symbol' | 'value' | 'pnl' = 'value';
  sortOrder: 'asc' | 'desc' = 'desc';

  private subs = new Subscription();

  constructor(
    private portfolioService: PortfolioService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadPortfolio();

    // Auto-refresh every 20s
    this.subs.add(
      interval(20000).subscribe(() => {
        this.loadPortfolio(false);
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  loadPortfolio(showLoading: boolean = true): void {
    if (showLoading) this.loading = true;
    this.portfolioService.getPortfolio().subscribe({
      next: res => {
        this.loading = false;
        if (res.success && res.data) {
          this.summary = res.data;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get filteredHoldings(): PortfolioItem[] {
    let list = this.summary.holdings || [];
    if (this.filterQuery.trim()) {
      const q = this.filterQuery.toLowerCase();
      list = list.filter(h => 
        h.symbol.toLowerCase().includes(q) || 
        h.companyName.toLowerCase().includes(q)
      );
    }

    return list.sort((a, b) => {
      let diff = 0;
      if (this.sortBy === 'symbol') diff = a.symbol.localeCompare(b.symbol);
      else if (this.sortBy === 'value') diff = a.currentValue - b.currentValue;
      else if (this.sortBy === 'pnl') diff = a.unrealizedPnl - b.unrealizedPnl;
      return this.sortOrder === 'desc' ? -diff : diff;
    });
  }

  setSort(by: 'symbol' | 'value' | 'pnl'): void {
    if (this.sortBy === by) {
      this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc';
    } else {
      this.sortBy = by;
      this.sortOrder = 'desc';
    }
  }

  tradeStock(symbol: string, side: 'BUY' | 'SELL'): void {
    this.router.navigate(['/trade'], { queryParams: { symbol, side } });
  }

  // Risk metrics calculations
  get cashAllocationPct(): number {
    if (!this.summary.totalPortfolioValue || this.summary.totalPortfolioValue === 0) return 100;
    return (this.summary.cashBalance / this.summary.totalPortfolioValue) * 100;
  }

  get equitiesAllocationPct(): number {
    if (!this.summary.totalPortfolioValue || this.summary.totalPortfolioValue === 0) return 0;
    return (this.summary.totalCurrentValue / this.summary.totalPortfolioValue) * 100;
  }

  get diversificationRating(): { rating: string; desc: string; color: string } {
    const count = this.summary.holdings.length;
    if (count === 0) return { rating: 'All Cash', desc: '100% Cash allocation', color: 'blue' };
    if (count < 3) return { rating: 'Concentrated', desc: 'High concentration in few tickers', color: 'gold' };
    if (count < 6) return { rating: 'Moderate', desc: 'Healthy balance across core positions', color: 'cyan' };
    return { rating: 'Well Diversified', desc: 'Strong multi-sector diversification', color: 'green' };
  }
}
