import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class GoogleExtensionService {

  constructor() {
    window.addEventListener('message', (event) => {
      // Check the origin of the message
      if (event.origin === 'https://your-original-window-domain.com') {
        // Log the message to the console
        console.log('Received message:', event.data);
      }
    });

   }
}
