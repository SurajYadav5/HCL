import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PortfolioSummary } from '../models/models';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly API = '/api/portfolio';

  constructor(private http: HttpClient) {}

  getPortfolio(): Observable<ApiResponse<PortfolioSummary>> {
    return this.http.get<ApiResponse<PortfolioSummary>>(this.API);
  }
}
