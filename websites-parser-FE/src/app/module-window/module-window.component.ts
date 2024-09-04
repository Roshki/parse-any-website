import { Component, inject } from '@angular/core';
import { ParserService } from '../parser.service';

@Component({
  selector: 'app-module-window',
  standalone: true,
  imports: [],
  templateUrl: './module-window.html',
  styles: ``
})
export class ModuleWindowComponent {

  parserService = inject(ParserService);
  isModalWindow: boolean = false;


  approvedOnClick(): void {
    this.parserService.approved();
    this.parserService.openModalSubject.next(false);
  }

  ngOnInit() {
    this.parserService.openModal$.subscribe(value => {
      this.isModalWindow = value;
      console.log('Modal state changed:', this.isModalWindow);
    });
  }

}
