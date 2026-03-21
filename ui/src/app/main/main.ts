import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar';
import { AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Tour } from '../models/Tour';

@Component({
  selector: 'app-main',
  imports: [Navbar, CommonModule],
  standalone: true,
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main implements AfterViewInit {
  createdTours: Tour[] = Tour.GetTours();

  private map: any = null;

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
