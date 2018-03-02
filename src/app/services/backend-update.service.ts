import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { environment } from './../../environments/environment';
import { AuthService } from './auth.service';
import { Regional, RegionalData } from '../classes/regional';
import { tap, catchError, map } from 'rxjs/operators';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/observable/forkJoin';
import { Team } from '../classes/team';
import { Match } from '../classes/match';

@Injectable()
export class BackendUpdateService {
  public is_online = navigator.onLine;
  public backlog: Backlog[] = [];

  private url_endpoint = environment.api_endpoint;
  private tba_api_key = environment.tba_key;
  private tba_cache_track: Object = {};
  // {
  //   endpoint: {
  //     modified: ,
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

  private makeTBARequest<T>(endpoint: string): Observable<T|Boolean> {
    let headers = new HttpHeaders({
      'X-TBA-Auth-Key': this.tba_api_key
    });
    if (this.tba_cache_track[endpoint]) {
      headers = headers.set('If-Modified-Since', this.tba_cache_track[endpoint].modified);
    }
    // console.log(headers);
    return this.http.get('https://www.thebluealliance.com/api/v3/' + endpoint, {headers: headers, observe: 'response'})
                    .mergeMap((res: HttpResponse<T|Boolean>) => {
                      if (res.status === 200) {
                        this.tba_cache_track[endpoint] = {
                          modified: res.headers.get('last-modified'),
                          data: res.body
                        };
                        return Observable.of(res.body);
                      }
                      console.log('TBA responded with non-200');
                      console.log(res);
                      return Observable.of(false);
                    }).catch((res: HttpErrorResponse) => {
                      if (res.status === 304) {
                        console.log('Using cached tba value.');
                        return Observable.of(this.tba_cache_track[endpoint].data);
                      }
                      if (this.tba_cache_track[endpoint]) {
                        console.log('Error, using local data');
                        return Observable.of(this.tba_cache_track[endpoint].data);
                      }
                      console.log('TBA responded with an error');
                      console.log(res);
                      return Observable.of(false);
                    });
  }

  public getRegionalData(regional_id: string): Observable<RegionalData> {
    // This needs to return an Observable<RegionalData> object that is constructed from 2 http requests:
    //  /event/{event_key}/teams/simple and /event/{event_key}/matches/simple
    return Observable.forkJoin(
      this.makeTBARequest<Team[]>('event/' + regional_id + '/teams/simple'),
      this.makeTBARequest<Match[]>('event/' + regional_id + '/matches/simple')
    ).mergeMap((res: [Team[], Match[]]) => {
      const rd: RegionalData = {
        teams: res[0],
        matches: res[1].sort((a, b) => this.sortReg(a, b))
      };
      return Observable.of(rd);
    });
  }

  public getRegionalList(): Observable<Regional[]|Boolean> {
    return this.makeTBARequest<Regional[]>('events/' + environment.year + '/simple')
                .map((res: Regional[]|Boolean) => {
                  if (res) {
                    res = <Regional[]>res;
                    return res.sort((a, b) => (a.name > b.name) ? 1 : ((b.name > a.name) ? -1 : 0) );
                  }
                  return false;
                });
  }

  private sortReg(a: Match, b: Match): number {
    return a.time > b.time ? 1 : b.time > a.time ? -1 : 0;
    // if (a.comp_level === b.comp_level) { // qm < qf < sf < f
    //   return a.match_number > b.match_number ? 1 : -1;
    // }
    // if (a.comp_level === 'f') { // b will be anything other than f
    //   return 1;
    // } else if (b.comp_level === 'f') {
    //   return -1;
    // }

    // if (a.comp_level === 'sf') {
    //   return 1;
    // } else if (b.comp_level === 'sf') {
    //   return -1;
    // }

    // if (a.comp_level === 'qf') {
    //   return 1;
    // } else if (b.comp_level === 'qf') {
    //   return -1;
    // }
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
