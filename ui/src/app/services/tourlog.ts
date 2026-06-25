import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, forkJoin, map, tap } from 'rxjs';
import { TourLog, Tour } from '../models/Tour';
import { TourService } from './TourService';

// What the backend SENDS BACK (matches TourLogResponse.java)
interface TourLogResponse {
  id: number;
  tourId: number;
  dateTime: string;      // ISO string e.g. "2024-01-15T10:30:00"
  comment: string;
  difficulty: string;
  totalDistance: number;
  totalTime: number;
  rating: number;
  imageName: string;
  imagePath: string;
}

// What we SEND to the backend (matches TourLogRequest.java)
interface TourLogRequest {
  dateTime: string;
  comment: string;
  difficulty: string;
  totalDistance: number;
  totalTime: number;
  rating: number;
  imageName: string;
  imageEncoded: string;
}

// What the components actually work with — the basic TourLog form fields
// plus the id (so we can update/delete it later) and tourName (for grouping in the profile page).
export interface TourLogEntry extends TourLog {
  id: number;
  tourId: number;
  tourName: string;
  imagePath: string;
}

@Injectable({ providedIn: 'root' })
export class TourLogService {
  // Note: we hit /api/tours/{tourId}/logs — logs are nested under a tour on the backend
  private readonly apiUrl = 'http://localhost:8080/api/tours';
  private readonly http = inject(HttpClient);
  private readonly tourService = inject(TourService);

  // BehaviorSubject = small in-memory cache. Any component that subscribes to
  // tourLogs$ gets the current list and any future updates automatically.
  private tourLogsSubject = new BehaviorSubject<TourLogEntry[]>([]);
  tourLogs$: Observable<TourLogEntry[]> = this.tourLogsSubject.asObservable();

  constructor() {
    // Whenever the list of tours changes (e.g. after login or when a tour gets created/deleted),
    // re-fetch all logs. Otherwise the profile page would have nothing to group.
    this.tourService.tours$.subscribe(tours => this.loadLogsForAllTours(tours));
  }

  // ---- HTTP calls ----

  // Loads logs for every tour in one go.
  // forkJoin waits until ALL the HTTP requests are done before emitting (like Promise.all).
  private loadLogsForAllTours(tours: Tour[]): void {
    if (tours.length === 0) {
      this.tourLogsSubject.next([]);
      return;
    }

    const requests = tours.map(t =>
      this.http.get<TourLogResponse[]>(`${this.apiUrl}/${t.id}/logs`).pipe(
        map(logs => logs.map(l => this.fromResponse(l, t.name)))
      )
    );

    forkJoin(requests).subscribe(all => {
      // .flat() turns [[a,b], [c]] into [a,b,c]
      this.tourLogsSubject.next(all.flat());
    });
  }

  addTourLog(tour: Tour, log: TourLog): Observable<TourLogEntry> {
    console.log("LOG BEFORE REQUEST:", log);
     console.log("REQUEST:", this.toRequest(log));
    return this.http.post<TourLogResponse>(
      `${this.apiUrl}/${tour.id}/logs`,
      this.toRequest(log)
    ).pipe(
      map(r => this.fromResponse(r, tour.name)),
      tap(created => {
        // push it into the cache so the UI updates without a full refetch
        this.tourLogsSubject.next([...this.tourLogsSubject.value, created]);
      })
    );
  }

  updateTourLog(tourId: number, logId: number, log: TourLog, tourName: string): Observable<TourLogEntry> {
    return this.http.put<TourLogResponse>(
      `${this.apiUrl}/${tourId}/logs/${logId}`,
      this.toRequest(log)
    ).pipe(
      map(r => this.fromResponse(r, tourName)),
      tap(updated => {
        // swap the old version with the new one in the cache
        const logs = this.tourLogsSubject.value.map(l => l.id === updated.id ? updated : l);
        this.tourLogsSubject.next(logs);
      })
    );
  }

  deleteTourLog(tourId: number, logId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${tourId}/logs/${logId}`).pipe(
      tap(() => {
        // pull it out of the cache
        this.tourLogsSubject.next(this.tourLogsSubject.value.filter(l => l.id !== logId));
      })
    );
  }

  // ---- helpers used by components ----

  // Groups all logs by their parent tour name.
  // The profile page uses this to render sections like:
  //   "Tour X" -> [log1, log2, log3]
  getTourLogsGroupedByTour(): Map<string, TourLogEntry[]> {
    const grouped = new Map<string, TourLogEntry[]>();

    for (const log of this.tourLogsSubject.value) {
      if (!grouped.has(log.tourName)) {
        grouped.set(log.tourName, []);
      }
      grouped.get(log.tourName)!.push(log);
    }

    // newest first inside each group
    grouped.forEach(logs => logs.sort((a, b) =>
      new Date(b.date).getTime() - new Date(a.date).getTime()
    ));

    return grouped;
  }

getImage(imagePath: string): Observable<string> {
  return this.http.get(
    `http://localhost:8080/api/images/${encodeURIComponent(imagePath)}`,
    { responseType: 'blob' }
  ).pipe(
    map(blob => URL.createObjectURL(blob))
  );
}

  // ---- shape mapping (frontend <-> backend) ----

  // The form fields use friendlier names ("duration", "difficultyRating").
  // The backend uses the spec names ("totalTime", "difficulty"). This converts UI -> backend.
  private toRequest(log: TourLog): TourLogRequest {
    return {
      // Java's LocalDateTime expects something like "2024-01-15T10:30:00".
      // The HTML date input gives only "2024-01-15", so we pad it with "T00:00:00".
      dateTime: log.date.includes('T') ? log.date : `${log.date}T00:00:00`,
      comment: log.comment,
      difficulty: log.difficultyRating,
      // totalDistance is whatever the user left in the form — it's pre-filled with
      // tour.distance in the component, so most users won't touch it
      totalDistance: log.totalDistance ?? 0,
      totalTime: log.duration,
      rating: 0,           // TODO add a star rating input
      imageName: log.imageName,
      imageEncoded: log.imageEncoded
    };
  }

  // Backend -> UI direction. Strips the time part out of dateTime
  // so the date input can show "2024-01-15" (it can't handle full timestamps).
  private fromResponse(r: TourLogResponse, tourName: string): TourLogEntry {
    return {
      id: r.id,
      tourId: r.tourId,
      tourName,
      date: r.dateTime?.split('T')[0] ?? '',
      comment: r.comment,
      difficultyRating: r.difficulty,
      duration: r.totalTime,
      totalDistance: r.totalDistance,
      imagePath: r.imagePath,  // image storage isn't wired up yet — will be a follow-up task
      imageEncoded: '',
      imageName: r.imageName
    };
  }
}
