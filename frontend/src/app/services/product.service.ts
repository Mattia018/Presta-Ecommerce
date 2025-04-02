import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environment';
import { Observable } from 'rxjs';
import { Product } from '../models/product';
import { RecommendationResponse } from '../models/RecommendationResponse';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private http: HttpClient) {}

  // Ottieni tutti i prodotti
  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${environment.apiUrl}/products`);
  }

  // Ottieni un prodotto per ID
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${environment.apiUrl}/products/${id}`);
  }

  // Crea un nuovo prodotto
  createProduct(product: { 
    title: string, 
    description: string, 
    imgResources: string, 
    imgResources2: string, 
    imgResources3: string, 
    price: number, 
    stock: number, 
    categoryId: number 
  }): Observable<Product> {
    return this.http.post<Product>(`${environment.apiUrl}/products`, product);
  }

   // Aggiorna un prodotto esistente
   updateProduct(id: number, product: { 
    title: string, 
    description: string, 
    imgResources: string, 
    imgResources2: string, 
    imgResources3: string, 
    price: number, 
    stock: number, 
    categoryId: number 
  }): Observable<Product> {
    return this.http.put<Product>(`${environment.apiUrl}/products/${id}`, product);
  }

  // Elimina un prodotto
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/products/${id}`);
  }

  // Ottieni tutti i prodotti per lo Shop
  getAllProductsShop(
    page: number = 0,
    size: number = 12,
    category?: string,
    minPrice?: number,
    maxPrice?: number,
    search?: string,
    sortBy?: string,
    sortDirection?: string
  ): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
  
    if (category) params = params.set('category', category);
    if (minPrice) params = params.set('minPrice', minPrice.toString());
    if (maxPrice) params = params.set('maxPrice', maxPrice.toString());
    if (search) params = params.set('search', search);
    if (sortBy) params = params.set('sortBy', sortBy);
    if (sortDirection) params = params.set('sortDirection', sortDirection);
  
    return this.http.get<any>(`${environment.apiUrl}/products/shop`, { params });
  }

  // Ottieni tutti i prodotti per lo Shop per Admin
  getAllProductsPaginated(page: number, size: number, sort: string, search: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<any>(`${environment.apiUrl}/products/admin`, { params });
  }

  // Count tutti i prodotti
  getCountProducts(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/products/admin/count`);
  }

  // Shop Assistant
  getRecommendation(query: string): Observable<RecommendationResponse> {    
    const body = { query };
    return this.http.post<RecommendationResponse>(`${environment.apiUrl}/recommendations`, body);
  }
}