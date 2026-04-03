import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Tour } from '../models/Tour';

@Injectable({
  providedIn: 'root',
})
export class TourService {
  // BehaviorSubject acts as a "mini-database" in the frontend.
  // It stores the current tours and notifies all subscribers when they change.
  private toursSubject = new BehaviorSubject<Tour[]>(Tour.GetTours());
  
  // Components subscribe to this Observable to get real-time updates.
  tours$: Observable<Tour[]> = this.toursSubject.asObservable();

  constructor() {}

  /**
   * CREATE: Adds a new tour.
   * This is what goes in a service: the logic for how data is updated.
   */
  addTour(tour: Tour): void {
    const currentTours = this.toursSubject.value;
    // We create a new array to trigger Angular's change detection properly.
    this.toursSubject.next([...currentTours, tour]);
  }

  /**
   * READ: Gets the current list of tours (snapshot).
   */
  getToursSnapshot(): Tour[] {
    return this.toursSubject.value;
  }

  /**
   * DELETE: Removes a tour by name.
   */
  deleteTour(tourName: string): void {
    const currentTours = this.toursSubject.value;
    const filteredTours = currentTours.filter(t => t.name !== tourName);
    this.toursSubject.next(filteredTours);
  }

  /**
   * UPDATE: Updates an existing tour.
   */
  updateTour(updatedTour: Tour): void {
    const currentTours = this.toursSubject.value;
    const index = currentTours.findIndex(t => t.name === updatedTour.name);
    if (index !== -1) {
      currentTours[index] = updatedTour;
      this.toursSubject.next([...currentTours]);
    }
  }
}
