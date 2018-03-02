import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { environment } from './../../environments/environment';
import { AuthService } from './auth.service';
import { Regional } from '../classes/regional';

@Injectable()
export class BackendUpdateService {
  public is_online = navigator.onLine;
  public backlog: Backlog[] = [];

  private url_endpoint = environment.api_endpoint;
  private tba_api_key = environment.tba_key;
  private tba_cache_track: Object = {};
  // {
  //   endpoint: {
  //     modified: '',
  //     data: [] || {},
  //   }
  // }

  // TODO: All backlog functions need some serious testing both online and specifically offline.
  constructor(private http: HttpClient) {
    window.addEventListener('online', () => {
      this.is_online = true;
      this.publishBacklog();
    });
    window.addEventListener('offline', () => this.is_online = false);
  }

  /**
   *
   * THIS AREA IS FOR THE TBA API
   *
   */

  private craftTBARequest(endpoint: string): Observable<Object> {
    let headers = new HttpHeaders({
      'X-TBA-Auth-Key': this.tba_api_key
    });
    if (this.tba_cache_track[endpoint]) {
      headers = headers.set('If-Modified-Since', this.tba_cache_track[endpoint].modified);
    }
    console.log(this.tba_cache_track);
    return this.http.get('https://www.thebluealliance.com/api/v3/' + endpoint, {headers: headers, observe: 'response'})
                    .flatMap((res: HttpResponse<string[]>) => {
                      console.log(res.status);
                      if (res.status === 304) {
                        console.log('Using cached tba value.');
                        return Observable.of(this.tba_cache_track[endpoint].data);
                      } else if (res.status === 200) {
                        this.tba_cache_track[endpoint] = {
                          modified: res.headers.get('last-modified'),
                          data: res.body
                        };
                        return Observable.of(res.body);
                      } else {
                        return Observable.of({});
                      }
                    });
  }

  public getRegionalData(regional_id: string): Observable<Regional> {
    // TODO: Make request to backend to get data for specific regional.
    const reg: Regional = {
      id: 'RegData',
      name: 'Regional Data',
      data: {
        teams: [
          { name: 'Team 1', number: 1 },
          { name: 'Team 2', number: 2 },
          { name: 'Team 3', number: 3 },
          { name: 'Team 4', number: 4 },
          { name: 'Team 5', number: 5 },
          { name: 'Team 6', number: 6 },
        ],
        matches: [
          {
            match_id: 'q1',
            red_teams: [
              { name: 'Team 1', number: 1 },
              { name: 'Team 2', number: 2 },
              { name: 'Team 3', number: 3 }
            ],
            blue_teams: [
              { name: 'Team 4', number: 4 },
              { name: 'Team 5', number: 5 },
              { name: 'Team 6', number: 6 }
            ],
            red_score: 100,
            blue_score: 10
          },
          {
            match_id: 'q2',
            red_teams: [
              { name: 'Team 1', number: 1 },
              { name: 'Team 2', number: 2 },
              { name: 'Team 3', number: 3 }
            ],
            blue_teams: [
              { name: 'Team 4', number: 4 },
              { name: 'Team 5', number: 5 },
              { name: 'Team 6', number: 6 }
            ],
            red_score: 100,
            blue_score: 10
          },
        ]
      }
    };

    return Observable.of(reg);
  }

  public getRegionalList(): Observable<Regional[]> {
    // TODO: Make request to backend to get all regional data.
    const regional_list = [
      { name: 'Regional 1', id: 'reg1' },
      { name: 'Regional 2', id: 'reg2' }
    ];
    // this.craftTBARequest('events/' + environment.year + '/keys').subscribe((res) => {
    this.craftTBARequest('events/' + environment.year + '/keys').subscribe((res) => {
      console.log(res);
    }, (err) => {
      console.log('ERROR');
      console.log(err);
    });
    return Observable.of(regional_list).delay(1000);
  }






  /**
   *
   * THIS AREA IS FOR THE BACKEND API
   *
   */

  private craftBackendRequest(endpoint: string, data: any): Observable<HttpResponse<Object>> {
    const headers = new HttpHeaders({});
    return this.http.post(this.url_endpoint + endpoint, headers, {observe: 'response'});
  }

  public submitPitScout(data: any): void {
    if (this.is_online) {
      // TODO: Finish implementation of backend.
      this.craftBackendRequest('pitscout', data).subscribe((resp) => {});
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
