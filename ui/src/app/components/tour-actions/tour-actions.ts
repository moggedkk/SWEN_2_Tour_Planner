import { Component, EventEmitter, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Tour, TransportType } from '../../models/Tour';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tour-actions',
  imports: [RouterLink, RouterLinkActive, FormsModule, CommonModule],
  templateUrl: './tour-actions.html',
  styleUrl: './tour-actions.css',
})
export class TourActions {
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

    this.tourCreated.emit(newTour);

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
