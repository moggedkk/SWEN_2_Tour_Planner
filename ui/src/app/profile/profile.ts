import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar'

@Component({
  selector: 'app-profile',
  imports: [Navbar],
  standalone: true,
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {

}
