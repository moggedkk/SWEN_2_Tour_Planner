import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export enum ToastType {
  Success = 'success',
  Danger = 'danger',
  Info = 'info',
  Warning = 'warning'
}

export interface Toast {
  message: string;
  type: ToastType;
  id: number;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();
  private nextId = 0;

  show(message: string, type: ToastType = ToastType.Success): void {
    const id = this.nextId++;
    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next([...currentToasts, { message, type, id }]);

    // Auto-remove after 3 seconds
    setTimeout(() => this.remove(id), 3000);
  }

  remove(id: number): void {
    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next(currentToasts.filter((t) => t.id !== id));
  }
}
