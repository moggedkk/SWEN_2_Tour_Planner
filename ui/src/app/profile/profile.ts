import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar';
import { CommonModule } from '@angular/common';

interface Tour {
  name: string;
  start: string;
  end: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [Navbar, CommonModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css'],
})
export class Profile {

  completedTours: Tour[] = [
    { name: 'Vienna City Tour', start: 'Vienna', end: 'Vienna' },
    { name: 'Graz Trip', start: 'Vienna', end: 'Graz' }
  ];

  createdTours: Tour[] = [
    { name: 'Alps Tour', start: 'Salzburg', end: 'Innsbruck' },
    { name: 'Danube Route', start: 'Linz', end: 'Vienna' }
  ];

}