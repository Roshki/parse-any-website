import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { WebsiteContentComponent } from '../website-content/website-content.component';

@Component({
  selector: 'app-list',
  standalone: true,
  imports: [CommonModule, WebsiteContentComponent],
  templateUrl: './list.html',
  styleUrl: './list.css'
})
export class ListComponent {
  @Input() display: SafeHtml | undefined;
  @Input() listItems: { key: string, values: string[] }[] = [];

  constructor( public website: Website) {
  }

  ngOnInit(): void {
  };

  removeItemsGroup(itemId: number) {
    console.log(this.website.getInformation(), "FROM LIST COMPONENT");
    const itemKey = this.listItems[itemId]?.key;
    this.listItems.splice(itemId, 1);
    if (itemKey) {
      console.log("before: "+ this.website.getInformation().size)
      this.website.getInformation().delete(itemKey);
      console.log("after: "+ this.website.getInformation().size)
    }
    this.website.getAllPagesHtml();
  }

}

