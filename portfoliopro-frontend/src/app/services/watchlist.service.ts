import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, WatchlistItem } from '../models/models';

@Injectable({ providedIn: 'root' })
export class WatchlistService {
  private readonly API = '/api/watchlist';

  constructor(private http: HttpClient) {}

  getWatchlist(): Observable<ApiResponse<WatchlistItem[]>> {
    return this.http.get<ApiResponse<WatchlistItem[]>>(this.API);
  }

  addToWatchlist(symbol: string, companyName?: string): Observable<ApiResponse<void>> {
    let params = new HttpParams();
    if (companyName) {
      params = params.set('companyName', companyName);
    }
    return this.http.post<ApiResponse<void>>(`${this.API}/${symbol.toUpperCase()}`, null, { params });
  }

  removeFromWatchlist(symbol: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${symbol.toUpperCase()}`);
  }
}
