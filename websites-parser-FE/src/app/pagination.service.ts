import { Injectable } from '@angular/core';
import { TergetedItemService } from './targeted-item.service';
@Injectable({
  providedIn: 'root'
})
export class PaginationService {

  public elementsOnMainPage: Element[] = [];


  constructor(private targetedService: TergetedItemService) { }




  getFromAllPagesInfoDevMode(tagId: string, AllPagesHtml: string[]): string[] {
    let InfoArray: string[] = [];
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;

    if (!docRoot) {
      throw new Error("Shadow root not found");
    }

    if (AllPagesHtml.length > 0) {
      AllPagesHtml.forEach(page => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(page, 'text/html');
        let elementsFromPage = this.getElementsFromPage(`[${tagId}]`, undefined, doc);
        elementsFromPage.forEach((item: Element) => {
          InfoArray.push(this.targetedService.fetchInfoFromChosenItem(item));
        });
      });
    }
    else {
      let elementsFromPage = this.getElementsFromPage(`[${tagId}]`, undefined, docRoot);
      elementsFromPage.forEach((item: Element) => {
        InfoArray.push(this.targetedService.fetchInfoFromChosenItem(item));
      });
    }
    return InfoArray;
  }

  getFromAllPagesInfoTargetFlow(target: HTMLElement, AllPagesHtml: string[]): string[] {
    let classSelector = target?.className.split(' ').join('.');
    let parentClassSelector = target?.parentElement?.className.split(' ').join('.');
    let InfoArray: string[] = [];
    this.elementsOnMainPage.length=0;
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;

    if (!docRoot) {
      throw new Error("Shadow root not found");
    }
    let elementsFromMainPage = this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, docRoot);
    elementsFromMainPage.forEach((item: Element) => {
      this.elementsOnMainPage.push(item);
    });
    console.log(this.elementsOnMainPage.length)
    if (AllPagesHtml.length > 0) {
      AllPagesHtml.forEach(page => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(page, 'text/html');
        let elementsFromPage = this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, doc);
        elementsFromPage.forEach((item: Element) => {
          InfoArray.push(this.targetedService.fetchInfoFromChosenItem(item));
        });
      });
    }
    else {
      this.elementsOnMainPage.forEach((item: Element) => {
        InfoArray.push(this.targetedService.fetchInfoFromChosenItem(item));
      });
    }
    return InfoArray;

  }



  private getElementsFromPage(selector: string, parentClassSelector: string | undefined, div: Document | ShadowRoot): NodeListOf<Element> {
    let items;
    try {
      items = div.querySelectorAll(selector);
    }
    catch (error) {
      if (parentClassSelector != undefined) {
        items = div.querySelectorAll(parentClassSelector);
      }
      else throw Error;
    }
    return items;
  }
}
