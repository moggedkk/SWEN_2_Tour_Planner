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
  };

  transportTypes = Object.values(TransportType);

  onSubmit(): void {
    const newTour: Tour = {
      name: this.tourForm.name,
      start: this.tourForm.start,
      end: this.tourForm.end,
      difficulty: this.tourForm.difficulty,
      description: this.tourForm.description,
      transportType: this.tourForm.transportType,
      distance: 0,
      estimatedTime: 0,
      logs: []
    };

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
        };
      },
      error: (err) => {
        const message = err.error?.message ?? 'Failed to create tour. Please try again.';
        this.toastService.show(message, ToastType.Danger);
      }
    });
  }
}
