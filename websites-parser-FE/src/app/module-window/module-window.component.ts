import { Component, inject, OnInit } from '@angular/core';
import { ParserService } from '../parser.service';
import { Website } from '../models/website.model';
import { WebsiteService } from '../website.service';

@Component({
  selector: 'app-module-window',
  standalone: true,
  imports: [],
  templateUrl: './module-window.html',
  styles: ``
})
export class ModuleWindowComponent implements OnInit {

  parserService = inject(ParserService);
  websiteService = inject(WebsiteService);
  isModalWindow: boolean = false;
  private website: Website | null = null;


  approvedOnClick(): void {
    if (this.website) {
      this.parserService.approved(this.website?.userGuid);
      this.parserService.openModalSubject.next(false);
    }
  }

  ngOnInit() {
    this.parserService.openModal$.subscribe(value => {
      this.isModalWindow = value;
      console.log('Modal state changed:', this.isModalWindow);
    });

    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });
  }

}
