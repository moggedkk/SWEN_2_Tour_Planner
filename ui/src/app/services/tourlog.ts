import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { TourLog } from '../models/Tour';

export interface TourLogEntry extends TourLog {
  id: string; // Unique identifier for each log
  tourName: string; // Reference to the tour this log belongs to
}

@Injectable({
  providedIn: 'root',
})
export class TourLogService {
  // BehaviorSubject acts as a "mini-database" in the frontend.
  // It stores all tour logs and notifies all subscribers when they change.
  private tourLogsSubject = new BehaviorSubject<TourLogEntry[]>([]);

  // Components subscribe to this Observable to get real-time updates.
  tourLogs$: Observable<TourLogEntry[]> = this.tourLogsSubject.asObservable();

  constructor() {}

  /**
   * CREATE: Adds a new tour log.
   * Automatically generates a unique ID for the log.
   */
  addTourLog(tourName: string, log: TourLog): void {
    const currentLogs = this.tourLogsSubject.value;
    const newLog: TourLogEntry = {
      ...log,
      id: this.generateId(),
      tourName
    };
    this.tourLogsSubject.next([...currentLogs, newLog]);
  }

  /**
   * READ: Gets the current list of all tour logs (snapshot).
   */
  getTourLogsSnapshot(): TourLogEntry[] {
    return this.tourLogsSubject.value;
  }

  /**
   * READ: Gets all logs for a specific tour.
   */
  getTourLogsByTourName(tourName: string): TourLogEntry[] {
    return this.tourLogsSubject.value.filter(log => log.tourName === tourName);
  }

  /**
   * READ: Gets all logs grouped by tour name.
   * Returns a Map where key is tour name and value is array of logs.
   */
  getTourLogsGroupedByTour(): Map<string, TourLogEntry[]> {
    const grouped = new Map<string, TourLogEntry[]>();
    const logs = this.tourLogsSubject.value;

    logs.forEach(log => {
      if (!grouped.has(log.tourName)) {
        grouped.set(log.tourName, []);
      }
      grouped.get(log.tourName)!.push(log);
    });

    // Sort logs within each group by date (newest first)
    grouped.forEach(logs => {
      logs.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
    });

    return grouped;
  }

  /**
   * UPDATE: Updates an existing tour log by ID.
   */
  updateTourLog(id: string, updatedLog: Partial<TourLog>): void {
    const currentLogs = this.tourLogsSubject.value;
    const index = currentLogs.findIndex(log => log.id === id);

    if (index !== -1) {
      currentLogs[index] = { ...currentLogs[index], ...updatedLog };
      this.tourLogsSubject.next([...currentLogs]);
    }
  }

  /**
   * DELETE: Removes a tour log by ID.
   */
  deleteTourLog(id: string): void {
    const currentLogs = this.tourLogsSubject.value;
    const filteredLogs = currentLogs.filter(log => log.id !== id);
    this.tourLogsSubject.next(filteredLogs);
  }

  /**
   * DELETE: Removes all logs for a specific tour.
   * Useful when deleting a tour.
   */
  deleteTourLogsByTourName(tourName: string): void {
    const currentLogs = this.tourLogsSubject.value;
    const filteredLogs = currentLogs.filter(log => log.tourName !== tourName);
    this.tourLogsSubject.next(filteredLogs);
  }

  /**
   * Generates a unique ID for tour logs.
   * Uses timestamp + random string for uniqueness.
   */
  private generateId(): string {
    return `log_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }
}
