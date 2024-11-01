import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class ModuleWindowService {

  private openModalSubject = new BehaviorSubject<boolean>(false);

  openModal$ = this.openModalSubject.asObservable();


  public updateOpenModal(openModal: boolean) {
    this.openModalSubject.next(openModal);
  }

  constructor() { }
}
