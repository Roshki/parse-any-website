import { Component, inject, Renderer2, HostListener } from '@angular/core';
import { ParserService } from './parser.service';
import { VerifierService } from './verifier.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-parser',
  templateUrl: './app.html',
  imports: [FormsModule, CommonModule],
  standalone: true,
  //encapsulation: ViewEncapsulation.None
})
export class ParserComponent {
  parserService = inject(ParserService);
  verifierService = inject(VerifierService);
  display: SafeHtml | undefined;
  sendUrl: string = '';
  sendLastPageUrl: string = '';
  private ifPaginationMode: boolean = false;
  private AllPagesHtml: string[] = [];
  private information: Map<string, string[]> = new Map<string, string[]>();
  private columnOrder: number = 1;


  constructor(private sanitizer: DomSanitizer, private renderer: Renderer2) { }

  ngOnInit(): void {
    //   this.addToIFrame();
  }

  @HostListener('document:click', ['$event'])
  preventLinkNavigation(event: MouseEvent) {
    const target = event.target as HTMLElement;

    if (target.className != 'item') {
      event.preventDefault();
      event.stopPropagation();

      window.addEventListener('beforeunload', function (event) {
        event.preventDefault();  // Block the navigation
        event.returnValue = '';  // Some browsers require this to trigger the confirmation dialog
        console.log('Page navigation prevented via beforeunload');
      });
      window.history.pushState = function () {
        console.log('Navigation using pushState prevented');
      };
      window.history.replaceState = function () {
        console.log('Navigation using replaceState prevented');
      };
      console.log('Navigation prevented for:', target.getAttribute('href'));
    }
  }

  htmlOnClick(): void {
    console.log("testtest", this.sendUrl);
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
    const htmlContent = target.outerHTML;
    console.log(target);
    let hrefAttr = target.getAttribute("href");
    console.log(hrefAttr);
    this.parserService.sendPaginationTag(hrefAttr).subscribe({
      next: (data: string[]) => {
        console.log(data)
        data.forEach(item => this.AllPagesHtml.push(item));
      },
      error: (error) => {

        console.error('There was an error!', error);
      },
    });
  }

  InsertUrlOfLastPageOnClick(): void {
    if (this.sendLastPageUrl != null) {
      this.parserService.sendPaginationTag(this.sendLastPageUrl).subscribe({
        next: (data: string[]) => {
          console.log(data)
          data.forEach(item => this.AllPagesHtml.push(item));
        },
        error: (error) => {

          console.error('There was an error!', error);
        },
      });
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
    if (target) {
      // let similarElNum = this.getAllSimilarToTargetElements(target).length;
      if (target.className.length > 0) {
        this.renderer.setStyle(target, 'background-color', 'yellow');
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
        // Remove background color from each child element
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
        const items = this.getAllSimilarToTargetElements(target);


        items.forEach((nodeList, index) => {
          console.log(`Processing NodeList ${index + 1}:`);
          nodeList.forEach((item: Element) => {
            arr.push(this.verifierService.fetchInfoFromChosenItem(item));
            // arr.push(
            //   `${item.getAttribute('src') != null
            //     ? item.getAttribute('src')?.trim()
            //     : item?.textContent?.trim()
            //   }`
            // );
            (item as HTMLElement).style.color = 'red';
          });
        });
      }

      this.information.set(this.columnOrder.toString(), arr);
      this.columnOrder++;
      this.parserService.sendInfo(this.information);
      console.log(this.information);
    }
  }

  getAllSimilarToTargetElements(target: HTMLElement): NodeListOf<Element>[] {

    const elements: NodeListOf<Element>[] = [];
    let items;
    let classSelector = target.className.split(' ').join('.');

    if (this.AllPagesHtml.length > 0) {
      this.AllPagesHtml.forEach(page => {
        let tempDiv = document.createElement('div');
        tempDiv.innerHTML = page;
        try {
          items = tempDiv.querySelectorAll(`.${classSelector}`);

          elements.push(items);
        }
        catch (error) {
          let classSelector1 = target.parentElement?.className.split(' ').join('.');
          items = tempDiv.querySelectorAll(`.${classSelector1}`);
          elements.push(items);
        }
      }
      );
      return elements;
    }
    try {
      items = document.querySelectorAll(`.${classSelector}`);
      elements.push(items);
    }
    catch (error) {
      const classSelector1 = target.parentElement?.className.split(' ').join('.');
      items = document.querySelectorAll(`.${classSelector1}`);
      elements.push(items);
    }

    return elements;
  }

}

@Component({
  selector: 'app-root',
  template: `<app-parser></app-parser>`,
  standalone: true,
  imports: [ParserComponent],
})
export class AppComponent { }
