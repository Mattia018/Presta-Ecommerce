
import { Component, OnInit } from '@angular/core';
import { loadStripe, Stripe, StripeElements, StripeError,PaymentIntent } from '@stripe/stripe-js';

import { HttpClient } from '@angular/common/http';
import { OrderService } from '../../services/order.service';
import { environment } from '../../../environment';
import { Router } from '@angular/router';
import { PaymentService } from 'src/app/services/payment.service';



type ConfirmPaymentResponse = {
  error?: StripeError;
  paymentIntent?: PaymentIntent;
};


@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.scss']
})
export class PaymentComponent implements OnInit {
  stripe: Stripe | null=null;
  elements: any;
  cardElement: any;
  orderId!: number;
  totalAmount!: number;
  clientSecret:any;

  

  constructor(private http: HttpClient,private router: Router,private paymentService: PaymentService) {}

  
  async createPaymentIntent(totalAmount: number): Promise<string> {
    try {
        const response = await this.http.post<{ clientSecret: string }>(
            `${environment.apiUrl}/payment/create-payment-intent`,
            { totalAmount }
        ).toPromise();

        if (!response || !response.clientSecret) {
            throw new Error('Errore durante la creazione del PaymentIntent');
        }

        return response.clientSecret;
    } catch (error) {
        console.error('Errore durante la creazione del PaymentIntent:', error);
        throw error;
    }
}
  
  
  
async ngOnInit() {
  try {
      
      this.stripe = await loadStripe(environment.stripePublicKey);

      if (!this.stripe) {
          console.error('Errore durante il caricamento di Stripe.');
          return;
      }

      
      this.paymentService.getPaymentData().subscribe((data) => {
        if (data) {
          this.orderId = data.orderId;
          this.totalAmount = data.totalAmount;
          

          //PaymentIntent e ottieni il clientSecret
          this.createPaymentIntent(this.totalAmount).then((clientSecret) => {
            this.clientSecret = clientSecret;

          
          this.elements = this.stripe!.elements({
            clientSecret: this.clientSecret,
            locale: 'auto', 
          });

          
          const paymentElement = this.elements.create('payment');
          paymentElement.mount('#payment-element'); 
          });

          
        } else {
          console.error('Dati del pagamento non disponibili.');
        }
      });
    } catch (error) {
      console.error('Errore durante l\'inizializzazione del pagamento:', error);
    }
  }

  async onSubmit() {
    if (!this.stripe || !this.clientSecret) {
      
      console.error('Stripe o clientSecret non inizializzato.');
      return;
    }
  
    try {
      
  
      
      const result: ConfirmPaymentResponse = await this.stripe.confirmPayment({
        elements: this.elements,
        confirmParams: {
          return_url: `${window.location.origin}/confirmation`,
        },
        redirect: 'if_required', 
      });
  
      
      if (result.error) {
        
        console.error('Errore durante la conferma del pagamento:', result.error.message);
        return;
      }
  
      
      if (!result.paymentIntent) {
        
        console.error('Impossibile ottenere il paymentIntent.');
        return;
      }
  
      // PaymentMethodId dal paymentIntent
      const paymentMethodId = result.paymentIntent.payment_method;
  
      if (!paymentMethodId) {
        
        console.error('Impossibile ottenere il paymentMethodId.');
        return;
      }
       
  
      // Confirm Payment
      this.http.post(`${environment.apiUrl}/payment/confirm`, {
        orderId: this.orderId,
        paymentMethodId: paymentMethodId,
      }).subscribe(
        (response) => {  
          this.router.navigate(['/order-success']).then(() => {
            window.location.reload();
          });
        },
        (error) => {
          console.error('Errore durante la conferma dell\'ordine sul backend:', error);
          
        }
      );
    } catch (error) {
      console.error('Errore generico durante la conferma del pagamento:', error);
      alert('Errore generico durante la conferma del pagamento.');
    }
  }
}