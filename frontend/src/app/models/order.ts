
import { User } from './user';
import { Product } from './product';

export interface Order {
  sequId: number; 
  user: User; 
  items: OrderItem[];
  totalPrice: number;
  status: string;
  userEmail: string;
  productCount: number;
  createdAt: Date; 
  updatedAt: Date;
  shippingAddress:string;
}

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  price: number;
}