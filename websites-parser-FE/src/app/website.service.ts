import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Website } from './models/website.model';
import { ParserService } from './parser.service';
import { PaginationService } from './pagination.service';
import { ListService } from './list.service';
import { Guid } from 'guid-typescript';

@Injectable({
    providedIn: 'root',
})
export class WebsiteService {
    private websiteSubject = new BehaviorSubject<Website>(new Website());
    public website$: Observable<Website> = this.websiteSubject.asObservable();

    private parserService = inject(ParserService);
    private paginationService = inject(PaginationService);
    private listService = inject(ListService);

    setAllPagesHtml(paginationInfo: { sendLastPageUrl: string, paginationTag: string, pageStart: string, pageFinish: string }, userGuid: string): void {
        let htmls = this.parserService.retrieveAllPages(paginationInfo, userGuid);
        const currentWebsite = this.websiteSubject.value;
        const updatedWebsite = { ...currentWebsite, allPagesHtml: htmls };
        this.websiteSubject.next(updatedWebsite);
    }

    setColumIndex(columIndex: number): void {
        const currentWebsite = this.websiteSubject.value;
        const updatedWebsite = { ...currentWebsite, columIndex };
        this.websiteSubject.next(updatedWebsite);
    }

    setElementsOnMainPage(key: string, value: NodeListOf<Element>): void {
        const currentWebsite = this.websiteSubject.value;
        const updatedElements = new Map(currentWebsite.elementsOnMainPage).set(key, value);
        const updatedWebsite = { ...currentWebsite, elementsOnMainPage: updatedElements };
        this.websiteSubject.next(updatedWebsite);
    }

    setInformationToSend(target: string | HTMLElement, key: string): void {
        const currentWebsite = this.websiteSubject.value;
        let value: string[] = [];

        if (typeof target === 'string') {
            value = this.paginationService.getFromAllPagesInfoDevMode(target, currentWebsite.allPagesHtml);
        }
        else if (target instanceof HTMLElement) {
            value = this.paginationService.getFromAllPagesInfoTargetFlow(target, currentWebsite.allPagesHtml);
        }

        let mainElements = this.paginationService.getElementsOnMainPage;

        const updatedInformationToSend = new Map(currentWebsite.informationToSend).set(key, value);
        const updatedElementsOnMainPage = new Map(currentWebsite.elementsOnMainPage.set(key, mainElements));
        const updatedIndex = currentWebsite.columIndex++;

        const updatedWebsite = {
            ...currentWebsite,
            informationToSend: updatedInformationToSend,
            columnIndex: updatedIndex,
            elementsOnMainPage: updatedElementsOnMainPage
        };

        let listItems = Array.from(updatedInformationToSend).map(([key, values]) => ({ key, values }));
        this.listService.updateList(listItems);

        this.websiteSubject.next(updatedWebsite);
    }


    deleteInformationItem(key: string): void {
        const currentWebsite = this.websiteSubject.getValue();
        currentWebsite.informationToSend.delete(key);
        currentWebsite.elementsOnMainPage.delete(key);
        this.websiteSubject.next(currentWebsite);
    }

    initWebsite(): void {
        const newWebsite = new Website();
        newWebsite.userGuid = Guid.create().toString();
        console.log("init website::: with guid::: "+newWebsite.userGuid )
        this.websiteSubject.next(newWebsite);
    }

    checkIfCachedWebsiteAndReturn() {

    }

}