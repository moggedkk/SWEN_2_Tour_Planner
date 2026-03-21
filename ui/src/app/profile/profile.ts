import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar';
import { CommonModule } from '@angular/common';
import { Tour } from '../models/Tour';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [Navbar, CommonModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
})
export class Profile {
  username: string;
  completedTours: Tour[] = Tour.GetTours();
  createdTours: Tour[] = [
    new Tour('Vienna City Tour', 'Vienna', 'Vienna'),
    new Tour('Graz Trip', 'Vienna', 'Graz'),
  ];

  constructor() {
    this.username = 'default';
  }
  GetUsername(): string {
    return this.username;
  }
}
