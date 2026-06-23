import { Component, OnInit, OnDestroy, inject, AfterViewInit } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { CommonModule } from '@angular/common';
import { Tour, TransportType, TourLog } from '../../models/Tour';
import { TourService } from '../../services/TourService';
import { TourLogService } from '../../services/tourlog';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ToastService, ToastType } from '../../services/ToastService';
import {Router} from '@angular/router';

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [Navbar, CommonModule, FormsModule],
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main implements AfterViewInit, OnInit, OnDestroy {
  // Services
  public router = inject(Router);
  private tourService = inject(TourService);
  private tourLogService = inject(TourLogService);
  private toastService = inject(ToastService);

  // Data
  allTours: Tour[] = [];
  filteredTours: Tour[] = [];
  private toursSubscription?: Subscription;

  // Four search fields, all AND-combined on the backend.
  // - searchStart / searchEnd / searchTransport filter the tour's own fields
  // - searchQuery is a full-text search across tour fields + logs + computed attributes
  //   (popularity, child-friendliness)
  // Empty = "don't filter on this one".
  searchStart: string = '';
  searchEnd: string = '';
  searchTransport: string = '';
  searchQuery: string = '';
  transportTypes = Object.values(TransportType);

  // Completion Modal State
  isLogModalOpen = false;
  selectedTour: Tour | null = null;
  newLog: TourLog = {
    date: new Date().toISOString().split('T')[0],
    comment: '',
    duration: 0,
    difficultyRating: 'Moderate',
    image: '',
    totalDistance: 0   // gets set to tour.distance when the modal opens
  };

  private map: any = null;

  ngOnInit() {
    this.toursSubscription = this.tourService.tours$.subscribe(tours => {
      this.allTours = tours;
      this.filteredTours = tours; // Now shows all tours by default
    });
  }

  ngOnDestroy() {
    this.toursSubscription?.unsubscribe();
  }


  onSearch(): void {
    // if every field is empty just show the cached list — no point hitting the backend
    const allEmpty =
      !this.searchStart?.trim() &&
      !this.searchEnd?.trim() &&
      !this.searchTransport?.trim() &&
      !this.searchQuery?.trim();

    if (allEmpty) {
      this.filteredTours = this.allTours;
      return;
    }

    // at least one filter is set -> backend does the matching
    this.tourService.searchTours(
      this.searchStart,
      this.searchEnd,
      this.searchTransport,
      this.searchQuery
    ).subscribe({
      next: results => this.filteredTours = results,
      error: () => this.toastService.show('Search failed.', ToastType.Danger)
    });
  }

  /**
   * Log Modal logic
   */
  openLogModal(tour: Tour): void {
    this.selectedTour = tour;
    this.newLog = {
      date: new Date().toISOString().split('T')[0],
      comment: '',
      duration: 0,
      difficultyRating: '',
      image: '',
      // pre-fill with the planned tour distance — user can adjust if they took a detour
      totalDistance: tour.distance
    };
    this.isLogModalOpen = true;
  }

  saveLog(): void {
    if (!this.selectedTour) {
      return;
    }

    // Validate required fields
    if (!this.newLog.date || this.newLog.duration < 1 || !this.newLog.difficultyRating) {
      this.toastService.show(
        'Please fill in all required fields (Date, Duration min 1, and Difficulty Rating)',
        ToastType.Danger
      );
      return;
    }

    // Now hits the backend instead of just storing in memory.
    // The service handles updating the cache so other pages (like profile) see the new log.
    this.tourLogService.addTourLog(this.selectedTour, this.newLog).subscribe({
      next: () => {
        this.toastService.show(
          `Completion log added for "${this.selectedTour!.name}"!`,
          ToastType.Success
        );
        this.closeLogModal();
      },
      error: () => this.toastService.show('Failed to save tour log.', ToastType.Danger)
    });
  }

  closeLogModal(): void {
    this.isLogModalOpen = false;
    this.selectedTour = null;
  }

  getCurrentDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  async ngAfterViewInit() {
    if (typeof window === 'undefined') return;

    const L = await import('leaflet');

    if (this.map) return;

    // Wait for the DOM to be fully ready
    setTimeout(() => {
      const mapElement = document.getElementById('map');
      if (!mapElement) {
        console.error('Map container not found');
        return;
      }

      this.map = L.map('map', {
        zoomControl: true,
        attributionControl: true,
      });

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
      }).addTo(this.map);

      this.map.setView([48.2082, 16.3738], 12); // Vienna
    }, 0);
  }
  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    const reader = new FileReader();

    reader.onload = () => {
      this.newLog.image = reader.result as string;
    };

    reader.readAsDataURL(file);
  }

  // Export tours
  exportTours(): void {

    this.tourService.exportTours();
  }
}
