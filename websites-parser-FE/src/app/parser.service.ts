import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { lastValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';
  private parserServiceUrl = environment.parserServiceUrl;

  sendHtmlUrl = this.parserServiceUrl + 'send-html';

  private lastPage = this.parserServiceUrl + 'last-page';

  private getInfoUrl = this.parserServiceUrl + 'get-info-url'

  private approveUrl = this.parserServiceUrl + 'approve'

  private noneCachedUrl = this.parserServiceUrl + 'none-cached-page'


  constructor(private http: HttpClient, private sanitizer: DomSanitizer) {
  }

  async getHtmlFromUrl(webUrl: string): Promise<string> {
    let getCachedWebIfExists = this.firstCall(webUrl);
    console.log(getCachedWebIfExists)

    if (getCachedWebIfExists == "null") {
      const notCachedPagePromise = await this.notCachedPage(webUrl);

      return notCachedPagePromise;
    }
    else {
      return getCachedWebIfExists;
    }
  }

  async notCachedPage(webUrl: string): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    const data = await lastValueFrom(this.http.post<string>(this.noneCachedUrl, webUrl, httpOptions));
    return data;

  }

  firstCall(webUrl: string): string {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };

    this.http.post<string>(this.sendHtmlUrl, webUrl, httpOptions).subscribe({
      next: (data: string) => {
        console.log(data)
        return data;
      },
      error: (error) => {
        alert(error);
        console.error('There was an error!', error);
      },
    });
    return "null";
  }

  approved(): Promise<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'text/plain'
      }),
      responseType: 'text' as 'json'
    };
    const data = lastValueFrom(this.http.post<string>(this.approveUrl, {}, httpOptions));
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

  sendInfo(map: Map<string, string[]>): void {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain',
        'Content-Type': 'application/json'
      }),
      responseType: 'text' as 'json'

    };
    alert(map);
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

  getIt(): string {
    return this.displayHTML;
  }
}
