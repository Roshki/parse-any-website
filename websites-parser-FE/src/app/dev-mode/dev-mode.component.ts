import { Component, inject, Output, EventEmitter } from '@angular/core';
import { AppComponent } from '../app.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Website } from '../models/website.model';
import { PaginationService } from '../pagination.service';
import { TergetedItemService } from '../targeted-item.service';
@Component({
  selector: 'app-dev-mode',
  standalone: true,
  imports: [FormsModule, CommonModule, AppComponent],
  templateUrl: './dev-mode.html',
  styles: ``
})
export class DevModeComponent {
  paginationService = inject(PaginationService);
  verifierService = inject(TergetedItemService);
  tagId: string = "";
  listItems: { key: string, values: string[] }[] = [];
  @Output() listItemsChange = new EventEmitter<any[]>();

  constructor(private website: Website) { }


  sendTagIdOnClick() {
    //let items =  this.paginationService.getFromAllPagesDevMode(this.tagId, this.website.getAllPagesHtml());
    let arr: string[] = [];
    arr = this.paginationService.getFromAllPagesInfoDevMode(this.tagId, this.website.getAllPagesHtml());
    let columnIndex = this.website.getColumIndex();
    this.website.setInformation(this.tagId + " " + this.website.getColumIndex().toString(), arr);

    this.listItems = Array.from(this.website.getInformation()).map(([key, values]) => ({ key, values }));
    this.listItemsChange.emit(this.listItems);
    this.website.setColumIndex(columnIndex + 1);
    this.paginationService.getFromAllPagesInfoDevMode(this.tagId, this.website.getAllPagesHtml())
    console.log(this.website.getInformation(), "FROM DEV COMPONENT");

  }


}
