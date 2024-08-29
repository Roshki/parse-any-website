import { Component, inject, Renderer2, RendererStyleFlags2, HostListener, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
import { DevModeComponent } from './dev-mode/dev-mode.component';
import { ParserService } from './parser.service';
import { TergetedItemService } from './targeted-item.service';
import { PaginationService } from './pagination.service';
import { Website } from './models/website.model';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-parser',
  templateUrl: './app.html',
  styleUrl: "../styles.css",
  imports: [FormsModule, CommonModule, DevModeComponent],
  encapsulation: ViewEncapsulation.Emulated,
  standalone: true,
  providers: [Website]
})
export class ParserComponent {
  parserService = inject(ParserService);
  tergetedItemService = inject(TergetedItemService);
  paginationService = inject(PaginationService);
  display: SafeHtml | undefined;
  sendUrl: string = '';
  sendLastPageUrl: string = '';
  isOpen = true;


  private ifPaginationMode: boolean = false;


  constructor(private sanitizer: DomSanitizer, private renderer: Renderer2, private website: Website, private cd: ChangeDetectorRef) { }


  ngOnInit(): void {
  }


  toggleSlide() {
    this.isOpen = !this.isOpen;
  }

  @HostListener('document:click', ['$event'])
  preventLinkNavigation(event: MouseEvent) {
    const target = event.target as HTMLElement;

    if (target.className != 'item') {
      event.preventDefault();
      event.stopPropagation();

      window.addEventListener('beforeunload', function (event) {
        event.preventDefault();
        console.log('Page navigation prevented via beforeunload');
      });
      window.history.pushState = function () {
        console.log('Navigation using pushState prevented');
      };
      window.history.replaceState = function () {
        console.log('Navigation using replaceState prevented');
      };
      //console.log('Navigation prevented for:', target.getAttribute('href'));
    }
  }

  htmlOnClick(): void {
    this.parserService.fetchHtmlFromUrl(this.sendUrl).subscribe({
      next: (data: string) => {
        this.display = this.sanitizer.bypassSecurityTrustHtml(data);
      },
      error: (error) => {

        console.error('There was an error fetching the HTML file!', error);
      },
    });
  }

  paginationOnClick(event: MouseEvent): void {
    event.preventDefault();
    const target = event.target as HTMLElement;
    let hrefAttr = target.getAttribute("href");
    this.website.setAllPagesHtml(this.parserService.retrieveAllPages(hrefAttr));
  }

  InsertUrlOfLastPageOnClick(): void {
    if (this.sendLastPageUrl != null) {
      this.website.setAllPagesHtml(this.parserService.retrieveAllPages(this.sendLastPageUrl));

    }
  }

  paginationModeOnClick(): void {
    const button = document.querySelector('#paginationBtn');
    const items = document.querySelectorAll('[class*="pagin"]');
    if (this.ifPaginationMode == false) {
      this.ifPaginationMode = true;
      this.renderer.setStyle(button, 'color', 'red');
      items.forEach(element => {
        this.renderer.setStyle(element, 'border', '2px solid gray');
      });
      return;
    }
    else {
      this.ifPaginationMode = false;
      this.renderer.removeStyle(button, 'color');
      items.forEach(element => {
        this.renderer.removeStyle(element, 'border');
      });
      return;
    }
  }


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
  getSimilarElementsAmnt(target: HTMLElement) {
    throw new Error('Method not implemented.');
  }

  onMouseOut(event: MouseEvent) {
    event.preventDefault();
    const target = event.target as HTMLElement;
    const children = document.querySelectorAll('*');

    if (target) {
      children.forEach(child => {

        this.renderer.removeStyle(child, 'background-color');
      });
    }
  }


  elementsOnClick(event: MouseEvent): void {
    event.preventDefault();

    if (this.ifPaginationMode == true) {
      this.paginationOnClick(event);
    }
    else {
      const target = event.target as HTMLElement;
      const arr: string[] = [];
      if (target) {

        const items = this.paginationService.getFromAllPagesTargetFlow(target, this.website.getAllPagesHtml());
        console.log("we have so many pages now ", this.website.getAllPagesHtml().length);
        items.forEach((nodeList, index) => {
          nodeList.forEach((item: Element) => {
            this.renderer.setStyle(item, 'color', 'red', RendererStyleFlags2.Important);
            (item as HTMLElement).style.color = 'red';
            this.renderer.setStyle(item, 'color', 'red', RendererStyleFlags2.Important);
            arr.push(this.tergetedItemService.fetchInfoFromChosenItem(item));
            this.renderer.setStyle(item, 'color', 'red', RendererStyleFlags2.Important);
            this.renderer.setStyle(item, 'color', 'red', RendererStyleFlags2.Important);
          });
        });
        this.cd.detectChanges();
      }


      this.website.getInformation().set(this.website.getColumIndex().toString(), arr);
      let columnIndex = this.website.getColumIndex();
      this.website.setColumIndex(columnIndex + 1);
      console.log("added new ", this.website.getInformation());

    }
  }

  exportBtnOnClick(): void {
    this.parserService.sendInfo(this.website.getInformation());
    console.log("exported:  ", this.website.getInformation());
  }


}

@Component({
  selector: 'app-root',
  template: `<app-parser></app-parser>`,
  standalone: true,
  imports: [ParserComponent],
  encapsulation: ViewEncapsulation.Emulated
})
export class AppComponent { }
