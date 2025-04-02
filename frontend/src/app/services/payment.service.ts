import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private paymentDataSubject = new BehaviorSubject<{ orderId: number; totalAmount: number } | null>(null);

  setPaymentData(data: { orderId: number; totalAmount: number }) {
    this.paymentDataSubject.next(data);
  }

  getPaymentData(): Observable<{ orderId: number; totalAmount: number } | null> {
    return this.paymentDataSubject.asObservable();
  }
  
  constructor() { }
}
