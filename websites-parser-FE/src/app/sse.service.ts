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


  private progressSubject = new BehaviorSubject<string>("0");

  isProgress$ = this.progressSubject.asObservable();

  constructor() { }

  updateIsLoading(id: string, value: boolean) {
    this.isLoadingSubject.next({ componentId: id, isLoading: value });
  }

  updateProgress(value: string) {
    this.progressSubject.next(value);
  }


  getSse(userGuid:string) {
    let url = '/api/sse?userGuid='+userGuid;
    const eventSource = new EventSource(url);
    eventSource.addEventListener("test", (event) => {
      console.log(event.data);
      this.updateProgress(event.data);
      console.log("got event for:::", userGuid)
    });

    eventSource.addEventListener("heartbeat", (event) => {
    });
  }
}