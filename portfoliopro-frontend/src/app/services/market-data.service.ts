import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { ApiResponse, OHLCVDataPoint, StockQuote, StockSearchResult } from '../models/models';

@Injectable({ providedIn: 'root' })
export class MarketDataService {
  private readonly API = '/api/market';

  constructor(private http: HttpClient) {}

  getQuote(symbol: string): Observable<ApiResponse<StockQuote>> {
    return this.http.get<ApiResponse<StockQuote>>(`${this.API}/quote/${symbol.toUpperCase()}`);
  }

  searchStocks(query: string): Observable<ApiResponse<StockSearchResult[]>> {
    const params = new HttpParams().set('q', query);
    return this.http.get<ApiResponse<StockSearchResult[]>>(`${this.API}/search`, { params });
  }

  getHistory(symbol: string, outputSize: 'compact' | 'full' = 'compact'): Observable<ApiResponse<OHLCVDataPoint[]>> {
    const params = new HttpParams().set('outputSize', outputSize);
    return this.http.get<ApiResponse<OHLCVDataPoint[]>>(`${this.API}/history/${symbol.toUpperCase()}`, { params });
  }

  getBatchQuotes(symbols: string[]): Observable<ApiResponse<StockQuote[]>> {
    const params = new HttpParams().set('symbols', symbols.join(','));
    return this.http.get<ApiResponse<StockQuote[]>>(`${this.API}/quotes/batch`, { params });
  }

  getMarketOverview(): Observable<ApiResponse<StockQuote[]>> {
    return this.http.get<ApiResponse<StockQuote[]>>(`${this.API}/market-overview`).pipe(
      shareReplay(1)
    );
  }
}
