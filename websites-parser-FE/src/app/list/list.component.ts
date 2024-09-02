import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import {SafeHtml} from '@angular/platform-browser';
import { Website } from '../models/website.model';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list.html',
  styleUrl: './list.css',
  providers: [Website]
})
export class ListComponent {
 @Input() display: SafeHtml | undefined;

 constructor(private website: Website){
  
 }



}
