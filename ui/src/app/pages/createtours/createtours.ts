import { Component } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { TourActions } from '../../components/tour-actions/tour-actions';
import { Tour } from '../../models/Tour';
import { TourService } from '../../services/tour';

@Component({
  selector: 'app-createtours',
  imports: [Navbar, TourActions],
  templateUrl: './createtours.html',
  styleUrl: './createtours.css',
})
export class Createtours {
  constructor(private tourService: TourService) {}

  addTour(newTour: Tour): void {
    this.tourService.addTour(newTour);
    console.log('Tour added via service:', newTour);
  }
}
