import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { Routes } from '@angular/router';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { ShopComponent } from './components/shop/shop.component';
import { HomeComponent } from './components/home/home.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';



import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatNativeDateModule } from '@angular/material/core';
import { PaginatorModule } from 'primeng/paginator';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { CheckoutComponent } from './components/checkout/checkout.component';
import { PaymentComponent } from './components/payment/payment.component';
import { OrderConfirmationComponent } from './components/orderconfirmation/orderconfirmation.component';
import { OrdersComponent } from './components/orders/orders.component';
import { OrderDetailsComponent } from './components/order-details/order-details.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { ShopDetailsComponent } from './components/shop-details/shop-details.component';
import { OrderSuccessComponent } from './components/order-success/order-success.component';
import { ChatbotComponent } from './components/chatbot/chatbot.component';

// PrimeNG Modules
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { SliderModule } from 'primeng/slider';
import { InputNumberModule } from 'primeng/inputnumber';
import { ButtonModule } from 'primeng/button';
import { BadgeModule } from 'primeng/badge';
import { ChipModule } from 'primeng/chip';
import { TooltipModule } from 'primeng/tooltip';
import { RadioButtonModule } from 'primeng/radiobutton';
import { NgxSliderModule } from '@angular-slider/ngx-slider';
import { ProfileComponent } from './components/profile/profile.component';
import { CountUpModule } from 'ngx-countup';
import {ChartModule} from 'primeng/chart';
import { FooterComponent } from './components/footer/footer.component';



const routes: Routes = [
  
  
];

@NgModule({
  declarations: [
    AdminDashboardComponent,
    ShopComponent,
    AppComponent,
    LoginComponent,
    RegisterComponent,
    NavbarComponent,
    CheckoutComponent,
    PaymentComponent,
    OrderConfirmationComponent,
    OrdersComponent,
    OrderDetailsComponent,
    ShopDetailsComponent,
    HomeComponent,
    ProfileComponent,
    ChatbotComponent,
    OrderSuccessComponent,
    FooterComponent,
    
    
  ],
  imports: [
    BrowserModule,
    CommonModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    BrowserAnimationsModule,
    MatPaginatorModule,
    MatTableModule,
    MatNativeDateModule,
    MatDatepickerModule,
    MatFormFieldModule,
    PaginatorModule,    
    CountUpModule,

    // PrimeNG
    InputTextModule,
    DropdownModule,
    MultiSelectModule,
    SliderModule,
    InputNumberModule,
    ButtonModule,
    BadgeModule,
    ChipModule,
    RadioButtonModule,
    NgxSliderModule,
    TooltipModule,
    ChartModule

  ],
  
  providers: [ 
  {
    provide: HTTP_INTERCEPTORS,
    useClass: AuthInterceptor,
    multi: true
  },
],
  bootstrap: [AppComponent]
})
export class AppModule { }
