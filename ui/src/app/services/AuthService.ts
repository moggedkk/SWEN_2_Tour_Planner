import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { map, Observable, tap } from 'rxjs';

interface TokenResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly apiUrl = 'http://localhost:8080/api';

  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);

  login(username: string, password: string): Observable<void> {
    return this.http
      .post<TokenResponse>(`${this.apiUrl}/sessions`, { username, password })
      .pipe(
        tap(response => this.storeToken(response.token)),
        map(() => undefined),
      );
  }

  register(username: string, email: string, password: string): Observable<void> {
    return this.http
      .post<TokenResponse>(`${this.apiUrl}/users`, { username, email, password })
      .pipe(
        tap(response => this.storeToken(response.token)),
        map(() => undefined),
      );
  }

  // Username may change, so the backend re-issues a fresh JWT. We overwrite the
  // stored token so the next request (and the profile page's username decode) sees the new sub.
  updateProfile(username: string, email: string, password: string): Observable<void> {
    return this.http
      .put<TokenResponse>(`${this.apiUrl}/users/me`, { username, email, password })
      .pipe(
        tap(response => this.storeToken(response.token)),
        map(() => undefined),
      );
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId )) {
      localStorage.removeItem(this.TOKEN_KEY);
    }
  }

  getToken(): string | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private storeToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }


}
