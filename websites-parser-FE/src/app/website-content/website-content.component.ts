import { CommonModule } from '@angular/common';
import { Component, Input, inject, RendererStyleFlags2, Renderer2, ViewEncapsulation } from '@angular/core';
import { SafeHtml, DomSanitizer } from '@angular/platform-browser';
import { ParserService } from '../parser.service';
import { PaginationService } from '../pagination.service';
import { TergetedItemService } from '../targeted-item.service';
import { Website } from '../models/website.model';

@Component({
  selector: 'app-website-content',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './website-content.html',
  encapsulation: ViewEncapsulation.ShadowDom,
  styles: ``
})
export class WebsiteContentComponent {

  parserService = inject(ParserService);
  tergetedItemService = inject(TergetedItemService);
  paginationService = inject(PaginationService);

  @Input() display: SafeHtml | undefined;
  public ifPaginationMode: boolean = false;

  constructor(private sanitizer: DomSanitizer, private renderer: Renderer2, private website: Website) { }


  onMouseOverHighliteElement(event: MouseEvent) {
    event.preventDefault();
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

  // paginationModeOnClick(): void {
  //   const button = document.querySelector('#paginationBtn');
  //   const items = document.querySelectorAll('[class*="pagin"]');
  //   if (this.ifPaginationMode == false) {
  //     this.ifPaginationMode = true;
  //     items.forEach(element => {
  //       this.renderer.setStyle(element, 'border', '2px solid gray');
  //     });
  //     return;
  //   }
  //   else {
  //     this.ifPaginationMode = false;
  //     this.renderer.removeStyle(button, 'color');
  //     items.forEach(element => {
  //       this.renderer.removeStyle(element, 'border');
  //     });
  //     return;
  //   }
  // }


  onMouseOut(event: MouseEvent) {
    event.preventDefault();
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
    event.preventDefault();
console.log("checking:: "+ this.website.getAllPagesHtml());
    if (this.ifPaginationMode == true) {
      this.paginationOnClick(event);
    }
    else {
      const target = event.target as HTMLElement;
      const arr: string[] = [];
      if (target) {
        const items = this.paginationService.getFromAllPagesTargetFlow(target, this.website.getAllPagesHtml());
        console.log("we have so many pages now ", this.website.getAllPagesHtml().length);
        items.forEach((nodeList) => {
          nodeList.forEach((item: Element) => {
            arr.push(this.tergetedItemService.fetchInfoFromChosenItem(item));
            this.renderer.setStyle(item, 'color', 'red', RendererStyleFlags2.Important);
          });
        });
      }

      const uniqueSet = new Set(arr);
      this.website.setInformation(this.website.getColumIndex().toString(), Array.from(uniqueSet));
      let columnIndex = this.website.getColumIndex();
      this.website.setColumIndex(columnIndex + 1);
      console.log("added new ", this.website.getInformation());

    }
  }

  paginationOnClick(event: MouseEvent): void {
    event.preventDefault();
    const target = event.target as HTMLElement;
    let hrefAttr = target.getAttribute("href");
    this.website.setAllPagesHtml(this.parserService.retrieveAllPages(hrefAttr));
  }
}