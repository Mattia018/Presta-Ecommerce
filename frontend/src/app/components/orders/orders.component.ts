import { Component, OnInit} from '@angular/core';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { PaymentService } from 'src/app/services/payment.service';
import { Order } from 'src/app/models/order';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  userId: number | null = null;
  page: number = 0;
  size: number = 4;
  totalOrders: number = 0;
  sortBy: string = 'createdAt';
  sortDirection: string = 'DESC';
  statusFilter: string ='';

  constructor(
      private orderService: OrderService,
      private authService: AuthService,
      private router: Router,
      private paymentService: PaymentService,
  ) {
      this.userId = this.authService.getCurrentUserId();
  }

  ngOnInit(): void {

    this.userId = this.authService.getCurrentUserId();
    if (this.userId) {
      this.loadOrders();
    }
    
  }

  loadOrders(): void {
    if (this.userId) {
      this.orderService.getOrdersByUserPaged(
        this.userId, 
        this.page, 
        this.size, 
        this.statusFilter, 
        this.sortBy, 
        this.sortDirection
      ).subscribe(
        (data) => {
          this.orders = data.content; 
          this.totalOrders = data.totalElements; 
        },
        (error) => {
          console.error('Errore durante il recupero degli ordini:', error);
        }
      );
    }
  }

  onPageChange(event: any): void {
    this.page = event.page;
    this.size = event.rows;
    this.loadOrders();
  }

  onSortChange(sortBy: string): void {
    
    if (this.sortBy === sortBy) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      
      this.sortBy = sortBy;
      this.sortDirection = 'DESC';
    }
    this.loadOrders();
  }

  onStatusFilterChange(status: string): void {
    this.statusFilter = status;
    this.page = 0; 
    this.loadOrders(); 
  }

  payOrder(orderId: number): void {
    const order = this.orders.find((o) => o.sequId === orderId);
    if (!order) {
      console.error('Ordine non trovato.');
      return;
    }
  
    
    this.paymentService.setPaymentData({
      orderId: order.sequId,
      totalAmount: order.totalPrice     
      
    });

    this.router.navigate(['/payment']);
    
  }

  cancelOrder(orderId: number): void {
      this.orderService.cancelOrder(orderId).subscribe(
          () => {
              this.orders = this.orders.filter((order) => order.sequId !== orderId);
              alert('Ordine cancellato con successo.');
          },
          (error) => {
              console.error('Errore durante la cancellazione dell\'ordine:', error);
          }
      );
  }

  viewOrderDetails(orderId: number): void {    
      this.router.navigate(['/order-details', orderId]);
  }
}
