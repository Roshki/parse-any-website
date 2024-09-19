import { Component, Input, inject, Renderer2 } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { PaginationService } from '../pagination.service';
import { WebsiteContentComponent } from '../website-content/website-content.component';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule, WebsiteContentComponent],
  templateUrl: './list.html',
  styleUrl: './list.css'
})
export class ListComponent {
  paginationService = inject(PaginationService);
  @Input() display: SafeHtml | undefined;
  @Input() listItems: { key: string, values: string[] }[] = [];

  constructor( public website: Website, private renderer: Renderer2) {
  }

  ngOnInit(): void {
  };

  removeItemsGroup(itemId: number) {
    const itemKey = this.listItems[itemId]?.key;
    this.listItems.splice(itemId, 1);
    if (itemKey) {
      console.log(this.listItems);
      this.website.getInformation().delete(itemKey);
      setTimeout(() => {
        this.paginationService.getElementsOnMainPage.forEach(e => {
          this.renderer.removeStyle(e, 'color');
        });
      }, 0);
    }
    this.website.getAllPagesHtml();
  }

}

