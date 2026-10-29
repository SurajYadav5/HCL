import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TradingService } from '../../services/trading.service';
import { Order, OrderSide, OrderStatus, OrderType } from '../../models/models';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  loading: boolean = true;
  currentPage: number = 0;
  totalPages: number = 1;
  totalOrders: number = 0;
  pageSize: number = 20;

  // Filters
  selectedSideFilter: 'ALL' | OrderSide = 'ALL';
  selectedStatusFilter: 'ALL' | OrderStatus = 'ALL';
  selectedTypeFilter: 'ALL' | OrderType = 'ALL';
  searchSymbol: string = '';

  // Order Detail Modal
  selectedOrder: Order | null = null;
  showDetailModal: boolean = false;

  // Cancelling state
  isCancelling: boolean = false;
  toastMessage: string = '';
  toastType: 'success' | 'error' = 'success';

  constructor(private tradingService: TradingService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(page: number = 0): void {
    this.loading = true;
    this.currentPage = page;

    this.tradingService.getOrderHistory(page, this.pageSize).subscribe({
      next: res => {
        this.loading = false;
        if (res.success && res.data) {
          this.orders = res.data.content || [];
          this.totalPages = res.data.totalPages || 1;
          this.totalOrders = res.data.totalElements || this.orders.length;
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  get filteredOrders(): Order[] {
    return this.orders.filter(o => {
      const matchSide = this.selectedSideFilter === 'ALL' || o.side === this.selectedSideFilter;
      const matchStatus = this.selectedStatusFilter === 'ALL' || o.status === this.selectedStatusFilter;
      const matchType = this.selectedTypeFilter === 'ALL' || o.type === this.selectedTypeFilter;
      const matchSym = !this.searchSymbol.trim() || 
        o.symbol.toLowerCase().includes(this.searchSymbol.trim().toLowerCase()) ||
        (o.companyName && o.companyName.toLowerCase().includes(this.searchSymbol.trim().toLowerCase()));
      return matchSide && matchStatus && matchType && matchSym;
    });
  }

  get pagesArray(): number[] {
    const pages: number[] = [];
    for (let i = 0; i < this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  cancelPendingOrder(order: Order, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    if (order.status !== 'PENDING') return;

    this.isCancelling = true;
    this.tradingService.cancelOrder(order.id).subscribe({
      next: res => {
        this.isCancelling = false;
        if (res.success) {
          order.status = 'CANCELLED';
          if (this.selectedOrder && this.selectedOrder.id === order.id) {
            this.selectedOrder.status = 'CANCELLED';
          }
          this.showToast(`Order #${order.id} (${order.symbol}) has been successfully cancelled.`, 'success');
        } else {
          this.showToast(res.message || 'Failed to cancel order.', 'error');
        }
      },
      error: err => {
        this.isCancelling = false;
        this.showToast(err.error?.message || 'Server rejected cancellation request.', 'error');
      }
    });
  }

  exportToCSV(): void {
    if (this.orders.length === 0) {
      this.showToast('No orders available to export.', 'error');
      return;
    }

    const headers = ['Order ID', 'Date & Time', 'Symbol', 'Company', 'Side', 'Type', 'Shares', 'Executed Price', 'Limit Price', 'Total Value', 'Status'];
    const rows = this.filteredOrders.map(o => [
      o.id,
      `"${new Date(o.createdAt).toLocaleString()}"`,
      `"${o.symbol}"`,
      `"${o.companyName || ''}"`,
      o.side,
      o.type,
      o.quantity,
      o.executedPrice ? o.executedPrice.toFixed(2) : '',
      o.limitPrice ? o.limitPrice.toFixed(2) : '',
      o.totalValue ? o.totalValue.toFixed(2) : '0.00',
      o.status
    ]);

    const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `trade_audit_ledger_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.showToast(`Exported ${this.filteredOrders.length} orders to CSV.`, 'success');
  }

  viewDetails(order: Order): void {
    this.selectedOrder = order;
    this.showDetailModal = true;
  }

  closeDetail(): void {
    this.showDetailModal = false;
  }

  showToast(msg: string, type: 'success' | 'error'): void {
    this.toastMessage = msg;
    this.toastType = type;
    setTimeout(() => { this.toastMessage = ''; }, 3500);
  }

  get totalVolumeTraded(): number {
    return this.orders.reduce((sum, o) => sum + (o.totalValue || 0), 0);
  }

  get totalBuyCount(): number {
    return this.orders.filter(o => o.side === 'BUY').length;
  }

  get totalSellCount(): number {
    return this.orders.filter(o => o.side === 'SELL').length;
  }

  get totalPendingCount(): number {
    return this.orders.filter(o => o.status === 'PENDING').length;
  }
}
