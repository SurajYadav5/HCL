import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Order, PagedResponse, PlaceOrderRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TradingService {
  private readonly API = '/api/orders';

  constructor(private http: HttpClient) {}

  placeOrder(request: PlaceOrderRequest): Observable<ApiResponse<Order>> {
    return this.http.post<ApiResponse<Order>>(this.API, request);
  }

  getOrderHistory(page: number = 0, size: number = 20): Observable<ApiResponse<PagedResponse<Order>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<ApiResponse<PagedResponse<Order>>>(this.API, { params });
  }

  getOrderById(orderId: number): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.API}/${orderId}`);
  }
}
