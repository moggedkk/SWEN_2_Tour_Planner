import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar'
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-main',
  imports: [Navbar, RouterOutlet],
  standalone: true,
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main {

}
