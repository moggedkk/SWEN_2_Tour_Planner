import { Component } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { TourActions } from '../../components/tour-actions/tour-actions';
@Component({
  selector: 'app-createtours',
  imports: [Navbar, TourActions],
  templateUrl: './createtours.html',
  styleUrl: './createtours.css',
})
export class Createtours {

}
