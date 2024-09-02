import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import {SafeHtml} from '@angular/platform-browser';

@Component({
  selector: 'app-website-content',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './website-content.html',
  styles: ``
})
export class WebsiteContentComponent {
  @Input() display: SafeHtml | undefined;

}


