import { Product } from "./product";


export interface RecommendationResponse {
    recommendationText: string;
    products: Product[];
  }