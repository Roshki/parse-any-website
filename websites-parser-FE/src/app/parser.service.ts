import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../environments/environment';
import { lastValueFrom, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';
  private parserServiceUrl = environment.parserServiceUrl;

  private openModalSubject = new BehaviorSubject<boolean>(false);

  openModal$ = this.openModalSubject.asObservable();

  sendHtmlUrl = this.parserServiceUrl + 'send-html';

  private lastPage = this.parserServiceUrl + 'last-page';

  private getInfoUrl = this.parserServiceUrl + 'get-info-url'

  private approveUrl = this.parserServiceUrl + 'approve'

  private noneCachedUrl = this.parserServiceUrl + 'none-cached-page'

  private infiniteScrollingUrl = this.parserServiceUrl + 'infinite-scroll'

  private cleanPageExtUrl = this.parserServiceUrl + 'html-page-cleanup'


  constructor(private http: HttpClient) {
  }
  
  public updateOpenModal(openModal: boolean) {
    this.openModalSubject.next(openModal);
  }

  geNotCachedWebPage(webUrl: string | null): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    this.openModalSubject.next(true);
    const data = lastValueFrom(this.http.post<string>(this.noneCachedUrl, webUrl, httpOptions));
    return data;

  }

  tryGetCachedWebPage(webUrl: string): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    this.openModalSubject.next(false);
    return lastValueFrom(this.http.post<string>(this.sendHtmlUrl, webUrl, httpOptions));
  }

  approved(): void {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    this.http.get<string>(this.approveUrl, httpOptions).subscribe({
      next: (data: string) => {
        console.log(data);
      },
      error: (error) => {
        alert(error);
        console.error('There was an error!', error);
      },
    });
  }

  getInfiniteScrolling(webUrl: string, scrollingSpeed: string): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    console.log("this is what we've got: ", webUrl);
    this.openModalSubject.next(false);
    const data = lastValueFrom(this.http.post<string>(this.infiniteScrollingUrl + "?speed=" + scrollingSpeed, webUrl, httpOptions));
    return data;
  }

  retrieveAllPages(paginationHref: string | null): string[] {
    let allPagesHtml: string[] = [];
    this.http.post<string[]>(this.lastPage, paginationHref).subscribe({
      next: (data: string[]) => {
        data.forEach(item => allPagesHtml.push(item));
        alert("done");
        alert(allPagesHtml.length + " length of all pages");
      },
      error: (error) => {
        alert(error);
        console.error('There was an error!', error);
      },
    });;
    return allPagesHtml;
  }

  getCleanPageFromExt(url: string, html: string): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        // 'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    const website = {
      websiteUrl: url,
      initialHtml: html
    }
    this.openModalSubject.next(false);
    const data = lastValueFrom(this.http.post<any>(this.cleanPageExtUrl, website, httpOptions));
    return data;

  }

  sendInfo(map: Map<string, string[]>): void {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'application/json'
      }),
      responseType: 'text' as 'json'

    };
    console.log("sending this: ", map);
    this.http.post<string>(this.getInfoUrl, Object.fromEntries(map), httpOptions).subscribe({
      next: (data: string) => {

        console.log("success");
        alert("file is saved!")

      },
      error: (error) => {
        console.error('There was an error!!', error);
      },
    });
  }

  // async chooseFolder(data: string) {
  //   try {
  //     // The directory picker is available in Chrome and Edge browsers
  //     const handle = await (window as any).showDirectoryPicker();
  //     console.log('Directory picked:', handle);

  //     // Example: Creating a file inside the picked directory
  //     const fileHandle = await handle.getFileHandle('example.xlsx', { create: true });
  //     const writable = await fileHandle.createWritable();
  //     await writable.write(data);
  //     await writable.close();
  //   } catch (error) {
  //     console.error('Folder selection failed:', error);
  //   }
  // }

}
