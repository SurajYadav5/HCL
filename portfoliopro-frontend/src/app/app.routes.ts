import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./components/layout/layout.component').then(m => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'market',
        loadComponent: () => import('./components/market/market.component').then(m => m.MarketComponent)
      },
      {
        path: 'trade',
        loadComponent: () => import('./components/trade/trade.component').then(m => m.TradeComponent)
      },
      {
        path: 'portfolio',
        loadComponent: () => import('./components/portfolio/portfolio.component').then(m => m.PortfolioComponent)
      },
      {
        path: 'orders',
        loadComponent: () => import('./components/orders/orders.component').then(m => m.OrdersComponent)
      },
      {
        path: 'watchlist',
        loadComponent: () => import('./components/watchlist/watchlist.component').then(m => m.WatchlistComponent)
      },
      {
        path: 'analysis',
        loadComponent: () => import('./components/analysis/analysis.component').then(m => m.AnalysisComponent)
      },
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
