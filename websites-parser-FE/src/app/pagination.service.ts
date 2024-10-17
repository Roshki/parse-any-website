import { Injectable } from '@angular/core';
import { TergetedItemService } from './targeted-item.service';
@Injectable({
  providedIn: 'root'
})
export class PaginationService {


  private elementsOnMainPageN: NodeListOf<Element> | null = null;

  constructor(private targetedService: TergetedItemService) { }


  public get getElementsOnMainPage(): NodeListOf<Element> {
    if (this.elementsOnMainPageN != null) {
      return this.elementsOnMainPageN;
    }
    throw new Error("no elements on main page??");
  }


  getFromAllPagesInfoDevMode(tagId: string, AllPagesHtml: string[]): string[] {
    let InfoArray: string[] = [];

    this.elementsOnMainPageN = this.getElementsFromPage(`[${tagId}]`, undefined, document);

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
      this.elementsOnMainPageN.forEach((item: Element) => {
        InfoArray.push(this.targetedService.fetchInfoFromChosenItem(item));
      });
    }
    return InfoArray;
  }

  getFromAllPagesInfoTargetFlow(target: HTMLElement, AllPagesHtml: string[]): string[] {
    let classSelector = target?.className.split(' ').join('.');
    let parentClassSelector = target?.parentElement?.className.split(' ').join('.');
    let InfoArray: string[] = [];

    this.elementsOnMainPageN = this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, document);

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
      this.elementsOnMainPageN.forEach((item: Element) => {
        if (this.isDisplayed(item)) {
          InfoArray.push(this.targetedService.fetchInfoFromChosenItem(item));
        }
      });
    }

    return InfoArray;

  }

  private getElementsFromPage(selector: string, parentClassSelector: string | undefined, div: Document): NodeListOf<Element> {
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

  isDisplayed(element: Element): boolean {
    const rect = element.getBoundingClientRect();
    return rect.width > 0 && rect.height > 0;
  }

}
