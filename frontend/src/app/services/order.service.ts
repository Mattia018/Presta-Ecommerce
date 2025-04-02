import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order';
import { environment } from '../../environment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(private http: HttpClient) { }

  createOrder(userId: number, shippingAddress: string): Observable<Order> {
    return this.http.post<Order>(`${environment.apiUrl}/orders`, { userId, shippingAddress });
  }

  getOrderById(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${environment.apiUrl}/orders/${orderId}`);
  }

  getOrderItems(orderId: number, page: number, size: number): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Order>(`${environment.apiUrl}/orders/${orderId}`, { params });
  }

  getOrdersByUser(userId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${environment.apiUrl}/orders/user/${userId}`);
  }

  updateOrderStatus(orderId: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${environment.apiUrl}/orders/${orderId}/${status}`, { status });
  }

  cancelOrder(orderId: number): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/orders/${orderId}/cancel`, {});
  }

  getAllOrdersPaged(params: HttpParams): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/orders/admin`, { params });
  }
  
  getOrdersByUserPaged(userId: number, page: number, size: number, statusFilter: string, sortBy: string, sortDirection: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
  
    if (statusFilter) {
      params = params.set('status', statusFilter);
    }
  
    return this.http.get<any>(`${environment.apiUrl}/orders/user/${userId}`, { params });
  }

  
  getTopCategories(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/orders/top-categories`);
  }
  
  getTopProducts(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/orders/top-products`);
  }

  getOrderCountsByUser(userId: number): Observable<any> {
    return this.http.get<Order[]>(`${environment.apiUrl}/orders/${userId}/orders-count`);
  }

  getCountConfirmedOrders(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/orders/admin/count-orders`);
  }

  getTotalRevenue(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/orders/admin/total-revenue`);
  }

  getDailySales(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrl}/orders/admin/daily-sales`);
  }


}
