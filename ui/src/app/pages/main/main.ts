import { Component, OnInit, OnDestroy } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Tour } from '../../models/Tour';
import { TourService } from '../../services/tour';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-main',
  imports: [Navbar, CommonModule],
  standalone: true,
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main implements AfterViewInit, OnInit, OnDestroy {
  createdTours: Tour[] = [];
  private toursSubscription?: Subscription;
  private map: any = null;

  constructor(private tourService: TourService) {}

  ngOnInit() {
    // We subscribe to the service to get the real-time list of tours.
    this.toursSubscription = this.tourService.tours$.subscribe(tours => {
      this.createdTours = tours;
    });
  }

  ngOnDestroy() {
    // Good practice: unsubscribe to prevent memory leaks.
    this.toursSubscription?.unsubscribe();
  }

  async ngAfterViewInit() {
    if (typeof window === 'undefined') return;

    const L = await import('leaflet'); // ✅ dynamic import

    if (this.map) return;

    this.map = L.map('map', {
      zoomControl: true,
      attributionControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
    }).addTo(this.map);

    this.map.setView([48.2082, 16.3738], 12); // Vienna
  }
}
