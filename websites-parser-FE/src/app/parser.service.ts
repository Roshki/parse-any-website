import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DomSanitizer } from '@angular/platform-browser';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';
  private parserServiceUrl = environment.parserServiceUrl;

  sendHtmlUrl = this.parserServiceUrl + 'send-html';

  private lastPage = this.parserServiceUrl + 'last-page';

  private getInfoUrl = this.parserServiceUrl + 'get-info-url'



  constructor(private http: HttpClient, private sanitizer: DomSanitizer) {
  }


  fetchHtmlFromUrl(webUrl: string): Observable<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain', 
        'Content-Type': 'text/plain' 
      }),
      responseType: 'text' as 'json' 
    };
    const headers = new HttpHeaders({ 'Content-Type': 'text/plain', "Accept": "text/plain" });
    return this.http.post<string>(this.sendHtmlUrl, webUrl, httpOptions);
  }

  retrieveAllPages(paginationHref: string | null): string[] {
    let allPagesHtml: string[] = [];
    //console.log(paginationTagString)
    this.http.post<string[]>(this.lastPage, paginationHref).subscribe({
      next: (data: string[]) => {
        console.log(data)
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
      // params: new HttpParams().set('param1', 'value1')

    };
    alert(map);
    console.log("sending this: ", map);
    this.http.post<string>(this.getInfoUrl, Object.fromEntries(map), httpOptions).subscribe({
      next: (data: string) => {
        //this.chooseFolder(data);
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
