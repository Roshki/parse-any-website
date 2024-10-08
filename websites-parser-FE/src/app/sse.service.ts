import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';

interface LoadingState {
  componentId: string;
  isLoading: boolean;
}

@Injectable({
  providedIn: 'root',
})

export class SseService {

  private isLoadingSubject = new BehaviorSubject<LoadingState>({ componentId: '', isLoading: false });

  isLoading$ = this.isLoadingSubject.asObservable();

  constructor() { }

  updateIsLoading(id: string, value: boolean) {
    this.isLoadingSubject.next({ componentId: id, isLoading: value });
  }


  getServerSentEvent(url: string): Observable<any> {
    return new Observable(observer => {
      const eventSource = new EventSource(url);

      eventSource.onmessage = (event) => {
        observer.next(event.data);
      };

      eventSource.onerror = (error) => {
        alert(error);
        this.updateIsLoading("sse", false);
        observer.error(error);
        eventSource.close();
      };
      return () => {
        eventSource.close();
      };
    });
  }
}