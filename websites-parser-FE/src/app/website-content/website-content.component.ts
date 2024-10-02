import { CommonModule } from '@angular/common';
import { Component, Input, inject, Renderer2, ViewEncapsulation, Output, EventEmitter, ChangeDetectorRef } from '@angular/core';
import { SafeHtml } from '@angular/platform-browser';
import { ParserService } from '../parser.service';
import { PaginationService } from '../pagination.service';
import { TergetedItemService } from '../targeted-item.service';
import { ListService } from '../list.service';
import { Website } from '../models/website.model';
import { ListComponent } from '../list/list.component';

@Component({
  selector: 'app-website-content',
  standalone: true,
  imports: [CommonModule, ListComponent],
  templateUrl: './website-content.html',
  encapsulation: ViewEncapsulation.ShadowDom,
  styles: ``
})
export class WebsiteContentComponent {

  parserService = inject(ParserService);
  tergetedItemService = inject(TergetedItemService);
  paginationService = inject(PaginationService);
  listService = inject(ListService);


  @Input() display: SafeHtml | undefined;
  @Input()  ifPaginationMode: boolean = false;
  @Output() ifPaginationModeChanged = new EventEmitter<boolean>();

  constructor(private renderer: Renderer2, private website: Website, private cd: ChangeDetectorRef) {
  }


  onMouseOverHighliteElement(event: MouseEvent) {
    // event.preventDefault();
    const target = event.target as HTMLElement;
    const src = target.getAttribute('src');
    const parentTarget = target?.parentElement?.className;
    if (target || parentTarget) {
      if (target.className.length > 0 || parentTarget) {
        this.renderer.setStyle(target, 'background-color', 'rgba(255, 255, 0, 0.5)');
        ;
      }
    }
  }


  onMouseOut(event: MouseEvent) {
    // event.preventDefault();
    const target = event.target as HTMLElement;
    let docrRoot = document.querySelector("app-website-content") as HTMLElement;
    const children = docrRoot.shadowRoot?.querySelectorAll('*');
    if (target) {
      children?.forEach(child => {

        this.renderer.removeStyle(child, 'background-color');
      });
    }
  }

  elementsOnClick(event: MouseEvent): void {
    // event.preventDefault();
    if (this.ifPaginationMode == true) {
      this.paginationOnClick(event);
      this.ifPaginationModeChanged.emit(false)
    }
    else {
      const target = event.target as HTMLElement;
      let arr: string[] = [];
      if (target) {
        console.log("we have so many pages now ", this.website.getAllPagesHtml().length);
        arr = this.paginationService.getFromAllPagesInfoTargetFlow(target, this.website.getAllPagesHtml());
        setTimeout(() => {
          this.paginationService.getElementsOnMainPage.forEach(e => {
            this.renderer.setStyle(e, 'color', 'red');
          });
        }, 0);
      }
      this.website.setInformation(target.className + " " + this.website.getColumIndex().toString(), arr);
      let listItems = Array.from(this.website.getInformation()).map(([key, values]) => ({ key, values }));
      this.listService.updateList(listItems);
      let columnIndex = this.website.getColumIndex();
      this.website.setColumIndex(columnIndex + 1);
      console.log("added new ", this.website.getInformation());

    }
  }

  paginationOnClick(event: MouseEvent): void {
    // event.preventDefault();
    const target = event.target as HTMLElement;
    let hrefAttr = target.getAttribute("href");
    this.website.setAllPagesHtml(this.parserService.retrieveAllPages(hrefAttr));
    this.ifPaginationMode = false;
  }
}