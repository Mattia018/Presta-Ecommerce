import { CartItem } from './cart-item';
import { User } from './user';

export interface Cart {
    sequId: number;
    items: CartItem[];
    user: User;
  }