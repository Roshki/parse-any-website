import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ParserService {
  displayHTML = 'test';
  private sendHtmlUrl = 'http://localhost:8080/send-html';

  private lastPage = 'http://localhost:8080/last-page';

  private getInfoUrl = 'http://localhost:8080/get-info-url'
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

  sendInfo(map: Map<string, string[]>): void  {
    const httpOptions = {
      headers: new HttpHeaders({
        'Accept': 'text/plain', // Accept plain text responses
        'Content-Type': 'application/json' // Set content type
      }),
      responseType: 'text' as 'json' // Specify response type as text
    };
     this.http.post<string>(this.getInfoUrl, Object.fromEntries(map), httpOptions).subscribe({
      next: (data: string) => {
        console.log("success");
        
      },
      error: (error) => {
        console.error('There was an error!!', error);
      },
    });
  }

  getIt(): string {
    return this.displayHTML;
  }
}
