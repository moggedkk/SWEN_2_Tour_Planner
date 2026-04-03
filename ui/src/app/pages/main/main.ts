import { Component, OnInit, OnDestroy, inject, AfterViewInit } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { CommonModule } from '@angular/common';
import { Tour, TransportType, TourLog } from '../../models/Tour';
import { TourService } from '../../services/tour';
import { Subscription } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { ToastService, ToastType } from '../../services/ToastService';

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [Navbar, CommonModule, FormsModule],
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main implements AfterViewInit, OnInit, OnDestroy {
  // Services
  private tourService = inject(TourService);
  private toastService = inject(ToastService);

  // Data
  allTours: Tour[] = [];
  filteredTours: Tour[] = [];
  private toursSubscription?: Subscription;

  // Search Fields (Google Maps style)
  searchStart: string = '';
  searchEnd: string = '';
  searchTransport: string = '';
  transportTypes = Object.values(TransportType);

  // Completion Modal State
  isLogModalOpen = false;
  selectedTour: Tour | null = null;
  newLog: TourLog = {
    date: new Date().toISOString().split('T')[0],
    comment: '',
    duration: 0,
    difficultyRating: 'Moderate'
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
    this.filteredTours = this.allTours;
  }

  /**
   * Log Modal logic
   */
  openLogModal(tour: Tour): void {
    this.selectedTour = tour;
    this.newLog = {
      date: new Date().toISOString().split('T')[0],
      comment: '',
      duration: tour.estimatedTime || 0,
      difficultyRating: tour.difficulty
    };
    this.isLogModalOpen = true;
  }

  saveLog(): void {
    if (!this.selectedTour) {
      return;
    }

    // Add the completion log to the tour
    this.tourService.addTourLog(this.selectedTour.name, this.newLog);

    this.toastService.show(
      `Completion log added for "${this.selectedTour.name}"!`,
      ToastType.Success
    );

    this.closeLogModal();
  }

  closeLogModal(): void {
    this.isLogModalOpen = false;
    this.selectedTour = null;
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
}
