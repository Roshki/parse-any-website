import { Component, inject, Output, EventEmitter, Renderer2 } from '@angular/core';
import { AppComponent } from '../app.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Website } from '../models/website.model';
import { PaginationService } from '../pagination.service';
import { TergetedItemService } from '../targeted-item.service';
import { ListService } from '../list.service';
@Component({
  selector: 'app-dev-mode',
  standalone: true,
  imports: [FormsModule, CommonModule, AppComponent],
  templateUrl: './dev-mode.html',
  styleUrl: '/src/styles.css'
})
export class DevModeComponent {
  paginationService = inject(PaginationService);
  verifierService = inject(TergetedItemService);
  listService = inject(ListService);
  tagId: string = "";

  constructor(private website: Website, private renderer: Renderer2) { }


  sendTagIdOnClick() {
    let arr: string[] = [];
    arr = this.paginationService.getFromAllPagesInfoDevMode(this.tagId, this.website.getAllPagesHtml());
    setTimeout(() => {
      this.paginationService.getElementsOnMainPage.forEach(e => {
        this.renderer.setStyle(e, 'color', 'red');
      });
    }, 0);
    let columnIndex = this.website.getColumIndex();
    this.website.setInformation(this.tagId + " " + this.website.getColumIndex().toString(), arr);

    let listItems = Array.from(this.website.getInformation()).map(([key, values]) => ({ key, values }));
    this.listService.updateList(listItems);
    this.website.setColumIndex(columnIndex + 1);
    console.log(this.website.getInformation(), "FROM DEV COMPONENT");

  }


}
