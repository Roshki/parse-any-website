import { Component, Input, inject, Renderer2, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { ListService } from '../list.service';
import { WebsiteContentComponent } from '../website-content/website-content.component';
import { WebsiteService } from '../website.service';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule, WebsiteContentComponent],
  templateUrl: './list.html',
  styleUrl: './list.css'
})
export class ListComponent {
  private listService = inject(ListService);
  @Input() display: SafeHtml | undefined;
  listItems: { key: string, values: string[] }[] = [];
  private website: Website | null = null;

  constructor(private renderer: Renderer2, private websiteService: WebsiteService) {
  }

  isOpen: { [key: number]: boolean } = {}; 
  accordionOpenState: { [key: number]: boolean } = {};


  toggleDropdown(index: number) {
    this.isOpen[index] = !this.isOpen[index];
  }
  toggleAccordion(index: number) {
    this.accordionOpenState[index] = !this.accordionOpenState[index];
  }

  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown')) {
      this.isOpen = {}; 
    }
  }


  ngOnInit(): void {
    this.listService.list$.subscribe(value => {
      this.listItems = value;
      console.log('added new list items: ', this.listItems);
    });

    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });
  };

  
  removeItemsGroup(itemId: number) {
    if (this.website) {
      const itemKey = this.listItems[itemId]?.key;
      this.listItems.splice(itemId, 1);
      if (itemKey) {
        console.log(this.listItems);
        this.deselectElements(itemKey);
        this.websiteService.deleteInformationItem(itemKey);
      }
    }
  }

  private deselectElements(key: string): void {
    const elementsOnMainPage = this.website?.elementsOnMainPage;
    if (elementsOnMainPage?.has(key)) {
      const elements = elementsOnMainPage.get(key);
      elements?.forEach(element => {
        this.renderer.removeStyle(element, 'color');
      });
    }
  }
}
