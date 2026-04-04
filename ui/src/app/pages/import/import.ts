import { Component } from '@angular/core';
import { Navbar } from '../../components/navbar/navbar';
import { FormsModule } from '@angular/forms'; 
import { Tour } from '../../models/Tour';
import { TourActions } from '../../components/tour-actions/tour-actions';
@Component({
  selector: 'app-import',
  imports: [Navbar, FormsModule, TourActions],
  templateUrl: './import.html',
  styleUrl: './import.css',
})
export class Import {
  jsonText: string = '';
 parsedData: Tour | null = null;
   parseJson() {
    try {
      this.parsedData = JSON.parse(this.jsonText);
      console.log('Parsed JSON:', this.parsedData);
    } catch (e) {
      this.parsedData = null;
      alert('Invalid JSON! Please check your input.');
    }
  }

}
