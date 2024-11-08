import { Component, inject, Renderer2, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Website } from '../models/website.model';
import { ListService } from '../list.service';
import { WebsiteService } from '../website.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './list.html',
  styleUrl: './list.css'
})
export class ListComponent {
  private listService = inject(ListService);

  listItems: { key: string, values: string[] }[] = [];

  private website: Website | null = null;
  regex: string = "";

  constructor(private renderer: Renderer2, private websiteService: WebsiteService) {
  }

  dropdownOpenState: { [key: number]: boolean } = {};
  accordionOpenState: { [key: number]: boolean } = {};


  toggleDropdown(index: number) {
    Object.keys(this.dropdownOpenState).forEach(key => {
      if (Number(key) != index) {
        this.dropdownOpenState[Number(key)] = false;
      }
    });
    this.dropdownOpenState[index] = !this.dropdownOpenState[index];
  }

  toggleAccordion(index: number) {
    Object.keys(this.accordionOpenState).forEach(key => {
      if (Number(key) != index) {
        this.accordionOpenState[Number(key)] = false;
      }
    });

    this.accordionOpenState[index] = !this.accordionOpenState[index];
  }

  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown')) {
      this.dropdownOpenState = {};
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

  sendRegex(itemId: number) {
    if (this.website) {
      const itemKey = this.listItems[itemId]?.key;
      const listItems = this.listItems.find(item => item.key === itemKey);
      if (listItems) {
        console.log("regex????---" + this.regex)
        listItems.values = this.websiteService.updateInformationList(itemKey, listItems.values, this.regex);
        console.log(this.listItems);
        this.listItems.push
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
