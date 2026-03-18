import { Routes } from '@angular/router';
import { Main } from './main/main';
import { Profile } from './profile/profile';

export const routes: Routes = [ 
    {path: '', component: Main},
    {path: 'profile', component: Profile}
];
