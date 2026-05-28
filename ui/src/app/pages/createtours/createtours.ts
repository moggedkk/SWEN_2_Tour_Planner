import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Navbar } from '../../components/navbar/navbar';
import { TourActions } from '../../components/tour-actions/tour-actions';
import { TourService } from '../../services/TourService';
import { Tour, TransportType } from '../../models/Tour';
import { CommonModule } from '@angular/common';
import { ToastService, ToastType } from '../../services/ToastService';

@Component({
  selector: 'app-createtours',
  imports: [Navbar, TourActions, FormsModule, CommonModule],
  templateUrl: './createtours.html',
  styleUrl: './createtours.css',
})
export class Createtours {
  private tourService = inject(TourService);
  private toastService = inject(ToastService);

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

  transportTypes = Object.values(TransportType);

  onSubmit(): void {
    const newTour = new Tour(
      0,
      this.tourForm.name,
      this.tourForm.start,
      this.tourForm.end,
      this.tourForm.difficulty,
      this.tourForm.description,
      this.tourForm.transportType,
      this.tourForm.distance,
      this.tourForm.estimatedTime
    );

    this.tourService.addTour(newTour).subscribe({
      next: () => {
        this.toastService.show(`Tour "${newTour.name}" created!`, ToastType.Success);
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
      },
      error: () => {
        this.toastService.show('Failed to create tour. Please try again.', ToastType.Danger);
      }
    });
  }
}
