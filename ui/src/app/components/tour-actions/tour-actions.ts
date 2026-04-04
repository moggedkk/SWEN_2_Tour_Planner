import { Component, EventEmitter, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-tour-actions',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './tour-actions.html',
  styleUrl: './tour-actions.css',
})
export class TourActions {}
