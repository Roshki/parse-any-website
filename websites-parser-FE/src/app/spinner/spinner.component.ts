import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-spinner',
  standalone: true,
  imports: [],
  templateUrl: "spinner.html",
  styleUrl: "spinner.css"
})
export class SpinnerComponent implements OnInit{

   @Input() progress: string="";

  ngOnInit() {
  
  }

}
