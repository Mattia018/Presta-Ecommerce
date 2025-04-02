
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environment';
import { of,Observable, BehaviorSubject,throwError } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { HttpHeaders} from '@angular/common/http';
import { catchError} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  private isAuthenticated = new BehaviorSubject<boolean>(false);
  private userId = new BehaviorSubject<number | null>(null);
  private accessToken: string | null = null;
  private refToken: string | null = null;
  private currentUserSubject: BehaviorSubject<any>;
  public currentUser: Observable<any>;
  private refreshTokenTimeout: any;

  constructor(private http: HttpClient, private router: Router) {  

    // Load Current User

    const storedUser = localStorage.getItem('auth_data');
    if (storedUser) {
      const currentUser = JSON.parse(storedUser);
      this.currentUserSubject = new BehaviorSubject<any>(currentUser);
      this.currentUser = this.currentUserSubject.asObservable();
      this.accessToken = currentUser.accessToken;
      this.refToken=currentUser.refreshToken;
      
      this.isAuthenticated.next(true);
      this.startRefreshTokenTimer(currentUser);
      
    } else {
      this.currentUserSubject = new BehaviorSubject<any>(null);
      this.currentUser = this.currentUserSubject.asObservable();
    }
    
  }

  public get currentUserValue(): any {
    return this.currentUserSubject.value;
  }

  // Metodo per ottenere l'ID utente corrente
  getCurrentUserId(): number | null {
    const currentUser = this.currentUserValue;
    return currentUser && currentUser.userId ? currentUser.userId : null;
  }

  
  // Registrer
   
  register(user: { username: string, name: string; surname: string; email: string; password: string }): Observable<any> {
    return this.http.post(`${environment.apiUrl}/auth/register`, user);
  }

  // Login

  login(username: string, password: string): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/auth/login`, { username, password })
      .pipe(
        tap(user => {

          
          // Salva l'utente nel localStorage
          localStorage.setItem('auth_data', JSON.stringify(user));          
          
          // Aggiorna lo stato dell'autenticazione
          this.currentUserSubject.next(user);
          this.isAuthenticated.next(true);
          
          // Timer per il refresh token
          this.startRefreshTokenTimer(user);
        }),
        catchError(error => {
          return throwError(() => new Error('Login fallito: ' + error.message));
        })
      );
  }

  // Refresh Token Timer

  private startRefreshTokenTimer(user: any): void {
    
    this.stopRefreshTokenTimer();

    const expiresIn = user.expiresIn || 300; // Default 5 minuti 
    const timeout = (expiresIn - 300) * 1000;
    
    if (timeout > 0) {
      this.refreshTokenTimeout = setTimeout(() => {
        this.refreshToken().subscribe();
      }, timeout);
    }
  }

  private stopRefreshTokenTimer(): void {
    if (this.refreshTokenTimeout) {
      clearTimeout(this.refreshTokenTimeout);
    }
  }



// Refresh Token

refreshToken(): Observable<any> {
  const user = this.currentUserSubject.value;
  
  if (!user) {
    return throwError(() => new Error('Nessun utente autenticato'));
  }
  
  return this.http.post<any>(`${environment.apiUrl}/auth/refresh`, {refreshToken: user.refreshToken })
    .pipe(
      tap(newTokens => {

        // Aggiorna token utente corrente

        const updatedUser = {
          ...user,
          accessToken: newTokens.accessToken,
          refreshToken: newTokens.refreshToken,
          expiresIn: newTokens.expiresIn
        };
        
        // Salva l'utente aggiornato
        localStorage.setItem('auth_data', JSON.stringify(updatedUser));
        this.currentUserSubject.next(updatedUser);
        
        // Riavvia il timer per il refresh token
        this.startRefreshTokenTimer(updatedUser);
        //console.log("token refresh OK");
      }),
      catchError(error => {
        
        this.logout();
        return throwError(() => new Error('Refresh token fallito: ' + error.message));
      })
    );
    
}


  // Logout

  logout(): void {    
  
    const currentUser = this.currentUserSubject.value;
  
    if (!currentUser || !currentUser.refreshToken) {
      
      this.clearAuthData();
      window.location.href = '/login';
      return;
    }
  
    const logoutUrl = `${environment.apiUrl}/auth/logout`;
    const body = { refreshToken: currentUser.refreshToken };  
    
  
    this.http.post<any>(logoutUrl, body, { headers: new HttpHeaders({ 'Content-Type': 'application/json' }) })
      .pipe(
        tap(response => {
          console.log("Logout avvenuto con successo:", response);
          this.clearAuthData();
          window.location.href = '/home';
        }),
        catchError(error => {
          console.error("Errore durante il logout:", error);
          this.clearAuthData();
          window.location.href = '/login';
          return of({ message: 'Logout completato con errori', error });
        })
      )
      .subscribe();
  }


// Metodo per ottenere il token di accesso corrente
getAccessToken(): string {
  const user = this.currentUserSubject.value;
  return user ? user.accessToken : '';
}

// Metodo per verificare se l'utente ha il ruolo di amministratore
isAdmin(): boolean {
  const currentUser = this.currentUserValue;
  if (currentUser && currentUser.roles) {
    return currentUser.roles.includes('ADMIN');
  }
  return false;
}


// Metodo per verificare se l'utente è autenticato
isLoggedIn(): Observable<boolean> {
  return this.isAuthenticated.asObservable();
}


// Metodo per ottenere l'ID utente come Observable
getUserId(): Observable<number | null> {
  return this.userId.asObservable();
}

// Clear local storage
clearAuthData(): void {  
  localStorage.removeItem('auth_data'); 
  this.currentUserSubject.next(null);
  this.isAuthenticated.next(false);
  
}

  
}