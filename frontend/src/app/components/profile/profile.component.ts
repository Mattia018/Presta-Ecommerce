import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { User } from 'src/app/models/user';
import { AuthService } from 'src/app/services/auth.service';
import { OrderService } from 'src/app/services/order.service';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {

  userId: number | null = null;
  ordersCount:number = 0;
  userInfo:User | any;
  
  constructor(
       
        private authService: AuthService,
        private router: Router,
        private orderService: OrderService,
        private userService: UserService,

    ) {this.userId = this.authService.getCurrentUserId();}

    ngOnInit(): void {

      this.userId = this.authService.getCurrentUserId();
      if (this.userId) {
        this.countOrders(this.userId);
        this.userDetails(this.userId);
      }
      
    }


    countOrders(userId:number): void{

      if(userId){
        this.orderService.getOrderCountsByUser(userId).subscribe(
          (data) => {
              this.ordersCount = data;
              
          },
          (error) => {
              console.error(error);
          }
      );
      }
    }

    userDetails(userId:number):void{
      if(userId){
        this.userService.getUserDetails(userId).subscribe(
          (data) => {
              this.userInfo = data;              
              
          },
          (error) => {
              console.error(error);
          }
      );
      }
    }
}

