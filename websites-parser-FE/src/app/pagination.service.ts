import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PaginationService {


  // private elementsOnMainPage: Element[] = [];

  constructor() { }


  // public get getElementsOnMainPage(): Element[] {
  //   return this.elementsOnMainPage;
  // }



  getFromAllPagesDevMode(tagId: string, AllPagesHtml: string[]): NodeListOf<Element>[] {
    let elements: NodeListOf<Element>[] = [];
    const parser = new DOMParser();
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;

    if (!docRoot) {
      throw new Error("Shadow root not found");
    }

    if (AllPagesHtml.length > 0) {
      AllPagesHtml.forEach(page => {
        const doc = parser.parseFromString(page, 'text/html');
        let elementsFromPage = this.getElementsFromPage(`[${tagId}]`, undefined, doc);
        elements.push(elementsFromPage);
      }
      );
    }
    else {
      elements.push(this.getElementsFromPage(`[${tagId}]`, undefined, docRoot));
    }
    return elements;
  }

  getFromAllPagesTargetFlow(target: HTMLElement, AllPagesHtml: string[]): NodeListOf<Element>[] {
    let elements: NodeListOf<Element>[] = [];
    let classSelector = target?.className.split(' ').join('.');
    let parentClassSelector = target?.parentElement?.className.split(' ').join('.');
    const parser = new DOMParser();
    let docRoot = document.querySelector("app-website-content")?.shadowRoot;

    if (!docRoot) {
      throw new Error("Shadow root not found");
    }

    if (AllPagesHtml.length > 0) {
      AllPagesHtml.forEach(page => {
        const doc = parser.parseFromString(page, 'text/html');
        let elementsFromPage = this.getElementsFromPage(`.${classSelector}`, `.${parentClassSelector}`, doc);
        elements.push(elementsFromPage);
      }
      );
    }
    else {
      elements.push(this.getElementsFromPage(`.${classSelector}]`, `.${parentClassSelector}`, docRoot));
    }
    return elements;

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
    // this.elementsOnMainPage.length = 0;
    // items.forEach(e => {
    //   this.elementsOnMainPage.push(e);
    // });
    // console.log(this.elementsOnMainPage);
    return items;
  }
}
