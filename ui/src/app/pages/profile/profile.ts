import { Component, OnInit, OnDestroy } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { CommonModule } from '@angular/common';
import { Tour } from '../../models/Tour';
import { TourService } from '../../services/tour';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [Navbar, CommonModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
})
export class Profile implements OnInit, OnDestroy {
  username: string = 'User Name'; // Placeholder
  createdTours: Tour[] = [];
  completedTours: Tour[] = []; // Usually you'd have logic for this too
  private toursSubscription?: Subscription;

  constructor(private tourService: TourService) {}

  ngOnInit() {
    this.toursSubscription = this.tourService.tours$.subscribe(tours => {
      this.createdTours = tours;
      // Just for demonstration, let's say the first 3 are "completed"
      this.completedTours = tours.slice(0, 3);
    });
  }

  ngOnDestroy() {
    this.toursSubscription?.unsubscribe();
  }

  /**
   * Deletes a tour after user confirmation.
   */
  onDeleteTour(tourName: string): void {
    const confirmed = window.confirm(`Are you sure you want to delete "${tourName}"?`);

    if (confirmed) {
      this.tourService.deleteTour(tourName);
    }
  }

  getStats() {
    return {
      total: this.createdTours.length,
      distance: this.createdTours.reduce((acc, t) => acc + t.distance, 0)
    };
  }
}
