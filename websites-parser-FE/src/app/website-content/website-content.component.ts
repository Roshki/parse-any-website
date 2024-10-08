import { CommonModule } from '@angular/common';
import { Component, Input, Renderer2, ViewEncapsulation, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { ListComponent } from '../list/list.component';
import { WebsiteService } from '../website.service';
import { SseService } from '../sse.service';

@Component({
  selector: 'app-website-content',
  standalone: true,
  imports: [CommonModule, ListComponent],
  templateUrl: './website-content.html',
  encapsulation: ViewEncapsulation.ShadowDom,
  styles: ``
})
export class WebsiteContentComponent {

  private website: Website | null = null;
  private sseService = inject(SseService);

  @Input() display: SafeHtml | undefined;
  @Input() ifPaginationMode: boolean = false;
  @Output() ifPaginationModeChanged = new EventEmitter<boolean>();
  @Output() progressUpdated = new EventEmitter();


  constructor(private renderer: Renderer2, private websiteService: WebsiteService, private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });
  }


  onMouseOverHighliteElement(event: MouseEvent) {
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
    if (this.ifPaginationMode == true) {
      this.paginationOnClick(event);
      this.ifPaginationModeChanged.emit(false)
    }
    else {
      if (this.website) {
        const target = event.target as HTMLElement;
        let key = target.className + " " + this.website.columIndex.toString();
        if (target) {
          console.log("we have so many pages now ", this.website.allPagesHtml.length);
          this.websiteService.setInformationToSend(target, key);
          setTimeout(() => {
            this.highlightElements(key);
          }, 100);
          console.log("added new ", this.website.informationToSend);
        }
      }
    }
  }

  paginationOnClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    let hrefAttr = target.getAttribute("href");
    if (hrefAttr != null) {
      this.subscribeToSseEvent();
      this.websiteService.setAllPagesHtml(hrefAttr);
      this.ifPaginationMode = false;
    }
  }

  private highlightElements(key: string): void {
    const elementsOnMainPage = this.website?.elementsOnMainPage;
    if (elementsOnMainPage?.has(key)) {
      const elements = elementsOnMainPage.get(key);
      elements?.forEach(element => {
        this.renderer.setStyle(element, 'color', 'red');
      });
    }
  }

  subscribeToSseEvent() {
    //let url = environment.parserServiceUrl + 'sse';
    let url = '/api/sse';
    this.sseService.getServerSentEvent(url).subscribe(
      (message: string) => {
        console.log(message);
        this.progressUpdated.emit(message);
      },
      (error) => {
        console.error('SSE error: ', error);
      }
    );
  }
}