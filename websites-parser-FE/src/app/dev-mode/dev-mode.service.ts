import { Injectable } from '@angular/core';
import { PaginationService } from '../pagination.service';

@Injectable({
  providedIn: 'root'
})
export class DevModeService {


  constructor(paginationService: PaginationService) { }

}
