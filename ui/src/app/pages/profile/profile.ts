import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { CommonModule } from '@angular/common';
import { Tour, TransportType, TourLog } from '../../models/Tour';
import { TourService } from '../../services/tour';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ToastService, ToastType } from '../../services/ToastService';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [Navbar, CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
})
export class Profile implements OnInit, OnDestroy {
  username: string = 'User Name';
  createdTours: Tour[] = [];
  recentLogs: Array<{ tour: Tour; log: TourLog }> = [];
  private toursSubscription?: Subscription;

  // Modern DI using inject()
  private tourService = inject(TourService);
  private toastService = inject(ToastService);

  // Modal State
  isEditModalOpen = false;
  editingTour: Tour | null = null;
  transportTypes = Object.values(TransportType);

  constructor() {}

  ngOnInit() {
    this.toursSubscription = this.tourService.tours$.subscribe(tours => {
      this.createdTours = tours;
      this.recentLogs = this.getRecentLogs(tours);
    });
  }

  /**
   * Extracts the most recent tour logs across all tours.
   * Flattens all logs, sorts by date, and returns the latest 5.
   */
  private getRecentLogs(tours: Tour[]): Array<{ tour: Tour; log: TourLog }> {
    const allLogs = tours
      .flatMap(tour =>
        (tour.logs || []).map(log => ({ tour, log }))
      )
      .sort((a, b) => new Date(b.log.date).getTime() - new Date(a.log.date).getTime());

    return allLogs.slice(0, 5);
  }

  ngOnDestroy() {
    this.toursSubscription?.unsubscribe();
  }

  onDeleteTour(tourName: string): void {
    const confirmed = window.confirm(`Are you sure you want to delete "${tourName}"?`);
    if (confirmed) {
      this.tourService.deleteTour(tourName);
      this.toastService.show(`Tour "${tourName}" deleted successfully!`, ToastType.Danger);
    }
  }

  onEditTour(tour: Tour): void {
    this.editingTour = { ...tour };
    this.isEditModalOpen = true;
  }

  onSaveEdit(): void {
    if (this.editingTour) {
      this.tourService.updateTour(this.editingTour);
      this.toastService.show(`Tour "${this.editingTour.name}" updated successfully!`, ToastType.Success);
      this.closeModal();
    }
  }

  onCancelEdit(): void {
    this.closeModal();
  }

  private closeModal(): void {
    this.isEditModalOpen = false;
    this.editingTour = null;
  }

  getStats() {
    return {
      total: this.createdTours.length,
      distance: this.createdTours.reduce((acc, t) => acc + t.distance, 0)
    };
  }
}
