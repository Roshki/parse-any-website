import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ListService {

  private listSubject = new BehaviorSubject<{ key: string; values: string[] }[]>([]);

  list$ = this.listSubject.asObservable();

  constructor() { }

  public updateList(newItems: { key: string; values: string[] }[]) {
    this.listSubject.next(newItems);
  }

}
