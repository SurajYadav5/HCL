import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TradingService } from '../../services/trading.service';
import { Order, OrderSide, OrderStatus } from '../../models/models';

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

  selectedSideFilter: 'ALL' | OrderSide = 'ALL';
  selectedStatusFilter: 'ALL' | OrderStatus = 'ALL';
  searchSymbol: string = '';

  selectedOrder: Order | null = null;
  showDetailModal: boolean = false;

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
      const matchSym = !this.searchSymbol || o.symbol.toLowerCase().includes(this.searchSymbol.toLowerCase());
      return matchSide && matchStatus && matchSym;
    });
  }

  viewDetails(order: Order): void {
    this.selectedOrder = order;
    this.showDetailModal = true;
  }

  closeDetail(): void {
    this.showDetailModal = false;
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
}
