import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class FileExportService {

  constructor() { }

  exportToExcel(data: Map<string, string[]>, fileName: string = 'data.xlsx') {
    const headers = Array.from(data.keys());
    const maxRows = Math.max(...Array.from(data.values()).map(values => values.length));
    const exportData: any[] = [];
    exportData.push(headers);
    for (let i = 0; i < maxRows; i++) {
      const row: string[] = [];

      headers.forEach(key => {
        const values = data.get(key) || [];
        row.push(values[i] || '');
      });
      exportData.push(row);
    }
    const worksheet: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(exportData);
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const dataBlob: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });
    saveAs(dataBlob, fileName);
  }
}
