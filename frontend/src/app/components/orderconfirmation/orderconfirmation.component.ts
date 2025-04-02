import { Component, OnInit  } from '@angular/core';

import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { PaymentService } from '../../services/payment.service';
import { Order } from '../../models/order';
@Component({
  selector: 'app-orderconfirmation',
  templateUrl: './orderconfirmation.component.html',
  styleUrls: ['./orderconfirmation.component.scss']
})
export class OrderConfirmationComponent implements OnInit {
  orderId: number = 0;
  order: any |null = null;
   

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
     
     this.paymentService.getPaymentData().subscribe((paymentData) => {
      if (paymentData) {
        this.orderId = paymentData.orderId;        

        
        this.orderService.getOrderById(this.orderId).subscribe({
          next: (order) => {
            this.order = order;
          },
          error: (err) => {
            console.error('Errore durante il recupero dell\'ordine:', err);
            this.router.navigate(['/shop']);
          }
        });
      } else {
        console.error('Nessun dato di pagamento trovato.');
        this.router.navigate(['/shop']);
      }
    });
  }


  proceedToPayment() {   
    
    this.paymentService.setPaymentData({ orderId: this.order?.content[0].sequId ??0, totalAmount: this.order?.content[0].totalPrice??0 });
    this.router.navigate(['/payment']);
    
  }

}
