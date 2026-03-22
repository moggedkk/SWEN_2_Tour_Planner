import { Component } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { TourActions } from '../../components/tour-actions/tour-actions';

@Component({
  selector: 'app-import',
  imports: [Navbar, TourActions],
  templateUrl: './import.html',
  styleUrl: './import.css',
})
export class Import {

}
