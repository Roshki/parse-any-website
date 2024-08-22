import { Component, inject } from '@angular/core';
import { AppComponent } from '../app.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Website } from '../models/website.model';
import { PaginationService } from '../pagination.service';
import { VerifierService } from '../verifier.service';
@Component({
  selector: 'app-dev-mode',
  standalone: true,
  imports: [FormsModule, CommonModule, AppComponent],
  templateUrl: './dev-mode.html',
  styles: ``
})
export class DevModeComponent {
  paginationService = inject(PaginationService);
  verifierService = inject(VerifierService);
  tagId: string = "";

  constructor(private website: Website) { }


  sendTagIdOnClick() {
    let items =  this.paginationService.devModeFlow(this.tagId, this.website.getAllPagesHtml());
    const arr: string[] = [];
    items.forEach((nodeList, index) => {
      console.log(`Processing NodeList ${index + 1}:`);
      nodeList.forEach((item: Element) => {
        arr.push(this.verifierService.fetchInfoFromChosenItem(item));
        (item as HTMLElement).style.color = 'red';
      });
    });
    this.website.setInformation("test", arr);
    this.paginationService.devModeFlow(this.tagId, this.website.getAllPagesHtml())
    console.log(this.website.getInformation(), "FROM DEV COMPONENT");

  }


}
