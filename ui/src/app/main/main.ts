import { Component } from '@angular/core';
import { Navbar } from '../navbar/navbar'
import { RouterOutlet } from "../../../node_modules/@angular/router/types/_router_module-chunk";

@Component({
  selector: 'app-main',
  imports: [Navbar, RouterOutlet],
  standalone: true,
  templateUrl: './main.html',
  styleUrl: './main.css',
})
export class Main {

}
