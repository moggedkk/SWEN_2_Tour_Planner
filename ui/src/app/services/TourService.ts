import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { map } from 'rxjs/operators';
import { Tour, TransportType } from '../models/Tour';

interface TourRequest {
  name: string;
  start: string;
  end: string;
  description: string;
  difficulty: string;
  transportType: string;
}

interface TourResponse {
  id: number;
  name: string;
  start: string;
  end: string;
  description: string;
  difficulty: string;
  transportType: string;
  distance: number;
  estimatedTime: number;
  routeGeometry?: any;
}

@Injectable({ providedIn: 'root' })
export class TourService {
  private readonly apiUrl = 'http://localhost:8080/api/tours';
  private readonly http = inject(HttpClient);

  private toursSubject = new BehaviorSubject<Tour[]>([]);
  tours$: Observable<Tour[]> = this.toursSubject.asObservable();

  constructor() {
    this.loadTours();
  }

  loadTours(): void {
    this.http.get<TourResponse[]>(this.apiUrl).subscribe(responses => {
      this.toursSubject.next(responses.map(r => this.fromResponse(r)));
    });
  }

  addTour(tour: Tour): Observable<Tour> {
    const request = this.toRequest(tour);
    return this.http.post<TourResponse>(this.apiUrl, request).pipe(
      map(r => this.fromResponse(r)),
      tap(created => {
        this.toursSubject.next([...this.toursSubject.value, created]);
      })
    );
  }

  updateTour(tour: Tour): Observable<Tour> {
    const request = this.toRequest(tour);
    return this.http.put<TourResponse>(`${this.apiUrl}/${tour.id}`, request).pipe(
      map(r => this.fromResponse(r)),
      tap(updated => {
        const tours = this.toursSubject.value.map(t => t.id === updated.id ? updated : t);
        this.toursSubject.next(tours);
      })
    );
  }

  deleteTour(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        this.toursSubject.next(this.toursSubject.value.filter(t => t.id !== id));
      })
    );
  }

  getToursSnapshot(): Tour[] {
    return this.toursSubject.value;
  }

  exportTours(): void {}

  private fromResponse(r: TourResponse): Tour {
    return { ...r, transportType: r.transportType as TransportType, logs: [], routeInfo: r.routeGeometry };
  }

  private toRequest(tour: Tour): TourRequest {
    return {
      name: tour.name,
      start: tour.start,
      end: tour.end,
      description: tour.description,
      difficulty: tour.difficulty,
      transportType: tour.transportType,
    };
  }
}
