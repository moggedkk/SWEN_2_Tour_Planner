import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { map } from 'rxjs/operators';
import { Tour, TransportType } from '../models/Tour';

// exported so the import page can type the parsed JSON array against the
// same shape the backend expects
export interface TourRequest {
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
  // computed on the backend, just passed through here
  popularity?: string;
  childFriendliness?: string;
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

  // Hits the backend's multi-field search endpoint.
  // start/end/transport filter the tour's own fields, q is a full-text search across
  // tour fields + every tour's logs + computed attributes. All four are AND-combined
  // on the backend. Empty strings = "don't filter on this field".
  // We don't update the toursSubject cache here — the page using this just shows the result.
  searchTours(start: string, end: string, transport: string, query: string): Observable<Tour[]> {
    const params = new HttpParams()
      .set('start', start ?? '')
      .set('end', end ?? '')
      .set('transport', transport ?? '')
      .set('q', query ?? '');
    return this.http.get<TourResponse[]>(`${this.apiUrl}/search`, { params }).pipe(
      map(rs => rs.map(r => this.fromResponse(r)))
    );
  }

  // Bulk-import via the backend. All-or-nothing on the backend side, so if this
  // observable errors out NONE of the tours were created. On success we push the
  // new tours into the cache so the home page / profile pick them up without a
  // full reload.
  importTours(requests: TourRequest[]): Observable<Tour[]> {
    return this.http.post<TourResponse[]>(`${this.apiUrl}/import`, requests).pipe(
      map(rs => rs.map(r => this.fromResponse(r))),
      tap(created => {
        if (created.length > 0) {
          this.toursSubject.next([...this.toursSubject.value, ...created]);
        }
      })
    );
  }

  // Downloads the user's tours as a .json file. The backend hands back the
  // same TourRequest[] shape that /import accepts, so the downloaded file can
  // be re-uploaded on the Import page without any editing.
  // Trick: we receive the JSON, wrap it in a Blob, build a hidden <a> with a
  // download attribute, click it programmatically, then revoke the object URL.
  // This is the standard browser pattern for "save as" — no extra libs needed.
  exportTours(): void {
    this.http.get<unknown[]>(`${this.apiUrl}/export`).subscribe(tours => {
      const blob = new Blob([JSON.stringify(tours, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);

      const today = new Date().toISOString().split('T')[0];
      const a = document.createElement('a');
      a.href = url;
      a.download = `tours-${today}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      // free the blob memory once the click is queued
      URL.revokeObjectURL(url);
    });
  }

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
