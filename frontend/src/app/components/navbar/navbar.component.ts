import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, NavigationStart  } from '@angular/router';
import { Cart} from '../../models/cart';
import { CartService } from '../../services/cart.service';
import { CartEventService } from '../../services/cart-event.service';
import { Subscription,Observable } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent {
  cart: Cart | null = null;
  totalItems: number = 0;
  totalPrice: number = 0;
  private cartSubscription: Subscription | undefined;
  private shopPageLoadedSubscription: Subscription | undefined;
  private routerSubscription: Subscription | undefined;  
  private authSubscription: Subscription | undefined;
  isCartOpen: boolean = false; 
  isLoggedIn: boolean = false; 

  
  constructor(
    private cartService: CartService,
    public authService: AuthService,
    private cartEventService: CartEventService,
    private router: Router) {}

  ngOnInit(): void {    
    
    this.loadCart(); 
    

    this.authSubscription = this.authService.isLoggedIn().subscribe((loggedIn) => {
      this.isLoggedIn = loggedIn;
      this.loadCart(); 
    });

    this.cartSubscription = this.cartEventService.cartUpdated$.subscribe(() => {
      this.loadCart();
    });

    this.shopPageLoadedSubscription = this.cartEventService.shopPageLoaded$.subscribe(() => {
      this.loadCart();
    });   

    
    this.routerSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {       
        this.isCartOpen = false;
      }
    });

  

  }

  ngOnDestroy(): void {
    
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
    if (this.shopPageLoadedSubscription) {
      this.shopPageLoadedSubscription.unsubscribe();
    }    
    
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
    }

    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    

  }

  toggleCart(): void {
    this.isCartOpen = !this.isCartOpen;
  }

  loadCart(): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.getProductsInCart(userId).subscribe(cart => {
        this.cart = cart;
        this.totalItems = cart.items.length;
      });

      this.cartService.getTotalPriceInCart(userId).subscribe(totalPrice => {
        this.totalPrice = totalPrice;
      });
    }
  }
  addProductToCart(productId: number, quantity: number): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.addProductToCart(userId, productId, quantity).subscribe(() => {
        this.loadCart(); // Ricarica il carrello dopo l'aggiunta
      });
    }
  }

  removeProductFromCart(productId: number, quantity?: number): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.removeProductFromCart(userId, productId, quantity).subscribe(() => {
        this.loadCart(); // Ricarica il carrello dopo la rimozione
      });
    }
  }

  updateQuantity(productId: number, newQuantity: number): void {
    const userId = this.authService.getCurrentUserId();
    if (userId && newQuantity > 0) { 
      this.cartService.updateProductQuantity(userId, productId, newQuantity).subscribe({
        next: () => {
          this.loadCart(); 
        },
        error: (err) => {
          console.error('Errore durante l\'aggiornamento della quantità:', err);
          alert('Si è verificato un errore durante l\'aggiornamento della quantità.');
        }
      });
    }
  }

  checkout(): void {
    const userId = this.authService.getCurrentUserId();
    if (userId) {
      this.cartService.checkout(userId).subscribe(() => {
        
        this.cart = null;
        this.totalItems = 0;
        this.totalPrice = 0;
      });
    }
  }


  goToCheckout(): void {
    this.router.navigate(['/checkout']);
  }

  
  logout(): void {
    this.authService.logout();   
    
  }
  

  
  
}
