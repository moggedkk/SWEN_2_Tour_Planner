import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { CommonModule } from '@angular/common';
import { Tour, TransportType, TourLog } from '../../models/Tour';
import { TourService } from '../../services/TourService';
import { TourLogService, TourLogEntry } from '../../services/tourlog';
import { Subscription, Observable, of } from 'rxjs';
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
  groupedTourLogs: Map<string, TourLogEntry[]> = new Map();
  private toursSubscription?: Subscription;
  private tourLogsSubscription?: Subscription;
  imageUrl: string | null = null;

  // Modern DI using inject()
  private tourService = inject(TourService);
  private tourLogService = inject(TourLogService);
  private toastService = inject(ToastService);

  // Modal State for Tour
  isEditModalOpen = false;
  editingTour: Tour | null = null;
  transportTypes = Object.values(TransportType);

  // Modal State for TourLog
  isEditLogModalOpen = false;
  editingLog: TourLogEntry | null = null;

  constructor() {}

  ngOnInit() {
    this.toursSubscription = this.tourService.tours$.subscribe(tours => {
      this.createdTours = tours;
    });

    this.tourLogsSubscription = this.tourLogService.tourLogs$.subscribe(() => {
      this.groupedTourLogs = this.tourLogService.getTourLogsGroupedByTour();
    });
  }

  ngOnDestroy() {
    this.toursSubscription?.unsubscribe();
    this.tourLogsSubscription?.unsubscribe();
  }

  /**
   * Get tour object by name for displaying grouped logs
   */
  getTourByName(tourName: string): Tour | undefined {
    return this.createdTours.find(tour => tour.name === tourName);
  }

  /**
   * Get all grouped tour names as array for iteration
   */
  getGroupedTourNames(): string[] {
    return Array.from(this.groupedTourLogs.keys());
  }

  onDeleteTour(tour: Tour): void {
    const confirmed = window.confirm(`Are you sure you want to delete "${tour.name}"?`);
    if (confirmed) {
      this.tourService.deleteTour(tour.id!).subscribe({
        next: () => this.toastService.show(`Tour "${tour.name}" deleted successfully!`, ToastType.Danger),
        error: () => this.toastService.show('Failed to delete tour.', ToastType.Danger)
      });
    }
  }

  onEditTour(tour: Tour): void {
    this.editingTour = { ...tour };
    this.isEditModalOpen = true;
  }

  onSaveEdit(): void {
    if (this.editingTour) {
      if (!this.editingTour.name || this.editingTour.name.length < 3 ||
          !this.editingTour.start || this.editingTour.start.length < 2 ||
          !this.editingTour.end || this.editingTour.end.length < 2 ||
          !this.editingTour.difficulty || !this.editingTour.transportType ||
          !this.editingTour.description || this.editingTour.description.length < 10 ||
          this.editingTour.distance < 0.1 || this.editingTour.estimatedTime < 1) {
        this.toastService.show('Please fill in all required fields correctly', ToastType.Danger);
        return;
      }

      this.tourService.updateTour(this.editingTour).subscribe({
        next: () => {
          this.toastService.show(`Tour "${this.editingTour!.name}" updated successfully!`, ToastType.Success);
          this.closeModal();
        },
        error: () => this.toastService.show('Failed to update tour.', ToastType.Danger)
      });
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

  // TourLog CRUD operations
onEditLog(log: TourLogEntry): void {
  this.editingLog = { ...log };
  this.isEditLogModalOpen = true;

  this.imageUrl = log.imagePath
    ? `http://localhost:8080/api/images/${log.imagePath}`
    : null;
}

  onSaveLogEdit(): void {
    if (this.editingLog) {
      // Validate required fields
      if (!this.editingLog.date || this.editingLog.duration < 1 || !this.editingLog.difficultyRating) {
        this.toastService.show(
          'Please fill in all required fields (Date, Duration min 1, and Difficulty Rating)',
          ToastType.Danger
        );
        return;
      }

      // updateTourLog now hits the backend. We pass tourId + logId (both numbers from the DB)
      // and tourName so the service can update the local cache with the right grouping key.
      const log = this.editingLog;
      this.tourLogService.updateTourLog(
        log.tourId,
        log.id,
        {
          date: log.date,
          comment: log.comment,
          duration: log.duration,
          difficultyRating: log.difficultyRating,
          imageName: log.imageName,
          imageEncoded : log.imageEncoded,
          totalDistance: log.totalDistance
        },
        log.tourName
      ).subscribe({
        next: () => {
          this.toastService.show('Tour log updated successfully!', ToastType.Success);
          this.closeLogModal();
        },
        error: () => this.toastService.show('Failed to update tour log.', ToastType.Danger)
      });
    }
  }

  onCancelLogEdit(): void {
    this.closeLogModal();
  }

  onDeleteLog(log: TourLogEntry): void {
    const confirmed = window.confirm(`Are you sure you want to delete this log from "${log.date}"?`);
    if (confirmed) {
      // deleteTourLog needs tourId + logId now because the backend URL is /api/tours/{tourId}/logs/{logId}
      this.tourLogService.deleteTourLog(log.tourId, log.id).subscribe({
        next: () => this.toastService.show('Tour log deleted successfully!', ToastType.Danger),
        error: () => this.toastService.show('Failed to delete tour log.', ToastType.Danger)
      });
    }
  }

  private closeLogModal(): void {
    this.isEditLogModalOpen = false;
    this.editingLog = null;
  }

  getCurrentDate(): string {
    return new Date().toISOString().split('T')[0];
  }
  downloadPicture() :void{

  }
}

