import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { environment } from './../../environments/environment';
import { AuthService } from './auth.service';

@Injectable()
export class BackendUpdateService {
  public is_online = navigator.onLine;
  public backlog: Backlog[] = [];

  private url_endpoint = environment.api_endpoint;

  // TODO: All backlog functions need some serious testing both online and specifically offline.
  constructor(private http: HttpClient) {
    window.addEventListener('online', () => {
      this.is_online = true;
      this.publishBacklog();
    });
    window.addEventListener('offline', () => this.is_online = false);
  }

  private getRequest(endpoint: string, data: any): Observable<HttpResponse<Object>> {
    const headers = new HttpHeaders({});
    return this.http.post(this.url_endpoint + endpoint, headers, {observe: 'response'});
  }

  public submitPitScout(data: any): void {
    if (this.is_online) {
      // TODO: Finish implementation of backend.
      this.getRequest('pitscout', data).subscribe((resp) => {});
    } else {
      this.addToBacklog({
        type: 'pitscout',
        data: data
      });
    }
  }

  public submitMatchScout(data: any): void {
    if (this.is_online) {
      // TODO: Add http call
    } else {
      this.addToBacklog({
        type: 'matchscout',
        data: data
      });
    }
  }

  private addToBacklog(item: Backlog): void {
    this.backlog.push(item);
    this.updateLocalStorage();
  }

  private publishBacklog(): void {
    let log: Backlog;
    while (this.is_online && this.backlog.length > 0) {
      log = this.backlog.shift();
      switch (log.type) {
        case 'pitscout':
          this.submitPitScout(log.data);
          break;
        case 'matchscout':
          this.submitMatchScout(log.data);
          break;
        default:
          console.log('Unknown Backlog type');
      }
      this.updateLocalStorage();
    }
  }

  private updateLocalStorage(): void {
    localStorage.setItem('offline-backlog', JSON.stringify(this.backlog));
  }
}

interface Backlog {
    type: string;
    data: any;
}
