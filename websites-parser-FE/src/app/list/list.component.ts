import { Component, Input, inject, Renderer2 } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { PaginationService } from '../pagination.service';
import { ListService } from '../list.service';
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
  listService = inject(ListService);
  @Input() display: SafeHtml | undefined;
  listItems: { key: string, values: string[] }[] = [];

  constructor( public website: Website, private renderer: Renderer2) {
  }

  ngOnInit(): void {
    this.listService.list$.subscribe(value => {
      this.listItems = value;
      console.log('added new list items: ', this.listItems);
    });
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

