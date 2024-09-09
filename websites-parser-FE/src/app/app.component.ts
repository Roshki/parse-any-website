import { Component, inject, Renderer2, HostListener, ViewEncapsulation, OnInit } from '@angular/core';
import { DevModeComponent } from './dev-mode/dev-mode.component';
import { ParserService } from './parser.service';
import { TergetedItemService } from './targeted-item.service';
import { PaginationService } from './pagination.service';
import { Website } from './models/website.model';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormsModule, Validators, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { environment } from './../environments/environment';
import { ListComponent } from './list/list.component';
import { WebsiteContentComponent } from './website-content/website-content.component';
import { ModuleWindowComponent } from './module-window/module-window.component';


@Component({
  selector: 'app-parser',
  templateUrl: './app.html',
  styleUrl: "../styles.css",
  imports: [FormsModule, CommonModule, DevModeComponent, ReactiveFormsModule, ListComponent, WebsiteContentComponent, ModuleWindowComponent],
  encapsulation: ViewEncapsulation.Emulated,
  standalone: true,
  providers: [Website],
})
export class ParserComponent implements OnInit {
  parserService = inject(ParserService);
  tergetedItemService = inject(TergetedItemService);
  paginationService = inject(PaginationService);
  display: SafeHtml | undefined;
  isModalWindow: boolean = false;
  listItems: { key: string, values: string[] }[] = [];
  // sendUrl: string = ''
  sendLastPageUrl: string = '';
  sendUrl = new FormControl('', [
    Validators.required,
    Validators.pattern('https?://.+')
  ]);
  isValidUrl: boolean = false;
  public ifPaginationMode: boolean = false;

  constructor(private sanitizer: DomSanitizer, private renderer: Renderer2, private website: Website) {

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

  handleListItemsChange(newListItems: any[]) {
    this.listItems = newListItems;
  }
  ngOnInit() {
    this.parserService.openModal$.subscribe(value => {
      this.isModalWindow = value;
      console.log('Modal state changed in app:', this.isModalWindow);
    });
  }


  htmlOnClick(): void {
    if (this.sendUrl.valid && this.sendUrl.value) {
      this.isValidUrl = true;
      this.website.getInformation().clear();
      this.parserService.tryGetCachedWebPage(this.sendUrl.value)
        .then(cachedPage => {
          if (cachedPage != "") {
            this.display = this.sanitizer.bypassSecurityTrustHtml(cachedPage);
          }
          else {
            this.parserService.geNotCachedWebPage(this.sendUrl.value).then(nonCachedPage => {
              this.display = this.sanitizer.bypassSecurityTrustHtml(nonCachedPage);
            })
          }
        });
    }
    else {
      this.isValidUrl = false;
    }
  }

  InsertUrlOfLastPageOnClick(): void {
    if (this.sendLastPageUrl != null) {
      this.website.setAllPagesHtml(this.parserService.retrieveAllPages(this.sendLastPageUrl));
    }
  }

  paginationModeOnClick(): void {
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;
    const button = docRoot?.querySelector('#paginationBtn');
    const items = docRoot?.querySelectorAll('[class*="pagin"]');
    if (this.ifPaginationMode == false) {
      this.ifPaginationMode = true;
      items?.forEach(element => {
        this.renderer.setStyle(element, 'border', '2px solid gray');
      });
      return;
    }
    else {
      this.ifPaginationMode = false;
      this.renderer.removeStyle(button, 'color');
      items?.forEach(element => {
        this.renderer.removeStyle(element, 'border');
      });
      return;
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
export class AppComponent {
  constructor() {
    console.log(environment.production + " - " + environment.apiUrl);
  }

}
