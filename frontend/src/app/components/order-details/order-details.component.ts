import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss']
})
export class OrderDetailsComponent implements OnInit {
  order!: Order;
  orderId: number | null = null;
  paginatedItems: any[] = [];
  rows: number = 6;
  totalRecords: number = 0;
  userId: number | null = null;

  constructor(
      private route: ActivatedRoute,
      private authService: AuthService,
      private router: Router,
      private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.orderId = +this.route.snapshot.paramMap.get('id')!;
    this.userId = this.authService.getCurrentUserId();
    this.loadOrderItems();   

  }

  loadOrderItems(): void {
    if (this.orderId) {
      this.orderService.getOrderItems(this.orderId, 0, 0 ).subscribe(
        (response) => {
          this.order = response.content[0];
          
          // Controllo se l'utente loggato è il proprietario dell'ordine
          if (this.order.user.sequId !== this.userId && !this.authService.isAdmin()) {
            
            this.router.navigate(['/shop']);
            return;
          }

          this.totalRecords = this.order.items.length;
          this.paginate({ first: 0, rows: this.rows });
          console.log("order items: ",this.order.items)
        },
        (error) => {
          console.error('Errore durante il recupero dei prodotti:', error);
        }
      );
    }
  }

  onPageChange(event: any): void {
    this.paginate(event);
  }

  paginate(event: any): void {
    const start = event.first;
    const end = start + event.rows;
    this.paginatedItems = this.order.items.slice(start, end);
  }

  goBack(): void {
      window.history.back();
  }

  getProgressValue(status: string): number {
    switch (status) {
      case 'PENDING':
        return 1; 
      case 'PAID':
        return 2; 
      case 'CONFIRMED':
        return 3; 
      case 'SHIPPED':
        return 4; 
      case 'DELIVERED':
        return 5; 
      default:
        return 0;
    }
  }

  
  


}