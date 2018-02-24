import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class BackendUpdateService {
  public is_online = true;
  public backlog: Backlog[] = [];

  private url_endpoint = '';

  constructor(private http: HttpClient) { }

  private getRequest(endpoint: string, data: any): Observable<HttpResponse<Object>> {
    const headers = new HttpHeaders({});
    return  this.http.post(this.url_endpoint + endpoint, headers, {observe: 'response'});
  }

  public submitPitScout(data: any): void {
    if (this.is_online) {
      // TODO: Finish implementation of backend.
      this.getRequest('pitscout', data).subscribe((resp) => {});
    } else {
      this.backlog.push({
        type: 'pitscout',
        data: data
      });
    }
  }

  public submitMatchScout(data: any): void {
    if (this.is_online) {
      // TODO: Add http call
    } else {
      this.backlog.push({
        type: 'matchscout',
        data: data
      });
    }
  }

  public publishBacklog(): void {
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
    }
  }
}

interface Backlog {
    type: string;
    data: any;
}
