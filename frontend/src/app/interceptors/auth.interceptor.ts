import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, EMPTY, Observable, from, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { environment } from 'src/environment';
@Injectable()
export class AuthInterceptor implements HttpInterceptor { 


    private isRefreshing = false;
    private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);
  
    constructor(private authService: AuthService) {}
  
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      
      if (request.url.includes(`${environment.apiUrl}/auth/login`) || request.url.includes(`${environment.apiUrl}/auth/refresh`)) {
        return next.handle(request);
      }
      
      // Add token di accesso alle richieste
      const token = this.authService.getAccessToken();
      if (token) {
        request = this.addTokenHeader(request, token);
      }
  
      return next.handle(request).pipe(
        catchError(error => {
          if (error instanceof HttpErrorResponse && error.status === 401) {
            return this.handle401Error(request, next);
          }
          return throwError(() => error);
        })
      );
    }
  
    private addTokenHeader(request: HttpRequest<any>, token: string): HttpRequest<any> {
      return request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    
    // Refresh token or Logout
    
    private handle401Error(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
      if (!this.isRefreshing) {
        this.isRefreshing = true;
        this.refreshTokenSubject.next(null);
  
        return this.authService.refreshToken().pipe(
          switchMap(token => {
            this.isRefreshing = false;
            this.refreshTokenSubject.next(token);
            return next.handle(this.addTokenHeader(request, token.accessToken));
          }),
          catchError(error => {
            this.isRefreshing = false;
            this.authService.logout();
            return throwError(() => error);
          })
        );
      }
  
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => next.handle(this.addTokenHeader(request, token.accessToken)))
      );
    }
  

  

}