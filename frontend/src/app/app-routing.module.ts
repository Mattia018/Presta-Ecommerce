
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { ShopComponent } from './components/shop/shop.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { CheckoutComponent } from './components/checkout/checkout.component';
import { OrderConfirmationComponent } from './components/orderconfirmation/orderconfirmation.component';
import { PaymentComponent } from './components/payment/payment.component';
import { OrdersComponent } from './components/orders/orders.component';
import { OrderDetailsComponent } from './components/order-details/order-details.component';
import {ShopDetailsComponent} from'./components/shop-details/shop-details.component';
import { HomeComponent } from './components/home/home.component';
import { ProfileComponent } from './components/profile/profile.component';
import { OrderSuccessComponent } from './components/order-success/order-success.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'admin', component: AdminDashboardComponent, canActivate: [RoleGuard]},
  { path: 'shop', component: ShopComponent },
  { path: 'home', component: HomeComponent },
  { path: 'checkout', component: CheckoutComponent, canActivate: [AuthGuard]},
  { path: 'order-confirmation', component: OrderConfirmationComponent, canActivate: [AuthGuard] },
  { path: 'order-success', component: OrderSuccessComponent, canActivate: [AuthGuard] },
  { path: 'payment', component: PaymentComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'order-details/:id', component: OrderDetailsComponent, canActivate: [AuthGuard]},
  { path: 'shop-details/:id', component: ShopDetailsComponent},
  { path: '', redirectTo: '/shop', pathMatch: 'full' },
  { path: '**', redirectTo: '/shop' },
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
