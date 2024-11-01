import { Injectable, inject } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { ModuleWindowService } from './module-window.service';

interface LoadingState {
  componentId: string;
  isLoading: boolean;
}

@Injectable({
  providedIn: 'root',
})

export class SseService {

  moduleWindowService = inject(ModuleWindowService);

  private isLoadingSubject = new BehaviorSubject<LoadingState>({ componentId: '', isLoading: false });

  isLoading$ = this.isLoadingSubject.asObservable();


  private progressSubject = new BehaviorSubject<string>("0");

  isProgress$ = this.progressSubject.asObservable();

  constructor() { }

  updateIsLoading(id: string, value: boolean) {
    console.log("updated is Loading!" + id + value)
    this.isLoadingSubject.next({ componentId: id, isLoading: value });
  }

  updateProgress(value: string) {
    this.progressSubject.next(value);
  }


  getSse(userGuid: string): EventSource {
    let url = '/api/sse?userGuid=' + userGuid;
    const eventSource = new EventSource(url);
    eventSource.onmessage = (event) => {
      console.log(event.data);
      this.updateProgress(event.data);
      console.log("got event for:::", userGuid);
    };
    return eventSource;
  }


  getQueueSse(userGuid: string, purpose: string): Observable<void> {
    return new Observable((observer) => {
      let url = '/api/sse/queue?userGuid=' + userGuid + "&purpose=" + purpose;
      const eventSource = new EventSource(url);

      eventSource.onmessage = (event) => {
        console.log("got event:::", event.data);

        if (event.data == "you are next" && purpose != "html") {
          observer.next();
          observer.complete();
          this.unsubscribeFromSse(eventSource);
        }
        if (event.data == "approve") {
          this.updateProgress("");
          this.moduleWindowService.updateOpenModal(true);
          this.unsubscribeFromSse(eventSource);
        }
        if (event.data == "you are next") {
          observer.next();
          observer.complete(); 
        } 
         else {
          this.updateProgress(event.data);
        }
      };
      //return () => this.unsubscribeFromSse(eventSource);
    });
  }


  unsubscribeFromSse(eventSource: EventSource | null) {
    if (eventSource) {
      console.log("Unsubscribing from SSE");
      eventSource.close();
    }
  }
}