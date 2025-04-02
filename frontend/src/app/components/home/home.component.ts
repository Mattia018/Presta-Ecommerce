import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { OrderService } from 'src/app/services/order.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {

  userId
  topCategories: any[] = [];
  topProducts: any[] = [];

  constructor(
      
      private orderService: OrderService,      
      private authService: AuthService,      
      private http: HttpClient,
      private router: Router
    ) { 
      this.userId = this.authService.getCurrentUserId();
    }


    ngOnInit(): void {
      // Top categorie
      this.orderService.getTopCategories().subscribe(
        (data) => {
          this.topCategories = data;
        },
        (error) => {
          console.error('Errore durante il recupero delle top categorie:', error);
        }
      );
  
      // Top prodotti
      this.orderService.getTopProducts().subscribe(
        (data) => {
          this.topProducts = data;
        },
        (error) => {
          console.error('Errore durante il recupero dei top prodotti:', error);
        }
      );
    }

}
