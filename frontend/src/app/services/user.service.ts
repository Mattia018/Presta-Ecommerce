
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user';
import { Order } from '../models/order';
import { environment } from '../../environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) {}

  // Ottieni i dettagli dell'utente
  getUserDetails(userId: number): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/users/${userId}`);
  }

  // Aggiorna il profilo dell'utente
  updateUserProfile(userId: number, userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${environment.apiUrl}/users/${userId}`, userData);
  }

  // Ottieni tutti gli ordini dell'utente
  getUserOrders(userId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${environment.apiUrl}/users/${userId}/orders`);
  }

  // Ottieni i dettagli di un ordine specifico
  getOrderDetails(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${environment.apiUrl}/orders/${orderId}`);
  }

  // Count Users
  getTotalUser(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/users/admin/count`);
  }
}