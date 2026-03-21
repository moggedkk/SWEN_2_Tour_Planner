import { Routes } from '@angular/router';
import { Main } from './main/main';
import { Profile } from './profile/profile';
import {Login} from './login/login';
import {Register} from './register/register';
import { Createtours } from './createtours/createtours';

export const routes: Routes = [ 
    {path: '', component: Login},
    {path : "register", component : Register},
    {path: 'profile', component: Profile},
    {path: 'main', component: Main},
    {path: 'createtours', component: Createtours}
];
