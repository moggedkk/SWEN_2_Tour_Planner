import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Main } from './pages/main/main';
import { Profile } from './pages/profile/profile';
import { Createtours } from './pages/createtours/createtours';
import { Import } from './pages/import/import';
import { TourDetails } from './pages/tour-details/tour-details';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: Login },
  { path: 'register', component: Register },
  { path: 'main', component: Main, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: 'createtours', component: Createtours, canActivate: [authGuard] },
  { path: 'import', component: Import, canActivate: [authGuard] },
  { path: 'tour-details/:id', component: TourDetails, canActivate: [authGuard] },
];
