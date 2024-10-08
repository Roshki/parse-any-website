import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { lastValueFrom, BehaviorSubject } from 'rxjs';
import { SseService } from './sse.service';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';

  serviceName = 'parser';

  openModalSubject = new BehaviorSubject<boolean>(false);

  openModal$ = this.openModalSubject.asObservable();

  sseService = inject(SseService);

  sendHtmlUrl = '/api/send-html';

  private lastPage = '/api/last-page';

  private approveUrl = '/api/approve'

  private noneCachedUrl = '/api/none-cached-page'

  private infiniteScrollingUrl = '/api/infinite-scroll'

  private cleanPageExtUrl = '/api/html-page-cleanup'


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
    this.sseService.updateIsLoading(this.serviceName, true);
    const data = lastValueFrom(this.http.post<string>(this.infiniteScrollingUrl + "?speed=" + scrollingSpeed, webUrl, httpOptions));
    return data;
  }

  retrieveAllPages(paginationHref: string | null): string[] {

    let allPagesHtml: string[] = [];
    this.sseService.updateIsLoading(this.serviceName, true);
    this.http.post<string[]>(this.lastPage, paginationHref).subscribe({
      next: (data: string[]) => {
        data.forEach(item => allPagesHtml.push(item));
        alert(allPagesHtml.length + " length of all pages");
        this.sseService.updateIsLoading(this.serviceName, false);
      },
      error: (error) => {
        alert(error);
        console.error('There was an error!', error);
        this.sseService.updateIsLoading(this.serviceName, false);
      },
    });
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

}
