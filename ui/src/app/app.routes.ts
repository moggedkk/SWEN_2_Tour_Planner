import { Routes } from '@angular/router';
import { Main } from './pages/main/main';
import { Profile } from './pages/profile/profile';
import {Login} from './pages/login/login';
import {Register} from './pages/register/register';
import { Createtours } from './pages/createtours/createtours';
import { Import } from './pages/import/import';
import {TourDetails} from './pages/tour-details/tour-details';

export const routes: Routes = [
    {path: '', component: Login},
    {path : "register", component : Register},
    {path: 'profile', component: Profile},
    {path: 'main', component: Main},
    {path: 'createtours', component: Createtours},
    {path: 'import', component: Import},
    {path: 'tour-details/:id', component : TourDetails}
];
