import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cart} from '../models/cart';
import { environment } from '../../environment';
import { CartEventService } from './cart-event.service';
import { tap } from 'rxjs/operators'; 

@Injectable({
  providedIn: 'root'
})
export class CartService {

  constructor(
    private http: HttpClient,
    private cartEventService: CartEventService) {}

  // Aggiunge un prodotto al carrello
  addProductToCart(userId: number, productId: number, quantity: number): Observable<void> {
    const url = `${environment.apiUrl}/cart/add`;
    const body = { userId, productId, quantity };
    return this.http.post<void>(url, body).pipe(
      tap(() => {
        // Notifica che il carrello è stato aggiornato
        this.cartEventService.notifyCartUpdated();
      })
    );
  }

  // Rimuove un prodotto dal carrello
  removeProductFromCart(userId: number, productId: number, quantity?: number): Observable<void> {
    const url = `${environment.apiUrl}/cart/remove`;
    const body = { userId, productId, quantity };
    return this.http.post<void>(url, body).pipe(
      tap(() => {
        // Notifica che il carrello è stato aggiornato
        this.cartEventService.notifyCartUpdated();
      })
    );
  }

  // Ottiene i prodotti nel carrello
  getProductsInCart(userId: number): Observable<Cart> {
    const url = `${environment.apiUrl}/cart/${userId}`;
    return this.http.get<Cart>(url);
  }

  // Ottiene il prezzo totale del carrello
  getTotalPriceInCart(userId: number): Observable<number> {
    const url = `${environment.apiUrl}/cart/total/${userId}`;
    return this.http.get<number>(url);
  }

  // Aggiorna quantità Prodotto
  updateProductQuantity(userId: number, productId: number, quantity: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/cart/updateQuantity`, { userId, productId, quantity }).pipe(
      tap(() => this.cartEventService.notifyCartUpdated())
    );
  }

  // Effettua il checkout
  checkout(userId: number): Observable<void> {
    const url = `${environment.apiUrl}/orders`;
    const body = { userId };
    return this.http.post<void>(url, body);
  }
}
