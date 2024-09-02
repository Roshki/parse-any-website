import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { WebsiteContentComponent } from '../website-content/website-content.component';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule, WebsiteContentComponent],
  templateUrl: './list.html',
  styleUrl: './list.css',
  providers: [Website]
})
export class ListComponent {
  @Input() display: SafeHtml | undefined;
  @Input() listItems: { key: string, values: string[] }[] = [];

  constructor(private website: Website) {
  

  }

  ngOnInit(): void {
  };
}

