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
  selectedSectorFilter: string = 'ALL';
  sortBy: 'symbol' | 'value' | 'pnl' | 'quantity' | 'return' = 'value';
  sortOrder: 'asc' | 'desc' = 'desc';

  toastMessage: string = '';
  toastType: 'success' | 'error' = 'success';

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

  get availableSectors(): string[] {
    const set = new Set<string>();
    (this.summary.holdings || []).forEach(h => {
      if (h.sector) set.add(h.sector);
    });
    return ['ALL', ...Array.from(set)];
  }

  get sectorAllocations(): { sector: string; value: number; pct: number }[] {
    const map = new Map<string, number>();
    const holdings = this.summary.holdings || [];
    holdings.forEach(h => {
      const sec = h.sector || 'Equities';
      map.set(sec, (map.get(sec) || 0) + (h.currentValue || 0));
    });

    const totalVal = this.summary.totalCurrentValue || 1;
    return Array.from(map.entries())
      .map(([sector, value]) => ({
        sector,
        value,
        pct: (value / totalVal) * 100
      }))
      .sort((a, b) => b.value - a.value);
  }

  get filteredHoldings(): PortfolioItem[] {
    let list = this.summary.holdings || [];

    // Filter by sector
    if (this.selectedSectorFilter !== 'ALL') {
      list = list.filter(h => (h.sector || '').toLowerCase() === this.selectedSectorFilter.toLowerCase());
    }

    // Filter by text
    if (this.filterQuery.trim()) {
      const q = this.filterQuery.toLowerCase();
      list = list.filter(h => 
        h.symbol.toLowerCase().includes(q) || 
        h.companyName.toLowerCase().includes(q) ||
        (h.sector && h.sector.toLowerCase().includes(q))
      );
    }

    // Sort
    return list.slice().sort((a, b) => {
      let diff = 0;
      if (this.sortBy === 'symbol') diff = a.symbol.localeCompare(b.symbol);
      else if (this.sortBy === 'value') diff = a.currentValue - b.currentValue;
      else if (this.sortBy === 'pnl') diff = a.unrealizedPnl - b.unrealizedPnl;
      else if (this.sortBy === 'quantity') diff = a.quantity - b.quantity;
      else if (this.sortBy === 'return') diff = a.unrealizedPnlPct - b.unrealizedPnlPct;
      return this.sortOrder === 'desc' ? -diff : diff;
    });
  }

  setSort(by: 'symbol' | 'value' | 'pnl' | 'quantity' | 'return'): void {
    if (this.sortBy === by) {
      this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc';
    } else {
      this.sortBy = by;
      this.sortOrder = by === 'symbol' ? 'asc' : 'desc';
    }
  }

  exportPortfolioCSV(): void {
    const list = this.filteredHoldings;
    if (list.length === 0) {
      this.showToast('No portfolio holdings available to export.', 'error');
      return;
    }

    const headers = [
      'Symbol',
      'Company',
      'Sector',
      'Shares Owned',
      'Average Cost ($)',
      'Current Price ($)',
      'Total Invested ($)',
      'Current Value ($)',
      'Unrealized P&L ($)',
      'Unrealized Return (%)',
      'Portfolio Weight (%)'
    ];

    const rows = list.map(h => [
      `"${h.symbol}"`,
      `"${h.companyName}"`,
      `"${h.sector || 'Equities'}"`,
      h.quantity,
      h.averageCost.toFixed(2),
      h.currentPrice.toFixed(2),
      h.totalInvested.toFixed(2),
      h.currentValue.toFixed(2),
      h.unrealizedPnl.toFixed(2),
      h.unrealizedPnlPct.toFixed(2),
      h.allocationPct ? h.allocationPct.toFixed(2) : ''
    ]);

    const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `portfolio_holdings_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.showToast(`Exported ${list.length} holdings to CSV.`, 'success');
  }

  tradeStock(symbol: string, side: 'BUY' | 'SELL'): void {
    this.router.navigate(['/trade'], { queryParams: { symbol, side } });
  }

  showToast(msg: string, type: 'success' | 'error'): void {
    this.toastMessage = msg;
    this.toastType = type;
    setTimeout(() => { this.toastMessage = ''; }, 3500);
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
    if (count < 4) return { rating: 'Concentrated', desc: 'High concentration in few tickers', color: 'gold' };
    if (count < 10) return { rating: 'Moderate', desc: 'Healthy balance across core positions', color: 'cyan' };
    return { rating: 'Well Diversified', desc: 'Strong multi-sector institutional diversification (16 positions)', color: 'green' };
  }
}
