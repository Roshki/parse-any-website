import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TergetedItemService {

  // the algoithm checks if the chosen element has links or text attached, if yes, it also attaches links to it,
  // if not, it checks first child or parent for the info

  private alreadyContainsLink: boolean = false;


  fetchInfoFromChosenItem(item: Element): string {
    const getSrcAndTextContent = (elem: Element): [string | null, string | null, string | null] => [
      elem.getAttribute('src'),
      elem.textContent,
      elem.getAttribute('href')
    ];

    let itemStr: string | null;
    const parentElement = item.parentElement;
    const childElement = item.children[0];

    let [src, textContent, href] = getSrcAndTextContent(item);
    itemStr = this.verifyElement(src, textContent, href);

    if (itemStr !== null && this.alreadyContainsLink == false) {
      itemStr = this.appendLinksToExistingStr(parentElement, childElement, getSrcAndTextContent(item), itemStr);
    }

    if (itemStr == null && parentElement) {
      [src, textContent] = getSrcAndTextContent(parentElement);
      itemStr = this.verifyElement(src, textContent, href);
    }

    if (itemStr == null && childElement) {
      [src, textContent] = getSrcAndTextContent(childElement);
      this.verifyElement(src, textContent, href);
    }

    return this.getItemOrThrow(itemStr);
  }

  private appendLinksToExistingStr(parentElement: Element | null, childElement: Element, [src, _, href]: any, itemStr: string): string {
    if (parentElement) {
      if (src) {
        itemStr += " " + src; // Append src from parent
      }
      else if (href) {
        itemStr += " " + href;
      }
    }
    else if (childElement) {

      if (src) {
        itemStr += " " + src; // Append src from child
      }
      else if (href) {
        itemStr += " " + href;
      }
    }
    return itemStr;
  }

  private verifyElement(src: string | null, textContent: string | null, href: string | null): string | null {

    if (src !== '' && textContent !== '' && src !== null && textContent !== null && href !== null && href !== null) {
      this.alreadyContainsLink = true;
      return `${src.trim()} ${textContent.trim()}`;
    }
    if (src !== '' && src !== null) {
      this.alreadyContainsLink = true;
      return src.trim();
    }
    if (textContent !== '' && textContent != null) {
      this.alreadyContainsLink = false;
      return textContent.trim();
    }
    if (href !== '' && href != null) {
      this.alreadyContainsLink = true;
      return href.trim();
    }
    return null;
  }


  getItemOrThrow(itemStr: string | null): string {
    if (itemStr != null) {
      return itemStr;
    } else {
      throw new Error('Item is null or undefined');
    }
  }

  constructor() { }
}
