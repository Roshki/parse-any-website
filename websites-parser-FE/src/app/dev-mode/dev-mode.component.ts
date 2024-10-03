import { Component, inject, Renderer2 } from '@angular/core';
import { AppComponent } from '../app.component';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Website } from '../models/website.model';
import { WebsiteService } from '../website.service';
@Component({
  selector: 'app-dev-mode',
  standalone: true,
  imports: [FormsModule, CommonModule, AppComponent],
  templateUrl: './dev-mode.html',
  styleUrl: '/src/styles.css'
})
export class DevModeComponent {
  tagId: string = "";
  website: Website | null = null;

  constructor(private websiteService: WebsiteService, private renderer: Renderer2) {
  }

  ngOnInit(): void {
    this.websiteService.website$.subscribe((website) => {
      this.website = website;
    });
  }

  sendTagIdOnClick() {
    if (this.website && this.tagId) {
      let key = this.tagId + " " + this.website.columIndex.toString();
      this.websiteService.setInformationToSend(this.tagId, key);
      setTimeout(() => {
        this.highlightElements(key);
      }, 100);
      console.log(this.website.informationToSend, "FROM DEV COMPONENT");
    }
  }

  highlightElements(key: string): void {
    const elementsOnMainPage = this.website?.elementsOnMainPage;
    if (elementsOnMainPage?.has(key)) {
      const elements = elementsOnMainPage.get(key);
      elements?.forEach(element => {
        this.renderer.setStyle(element, 'color', 'red');
      });
    }
  }
}
