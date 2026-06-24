import { Component, inject } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { FormsModule } from '@angular/forms';
import { TourActions } from '../../components/tour-actions/tour-actions';
import { TourRequest, TourService } from '../../services/TourService';
import { ToastService, ToastType } from '../../services/ToastService';

// fields every tour entry in the JSON must have. matches the backend's
// TourRequest exactly (which is what the manual create form also sends).
const REQUIRED_FIELDS: (keyof TourRequest)[] = [
  'name', 'start', 'end', 'description', 'difficulty', 'transportType'
];

@Component({
  selector: 'app-import',
  imports: [Navbar, FormsModule, TourActions],
  templateUrl: './import.html',
  styleUrl: './import.css',
})
export class Import {
  private tourService = inject(TourService);
  private toastService = inject(ToastService);

  // ---- UI state ----
  jsonText: string = '';
  parsedTours: TourRequest[] | null = null;
  parseError: string | null = null;
  // disables the import button + shows a "working..." hint
  isImporting: boolean = false;
  // gets set after a successful import so we can show a "done" banner instead
  // of the preview table
  lastImportedCount: number | null = null;

  // sample shown to the user inside a <details> block on the page so they don't
  // have to guess the format
  readonly exampleJson = JSON.stringify([
    {
      name: 'Vienna Walk',
      start: 'Vienna',
      end: 'Schönbrunn',
      description: 'Easy stroll through the park',
      difficulty: 'Easy',
      transportType: 'foot-walking'
    }
  ], null, 2);

  // reads the picked .json file as plain text, drops it into the textarea so
  // the user can still tweak it before parsing, then auto-parses
  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      this.jsonText = reader.result as string;
      this.parseJson();
    };
    reader.readAsText(file);
  }

  // turns the JSON string into a typed array of TourRequest. validates that
  // it's actually an array and that each entry has all required fields. on
  // failure: sets parseError, clears parsedTours.
  parseJson(): void {
    this.parseError = null;
    this.parsedTours = null;
    this.lastImportedCount = null;

    if (!this.jsonText.trim()) {
      this.parseError = 'Paste JSON or pick a file first.';
      return;
    }

    let parsed: unknown;
    try {
      parsed = JSON.parse(this.jsonText);
    } catch (e) {
      this.parseError = 'Invalid JSON syntax.';
      return;
    }

    if (!Array.isArray(parsed)) {
      this.parseError = 'JSON must be an array of tour objects, even for a single tour.';
      return;
    }

    if (parsed.length === 0) {
      this.parseError = 'Array is empty — nothing to import.';
      return;
    }

    // check every entry has all the required fields. report the FIRST broken
    // one so the user can fix it and try again.
    for (let i = 0; i < parsed.length; i++) {
      const entry = parsed[i] as Record<string, unknown>;
      const missing = REQUIRED_FIELDS.filter(f => !entry?.[f] || typeof entry[f] !== 'string' || !(entry[f] as string).trim());
      if (missing.length > 0) {
        this.parseError = `Tour #${i + 1} is missing or has empty fields: ${missing.join(', ')}`;
        return;
      }
    }

    this.parsedTours = parsed as TourRequest[];
  }

  // fires the bulk-import HTTP call. all-or-nothing on the backend, so either
  // every tour ends up in the DB or none of them do.
  importParsedTours(): void {
    if (!this.parsedTours || this.parsedTours.length === 0) return;

    this.isImporting = true;
    this.tourService.importTours(this.parsedTours).subscribe({
      next: imported => {
        this.isImporting = false;
        this.lastImportedCount = imported.length;
        this.parsedTours = null;
        this.jsonText = '';
        this.toastService.show(`Imported ${imported.length} tour(s)`, ToastType.Success);
      },
      error: err => {
        this.isImporting = false;
        // backend wraps the failure with which tour broke it ("Import failed
        // at tour #2 ('Bad') ..."). pluck the message out of the HTTP error.
        const message = err?.error?.message || err?.message || 'Import failed';
        this.toastService.show(message, ToastType.Danger);
      }
    });
  }

  // back to a blank slate so the user can import another batch
  reset(): void {
    this.jsonText = '';
    this.parsedTours = null;
    this.parseError = null;
    this.lastImportedCount = null;
  }
}
