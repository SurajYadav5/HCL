// ─── Auth Models ──────────────────────────────────────────────
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  role: 'USER' | 'ADMIN';
  cashBalance: number;
  createdAt: string;
}

// ─── Market Data Models ───────────────────────────────────────
export interface StockQuote {
  symbol: string;
  companyName: string;
  price: number;
  open: number;
  high: number;
  low: number;
  previousClose: number;
  change: number;
  changePercent: number;
  volume: number;
  avgVolume?: number;
  marketCap?: number;
  peRatio?: number;
  week52High?: number;
  week52Low?: number;
  lastUpdated: string;
  exchange?: string;
}

export interface StockSearchResult {
  symbol: string;
  name: string;
  type: string;
  region: string;
  currency: string;
}

export interface OHLCVDataPoint {
  date: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

// ─── Order Models ─────────────────────────────────────────────
export type OrderSide = 'BUY' | 'SELL';
export type OrderType = 'MARKET' | 'LIMIT';
export type OrderStatus = 'PENDING' | 'EXECUTED' | 'CANCELLED' | 'FAILED' | 'PARTIALLY_FILLED';

export interface PlaceOrderRequest {
  symbol: string;
  side: OrderSide;
  type: OrderType;
  quantity: number;
  limitPrice?: number;
}

export interface Order {
  id: number;
  symbol: string;
  companyName: string;
  side: OrderSide;
  type: OrderType;
  status: OrderStatus;
  quantity: number;
  limitPrice?: number;
  executedPrice?: number;
  totalValue?: number;
  commission?: number;
  createdAt: string;
  executedAt?: string;
  notes?: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// ─── Portfolio Models ─────────────────────────────────────────
export interface PortfolioItem {
  id: number;
  symbol: string;
  companyName: string;
  sector?: string;
  quantity: number;
  averageCost: number;
  totalInvested: number;
  currentPrice: number;
  currentValue: number;
  unrealizedPnl: number;
  unrealizedPnlPct: number;
  realizedPnl: number;
  allocationPct: number;
  createdAt: string;
}

export interface PortfolioSummary {
  totalInvested: number;
  totalCurrentValue: number;
  totalUnrealizedPnl: number;
  totalUnrealizedPnlPct: number;
  totalRealizedPnl: number;
  cashBalance: number;
  totalPortfolioValue: number;
  totalPositions: number;
  totalTrades: number;
  holdings: PortfolioItem[];
}

// ─── Watchlist Models ─────────────────────────────────────────
export interface WatchlistItem {
  id: number;
  symbol: string;
  companyName: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  addedAt: string;
}

// ─── API Wrapper ──────────────────────────────────────────────
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
