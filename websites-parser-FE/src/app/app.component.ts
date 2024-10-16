import { Component, inject, EventEmitter, HostListener, ViewEncapsulation, OnInit, Output, NgZone } from '@angular/core';
import { DevModeComponent } from './dev-mode/dev-mode.component';
import { ParserService } from './parser.service';
import { Website } from './models/website.model';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormsModule, Validators, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { environment } from './../environments/environment';
import { ListComponent } from './list/list.component';
import { WebsiteContentComponent } from './website-content/website-content.component';
import { ModuleWindowComponent } from './module-window/module-window.component';
import { SpinnerComponent } from './spinner/spinner.component';
import { WebsiteService } from './website.service';
import { SseService } from './sse.service';
import { FileExportService } from './file-export.service';
import { InstructionsComponent } from "./instructions/instructions.component";

@Component({
  selector: 'app-parser',
  templateUrl: './app.html',
  styleUrl: "../styles.css",
  imports: [FormsModule, CommonModule, DevModeComponent, ReactiveFormsModule, ListComponent,
    WebsiteContentComponent, ModuleWindowComponent, SpinnerComponent, InstructionsComponent],
  encapsulation: ViewEncapsulation.ShadowDom,
  standalone: true
})
export class ParserComponent implements OnInit {
  private parserService = inject(ParserService);
  sseService = inject(SseService);
  private fileExportService = inject(FileExportService);
  display: SafeHtml | undefined;
  @Output() progress: string = "";
  isLoading: boolean = false;
  isModalWindow: boolean = false;
  scrollingSpeed: string = '';
  sendUrl = new FormControl('', [
    Validators.required,
    Validators.pattern('https?://.+')
  ]);
  isValidUrl: boolean = false;
  private website: Website | null = null;

  paginationInfo = {
    sendLastPageUrl: "",
    paginationTag: "",
    pageStart: "",
    pageFinish: ""
  }

  @Output() displayEmitter = new EventEmitter<SafeHtml>();

  constructor(private sanitizer: DomSanitizer, private ngZone: NgZone, private websiteService: WebsiteService) {
    let docrRoot = document.querySelector("app-parser") as HTMLElement;
    let shadowR = docrRoot.shadowRoot;
    if (shadowR)
      this.copyStylesToShadowDom(shadowR);
  }

  copyStylesToShadowDom(shadowRoot: ShadowRoot) {
    const headStyles = document.querySelectorAll('style, link');

    headStyles.forEach(styleElement => {
      const clonedStyle = styleElement.cloneNode(true) as HTMLElement;
      shadowRoot.appendChild(clonedStyle);
    });
  }

  isDropdownOpen = false;

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  @HostListener('document:click', ['$event'])
  onClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown')) {
      this.isDropdownOpen = false;
    }
  }


  sendDisplay() {
    this.displayEmitter.emit(this.display); // Emit the data
  }

  updateProgress(newProgress: string) {
    this.ngZone.run(() => {
      this.progress = newProgress; // Update the UI-bound variable
    });
  }

  @HostListener('document:click', ['$event'])
  preventLinkNavigation(event: MouseEvent) {
    const target = event.target as HTMLElement;

    if (target.className != 'item' && target.className != 'file-upload') {
      event.preventDefault();
      event.stopPropagation();

      window.addEventListener('beforeunload', function (event) {
        event.preventDefault();
      });
      window.history.pushState = function () {
        console.log('Navigation using pushState prevented');
      };
      window.history.replaceState = function () {
        console.log('Navigation using replaceState prevented');
      };
    }
  }

  ngOnInit() {
    this.sseService.isLoading$.subscribe(value => {
      this.isLoading = value.isLoading;
    });

    this.sseService.isProgress$.subscribe(value => {
      this.ngZone.run(() => {
        this.progress = value; // Update the UI-bound variable
      });
    });

    this.parserService.openModal$.subscribe(value => {
      this.isModalWindow = value;
      console.log('Modal state changed in app:', this.isModalWindow);
    });

    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });

    //listens to events from google extension
    window.addEventListener('message', (event) => {
      if (event.data && event.data.websiteUrl) {
        this.isLoading = true;
        console.log(event.data);
        this.parserService.getCleanPageFromExt(event.data.websiteUrl, event.data.initialHtml).then(cleanPage => {
          this.sendUrl.setValue(event.data.websiteUrl);
          this.isLoading = false;
          this.display = this.sanitizer.bypassSecurityTrustHtml(cleanPage);
          this.sendDisplay()
        })
      }
    });
  }


  htmlOnClick(): void {
    if (this.sendUrl.valid && this.sendUrl.value) {
      this.isLoading = true;
      this.isValidUrl = true;
      this.progress = "";
      this.websiteService.initWebsite();
      this.parserService.tryGetCachedWebPage(this.sendUrl.value)
        .then(cachedPage => {
          if (cachedPage != "") {
            this.isLoading = false;
            this.display = this.sanitizer.bypassSecurityTrustHtml(cachedPage);
            this.sendDisplay();
          }
          else {
            this.parserService.geNotCachedWebPage(this.sendUrl.value)
              .then(nonCachedPage => {
                this.isLoading = false;
                this.display = this.sanitizer.bypassSecurityTrustHtml(nonCachedPage);
                this.sendDisplay();
              })
              .catch(e => {
                this.isLoading = false;
                alert("error happened, please retry!");
              });
          }
        })
        .catch(e => {
          this.isLoading = false;
          alert("error happened, please retry!");
        });
    }
    else {
      this.isValidUrl = false;
    }
  }

  InsertUrlOfLastPageOnClick(): void {
    this.progress = "0";
    this.sseService.getSse();
    this.websiteService.setAllPagesHtml(this.paginationInfo);
  }

  exportBtnOnClick(): void {
    if (this.website) {
      this.fileExportService.exportToExcel(this.website.informationToSend);
      console.log("exported:  ", this.website.informationToSend);
    }
  }

  inifiniteScrollingOnClick(): void {
    this.progress = "0";
    if (this.sendUrl.valid && this.sendUrl.value) {
      this.isLoading = true;
      this.isValidUrl = true;
      this.sseService.getSse();
      this.websiteService.initWebsite();
      this.parserService.getInfiniteScrolling(this.sendUrl.value, this.scrollingSpeed)
        .then(data => {
          this.isLoading = false;
          if (data != "") {
            console.log("this.isModalWindow from infiniteScroll" + this.isModalWindow)
            this.display = this.sanitizer.bypassSecurityTrustHtml(data);
            this.sendDisplay()
            if (this.progress.length > 4) {
              alert(this.progress);
            }
          }
          else {
          }
        }).catch(e => {
          this.isLoading = false;
          alert("error happened, please retry!");
        });
    }
    else {
      this.isValidUrl = false;
    }
  }

}

@Component({
  selector: 'app-root',
  template: `<app-parser (displayEmitter)="receiveData($event)"></app-parser><app-website-content [display]="display"></app-website-content>`,
  standalone: true,
  imports: [ParserComponent, WebsiteContentComponent],
  encapsulation: ViewEncapsulation.Emulated
})
export class AppComponent {

  display: SafeHtml | undefined;

  receiveData(data: SafeHtml) {
    this.display = data;
  }

  constructor() {
    console.log(environment.production + " - " + environment.apiUrl);
  }

}
