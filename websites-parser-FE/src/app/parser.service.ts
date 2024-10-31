import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { lastValueFrom, BehaviorSubject } from 'rxjs';
import { SseService } from './sse.service';
import { ModuleWindowService } from './module-window.service';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';

  serviceName = 'parser';

  moduleWindowService = inject(ModuleWindowService);

  sseService = inject(SseService);

  sendHtmlUrl = '/api/send-html';

  private lastPageUrl = '/api/last-page';

  private approveUrl = '/api/approve'

  private noneCachedUrl = '/api/none-cached-page'

  private infiniteScrollingUrl = '/api/infinite-scroll'

  private cleanPageExtUrl = '/api/html-page-cleanup'


  constructor(private http: HttpClient) {
  }


  geNotCachedWebPage(webUrl: string | null, userGuid: string): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    const data = lastValueFrom(this.http.post<string>(this.noneCachedUrl + "?userGuid=" + userGuid, webUrl, httpOptions));
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
    this.moduleWindowService.updateOpenModal(false);
    return lastValueFrom(this.http.post<string>(this.sendHtmlUrl, webUrl, httpOptions));
  }

  approved(userGuid: string): void {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    this.http.get<string>(this.approveUrl + "?userGuid=" + userGuid, httpOptions).subscribe({
      next: (data: string) => {
        console.log(data);
      },
      error: (error) => {
        alert(error);
        console.error('There was an error!', error);
      },
    });
  }

  getInfiniteScrolling(webUrl: string, scrollingSpeed: string, userGuid: string): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    console.log("this is what we've got: ", webUrl);
    this.moduleWindowService.updateOpenModal(false);
    this.sseService.updateIsLoading(this.serviceName, true);
    const data = lastValueFrom(this.http.post<string>(this.infiniteScrollingUrl + "?speed=" + scrollingSpeed + "&userGuid=" + userGuid, webUrl, httpOptions));
    return data;
  }

  retrieveAllPages(paginationInfo: { sendLastPageUrl: string, paginationTag: string, pageStart: string, pageFinish: string }, userGuid: string): string[] {

    let allPagesHtml: string[] = [];
    this.sseService.updateIsLoading(this.serviceName, true);
    this.http.post<string[]>(this.lastPageUrl + "?pageTag=" + paginationInfo.paginationTag + "&pageStart=" + paginationInfo.pageStart + "&pageFinish=" + paginationInfo.pageFinish + "&userGuid=" + userGuid, paginationInfo.sendLastPageUrl).subscribe({
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
    const data = lastValueFrom(this.http.post<any>(this.cleanPageExtUrl, website, httpOptions));
    return data;

  }

}
