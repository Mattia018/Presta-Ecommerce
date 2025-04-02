import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CartEventService {
  
  private cartUpdated = new Subject<void>();
  private shopPageLoadedSource = new Subject<void>();
  private orderSuccess = new Subject<void>();
  
  cartUpdated$ = this.cartUpdated.asObservable();
  shopPageLoaded$ = this.shopPageLoadedSource.asObservable();
  
  
  notifyCartUpdated(): void {
    this.cartUpdated.next();
  }

  notifyShopPageLoaded() {
    this.shopPageLoadedSource.next();
  } 

}
