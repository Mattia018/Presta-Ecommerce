import { Category } from "./category";

export interface Product {
    sequId: number;
    title: string;
    description: string;
    price: number;
    stock: number;
    category:Category;
    imgResources:string;
    imgResources2: string;
    imgResources3: string;
  }
