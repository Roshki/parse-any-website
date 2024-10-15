import { CommonModule } from '@angular/common';
import { Component, SimpleChanges, Input, Renderer2, ViewEncapsulation, Output, EventEmitter, ElementRef} from '@angular/core';
import { SafeHtml } from '@angular/platform-browser';
import { Website } from '../models/website.model';
import { ListComponent } from '../list/list.component';
import { WebsiteService } from '../website.service';

@Component({
  selector: 'app-website-content',
  standalone: true,
  imports: [CommonModule, ListComponent],
  templateUrl: './website-content.html',
  encapsulation: ViewEncapsulation.ShadowDom
})
export class WebsiteContentComponent {

  private website: Website | null = null;

  @Input() display: SafeHtml | undefined;
  @Output() progressUpdated = new EventEmitter();
  shadowRoot: any;


  constructor(private renderer: Renderer2, private websiteService: WebsiteService, private el: ElementRef) {
  }

  ngOnInit(): void {

    //  this.shadowRoot = this.el.nativeElement.attachShadow({mode:'open'});


    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['display']) {
      console.log('Display changed:');
      // let docrRoot = document.querySelector("app-website-content") as HTMLElement;
      // const children = docrRoot.shadowRoot;
      // this.copyStylesAndHead(children)

    }
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

  private highlightElements(key: string): void {
    const elementsOnMainPage = this.website?.elementsOnMainPage;
    if (elementsOnMainPage?.has(key)) {
      const elements = elementsOnMainPage.get(key);
      elements?.forEach(element => {
        this.renderer.setStyle(element, 'color', 'red');
      });
    }
  }
}