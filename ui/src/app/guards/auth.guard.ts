import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/AuthService';

// Client-side counterpart to Spring Security's SecurityConfig.
// Spring protects the API endpoints; this guard protects the UI routes.
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) return true;

  return router.createUrlTree(['/']);
};
