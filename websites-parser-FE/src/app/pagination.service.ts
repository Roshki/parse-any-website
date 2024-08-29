import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PaginationService {

  constructor() { }


  getFromAllPagesDevMode(tagId: string, AllPagesHtml: string[]): NodeListOf<Element>[] {
    let elements: NodeListOf<Element>[] = [];
    const parser = new DOMParser();
    if (AllPagesHtml.length > 0) {
      AllPagesHtml.forEach(page => {
         //let tempDiv = document.createElement('div');
         const doc = parser.parseFromString(page, 'text/html');
         // tempDiv.innerHTML = page;
         elements.push(this.getElementsFromPage(`[${tagId}]`, undefined, doc));
      }
      );
      return elements;
    }
    elements.push(this.getElementsFromPage(`[${tagId}]`, undefined, document));
    return elements;

  }

  getFromAllPagesTargetFlow(target: HTMLElement, AllPagesHtml: string[]): NodeListOf<Element>[] {
    let elements: NodeListOf<Element>[] = [];
    let classSelector = target?.className.split(' ').join('.');
    let parentClassSelector = target?.parentElement?.className.split(' ').join('.');
    const parser = new DOMParser();

    elements.push(this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, document));
    if (AllPagesHtml.length > 0) {
      console.log(AllPagesHtml.length)
      AllPagesHtml.forEach(page => {
        //let tempDiv = document.createElement('div');
        const doc = parser.parseFromString(page, 'text/html');
        // tempDiv.innerHTML = page;
        elements.push(this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, doc));
        // tempDiv.remove();
      }
      );
    }
    //  elements.push(this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, document));
    //console.log(elements);
    return elements;
  }

  private getElementsFromPage(selector: string, parentClassSelector: string | undefined, div: Element | Document): NodeListOf<Element> {
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
