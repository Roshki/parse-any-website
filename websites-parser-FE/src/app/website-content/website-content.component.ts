import { CommonModule } from '@angular/common';
import { Component, OnChanges, SimpleChanges, Input, Renderer2, ViewEncapsulation, Output, EventEmitter, ChangeDetectorRef, inject } from '@angular/core';
import { SafeHtml, DomSanitizer } from '@angular/platform-browser';
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
  styleUrl: '/src/styles.css'
})
export class WebsiteContentComponent {

  private website: Website | null = null;
  private sseService = inject(SseService);

  @Input() displayT: SafeHtml | undefined;
  @Input() display: SafeHtml | undefined;
  @Input() ifPaginationMode: boolean = false;
  @Output() ifPaginationModeChanged = new EventEmitter<boolean>();
  @Output() progressUpdated = new EventEmitter();


  constructor(private renderer: Renderer2, private websiteService: WebsiteService, private sanitizer: DomSanitizer, private cd: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['display']) {
      // Do something when display changes
      console.log('Display changed:');
      let docrRoot = document.querySelector("app-website-content") as HTMLElement;
    const children = docrRoot.shadowRoot;
     // this.copyStylesAndHead(children)

    }
  }

  copyStylesAndHead(shadow: ShadowRoot| null) {
    // Copy styles from the main document
    let docrRoot = document.querySelector("app-website-content") as HTMLElement;
    const styles = docrRoot.querySelectorAll('style, link[rel="stylesheet"]');

    styles.forEach(style => {
      // Clone the styles or links and append them to the shadow DOM
      const clonedNode = style.cloneNode(true);
      shadow?.appendChild(clonedNode);
    });

    // If you want to copy specific <script> tags, you can do it like this:
    const scripts = docrRoot.querySelectorAll('script');
    scripts.forEach(script => {
      if (script.src) {
        // If the script has a src, create a new script element
        const newScript = document.createElement('script');
        newScript.src = script.src;
        newScript.async = script.async; // Preserve async attribute
        shadow?.appendChild(newScript);
      } else {
        // If the script is inline, clone it as is
        const inlineScript = document.createElement('script');
        inlineScript.textContent = script.textContent;
        shadow?.appendChild(inlineScript);
      }
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
        this.progressUpdated.emit(this.sseService.getSse());
      }
}