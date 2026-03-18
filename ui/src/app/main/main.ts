import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar';
import { AfterViewInit } from '@angular/core';

@Component({
  selector: 'app-main',
  imports: [Navbar],
  standalone: true,
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main implements AfterViewInit{
    
 private map: any = null;

  async ngAfterViewInit() {
    if (typeof window === 'undefined') return;

    const L = await import('leaflet'); // ✅ dynamic import

    if (this.map) return;

    this.map = L.map('map', {
      zoomControl: true,
      attributionControl: true,
    });

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "© OpenStreetMap contributors"
    }).addTo(this.map);

    this.map.setView([48.2082, 16.3738], 12); // Vienna
  }

}
