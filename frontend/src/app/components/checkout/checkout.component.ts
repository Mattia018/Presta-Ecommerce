
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { UserService } from '../../services/user.service';
import { Cart } from '../../models/cart';
import { Router } from '@angular/router';
import { User } from 'src/app/models/user';
import { Order } from 'src/app/models/order';
import { HttpClient } from '@angular/common/http';
import { PaymentService } from 'src/app/services/payment.service';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {
  cart: Cart | null = null;
  totalPrice: number = 0;
  currentStep: number = 1; 
  order: Order | null = null;
  userId: number | null;
  addressSuggestions: any[] = [];
  selectedAddress: any = null;
  shippingAddress: string = '';

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private userService: UserService,
    private authService: AuthService,
    private paymentService:  PaymentService,
    private http: HttpClient,
    private router: Router
  ) { 
    this.userId = this.authService.getCurrentUserId();
  }

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.getProductsInCart(userId).subscribe((cart: Cart) => {
        this.cart = cart;
      });

      this.cartService.getTotalPriceInCart(userId).subscribe((total: number) => {
        this.totalPrice = total;
      });
    }
  }

  updateQuantity(productId: number, quantity: number): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.updateProductQuantity(userId, productId, quantity).subscribe(() => {
        this.loadCart(); 
      });
    }
  }

  removeProduct(productId: number): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.removeProductFromCart(userId, productId).subscribe(() => {
        this.loadCart(); // Ricarica il carrello dopo la rimozione
      });
    }
  }

  proceedToPayment() {
    if (this.userId === null) {
      
      this.router.navigate(['/login']);
      return;
    }

    if (!this.shippingAddress || this.shippingAddress.trim() === '') {
      alert('Inserisci un indirizzo di spedizione valido.');
      return;
    }

    this.orderService.createOrder(this.userId, this.shippingAddress).subscribe({
      next: (order) => {        
        
        this.paymentService.setPaymentData({ orderId: order?.sequId ??0, totalAmount: order?.totalPrice??0 });
        this.router.navigate(['/order-confirmation']);
      },
      error: (err) => {
        console.error('Errore durante la creazione dell\'ordine:', err);
      }
    });
  }
  
  searchAddress(query: string): void {
    if (query.trim() === '') {
      this.addressSuggestions = [];
      return;
    } 

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5&countrycodes=IT`;
    this.http.get<any[]>(url).subscribe(
      (data) => {
        this.addressSuggestions = data;
      },
      (error) => {
        console.error('Errore:', error);
      }
    );
  }
  
  
  selectAddress(address: any): void {
    this.shippingAddress = address.display_name;
    this.addressSuggestions = [];
    this.selectedAddress = address;
  }

}