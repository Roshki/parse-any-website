import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PaginationService {

  constructor() { }


  getFromAllPagesDevMode(tagId: string, AllPagesHtml: string[]): NodeListOf<Element>[] {
    let elements: NodeListOf<Element>[] = [];
    const parser = new DOMParser();
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;

    if (docRoot) {
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
      elements.push(this.getElementsFromPage(`[${tagId}]`, undefined, docRoot));
      return elements;
    }
    throw Error;
  }

  getFromAllPagesTargetFlow(target: HTMLElement, AllPagesHtml: string[]): NodeListOf<Element>[] {
    let elements: NodeListOf<Element>[] = [];
    let classSelector = target?.className.split(' ').join('.');
    let parentClassSelector = target?.parentElement?.className.split(' ').join('.');
    const parser = new DOMParser();
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;

    if (docRoot) {
      if (AllPagesHtml.length > 0) {
console.log("allPagesHtml")
        console.log(AllPagesHtml.length)
        AllPagesHtml.forEach(page => {
          const doc = parser.parseFromString(page, 'text/html');
          elements.push(this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, doc));
        }
        );
        return elements;
      }
      elements.push(this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, docRoot));

      return elements;
    }
    throw Error;
  }


  private getElementsFromPage(selector: string, parentClassSelector: string | undefined, div: Document | ShadowRoot): NodeListOf<Element> {
    let items;
    try {
      items = div?.querySelectorAll(selector);
    }
    catch (error) {
      if (parentClassSelector != undefined) {
        items = div?.querySelectorAll(parentClassSelector);
      }
      else throw Error;
    }
    return items;
  }

}
