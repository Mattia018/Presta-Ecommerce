import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  constructor(private http: HttpClient) {}

  // Ottieni tutte le categorie
  getAllCategories(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/categories`);
  }

  // Ottieni una categoria per ID
  getCategoryById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/categories/${id}`);
  }

  // Crea una nuova categoria
  createCategory(category: { title: string }): Observable<any> {
    return this.http.post(`${environment.apiUrl}/categories`, category);
  }

  // Aggiorna una categoria esistente
  updateCategory(id: number, newTitle: string): Observable<any> {
    const url = `${environment.apiUrl}/categories/${id}`;
    return this.http.put(url, null, { params: { newTitle } });
  }

  // Elimina una categoria
  deleteCategory(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/categories/${id}`);
  }

  // Count prodotti associati alla categoria
  getProductCountByCategory(): Observable<Map<number, number>> {
    return this.http.get<Map<number, number>>(`${environment.apiUrl}/categories/count`);
  }

  //Count categorie
  getTotalCount(): Observable<number> {
    return this.http.get<number>(`${environment.apiUrl}/categories/admin/count`);
  }

}