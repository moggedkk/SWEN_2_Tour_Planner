import { Component, OnInit, AfterViewInit, inject } from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';
import {Navbar} from '../../components/navbar/navbar';
import {Tour} from '../../models/Tour';
import {TourService} from '../../services/tour';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-tour-details',
  imports: [CommonModule, Navbar],
  templateUrl: './tour-details.html',
  styleUrl: './tour-details.css',
})
export class TourDetails implements OnInit, AfterViewInit{

    private route =inject(ActivatedRoute)
    public router = inject(Router)
    private tourService = inject(TourService);

    private map : any = null;
    tour : Tour | any = null;

    ngOnInit(): void {
      const tourName = this.route.snapshot.paramMap.get('id');
      if(tourName){
        const tourId = Number(decodeURIComponent(tourName));
        const tours = this.tourService.getToursSnapshot();
        this.tour = tours.find(tour => tour.id === tourId);
        if(!this.tour){
          this.router.navigate(['/main']);
        }

      }else{
        this.router.navigate(['/main']);
      }
    }
    async ngAfterViewInit() {
      if (typeof window === 'undefined' || !this.tour) return;

      const L = await import('leaflet');

      // Wait for the DOM to be fully ready
      setTimeout(() => {
        const mapElement = document.getElementById('tour-map');
        if (!mapElement) {
          console.error('Map container not found');
          return;
        }

        this.map = L.map('tour-map', {
          zoomControl: true,
          attributionControl: true,
        });

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap contributors',
        }).addTo(this.map);

        // Center map on Vienna (placeholder)
        this.map.setView([48.2082, 16.3738], 10);
      }, 0);
    }


}
