import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Navbar } from '../../components/navbar/navbar';
import { TourActions } from '../../components/tour-actions/tour-actions';
import { TourService } from '../../services/tour';
import { Tour, TransportType } from '../../models/Tour';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-createtours',
  imports: [Navbar, TourActions, FormsModule, CommonModule],
  templateUrl: './createtours.html',
  styleUrl: './createtours.css',
})
export class Createtours {
  constructor(private tourService: TourService) {}
  @Output() tourCreated = new EventEmitter<Tour>();

  // Form model
  tourForm = {
    name: '',
    start: '',
    end: '',
    difficulty: '',
    description: '',
    transportType: TransportType.Foot,
    distance: 0,
    estimatedTime: 0
  };

  // Expose TransportType enum to template  
  transportTypes = Object.values(TransportType);

  onSubmit(): void {
    const newTour = new Tour(
      this.tourForm.name,
      this.tourForm.start,
      this.tourForm.end,
      this.tourForm.difficulty,
      this.tourForm.description,
      this.tourForm.transportType,
      this.tourForm.distance,
      this.tourForm.estimatedTime
    );

    this.tourService.addTour(newTour);

    // Reset form
    this.tourForm = {
      name: '',
      start: '',
      end: '',
      difficulty: '',
      description: '',
      transportType: TransportType.Foot,
      distance: 0,
      estimatedTime: 0
    };
  }
}
