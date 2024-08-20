import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';
  private sendHtmlUrl = 'http://localhost:8080/send-html';

  // private sendPaginationTagUrl = 'http://localhost:8080/pagination-tag';


  private lastPage = 'http://localhost:8080/last-page';

  constructor(private http: HttpClient) { }


  fetchHtmlFromUrl(webUrl: string): Observable<string> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain', // Accept plain text responses
        'Content-Type': 'text/plain' // Set content type
      }),
      responseType: 'text' as 'json' // Specify response type as text
    };
    const headers = new HttpHeaders({ 'Content-Type': 'text/plain', "Accept": "text/plain" });
    return this.http.post<string>(this.sendHtmlUrl, webUrl, httpOptions);
  }

  sendPaginationTag(paginationTagString: string | null): Observable<string[]> {
    console.log(paginationTagString)
    return this.http.post<string[]>(this.lastPage, paginationTagString);
  }

  sendInfo(arr: string[]) {
    console.log('info is sent', arr)
  }

  getIt(): string {
    return this.displayHTML;
  }
}
