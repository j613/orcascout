import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders, HttpErrorResponse, HttpRequest } from '@angular/common/http';
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
import { UtilsService } from './utils.service';
import { User } from '../classes/user';
import { PitTeam } from '../classes/pit-team';
import { NotificationsService } from './notifications.service';

@Injectable()
export class BackendUpdateService {
  private is_online = navigator.onLine;
  private backlog: BacklogItem[];
  private team_data: PitTeam[];
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
  constructor(private http: HttpClient, private utils: UtilsService, private notif: NotificationsService) {
    window.addEventListener('online', () => {
      this.is_online = true;
      this.publishBacklog();
    });
    window.addEventListener('offline', () => this.is_online = false);
    this.team_data = JSON.parse(localStorage.getItem('team_data'));
    if (this.team_data === null) {
      this.team_data = [];
    }
    this.backlog = JSON.parse(localStorage.getItem('offline-backlog'));
    if (this.backlog === null) {
      this.backlog = [];
    }
    this.updateLocalStorage();
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
      this.makeTBARequest<Match[]>('event/' + regional_id + '/matches/simple'),
      this.utils.craftHttpGetPit('getteams')
    ).mergeMap((res: [Team[]|Boolean, Match[]|Boolean, HttpResponse<any>]) => {
      if (res[0] && res[1]) {
        this.team_data = res[2].body.teams.map((team) => {
          team.teamname = team.team_name;
          team.teamnumber = team.team_number;
          delete team.team_name;
          delete team.team_number;
          return team;
        });
        this.updateLocalStorage();
        const rd: RegionalData = {
          teams: (<Team[]>res[0]),
          matches: (<Match[]>res[1]).sort((a, b) => this.sortReg(a, b))
        };
        return Observable.of(rd);
      }
    });
  }

  public getRegionalListTBA(): Observable<Regional[]|Boolean> {
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
  }







  /**
   *
   * THIS AREA IS FOR THE BACKEND API
   *
   */


  public getRegionalList(): Observable<Regional[]|Boolean> {
    return this.utils.craftHttpGetComp('getcomps')
                    .mergeMap((res: HttpResponse<any>) => {
                      if (res.body.hasOwnProperty('competitions')) {
                        const t = res.body.competitions.map((comp) => {
                          comp.key = comp.comp_id;
                          comp.name = comp.nickname;
                          delete comp.comp_id;
                          delete comp.nickname;
                          return comp;
                        });
                        return Observable.of(<Regional[]>t);
                      }
                    }).catch((res: HttpErrorResponse) => {
                      return Observable.of(false);
                    });
  }

  // public updateTeamData(): Observable<PitTeam[]|Boolean> {
  //   return this.utils.craftHttpGetPit('getteams')
  //                   .mergeMap((res: HttpResponse<any>) => {
  //                     return Observable.of(<PitTeam[]>res.body.teams);
  //                   }).catch((res: HttpErrorResponse) => {
  //                     return Observable.of(false);
  //                   }).do((res: Boolean|PitTeam[]) => {
  //                     if (res) {
  //                       this.team_data = <PitTeam[]>res;
  //                       this.updateLocalStorage();
  //                     }
  //                   });
  // }

  public addRegional(key: string, name: string): Observable<Boolean> {
    const data = {
      nickname: name,
      comp_id: key
    };
    return this.utils.craftHttpPostComp('register', data)
                    .mergeMap((res: HttpResponse<null>) => {
                      if (res.status === 204) {
                        this.notif.addNotification('Competition ' + name + ' added.', 1);
                        return Observable.of(true);
                      }
                      return Observable.of(false);
                    }).catch((res: HttpErrorResponse) => {
                      return Observable.of(false);
                    });
  }

  public getPendingUsers(): Observable<User[]|Boolean> {
    return this.utils.craftHttpGetUser('getpending')
                    .mergeMap((res: HttpResponse<any>) => {
                      return Observable.of(res.body.users);
                    }).catch((res: HttpErrorResponse) => {
                      this.notif.addNotification('Error fetching pending users', 3);
                      return Observable.of(false);
                    });
  }

  public acceptPendingUser(username: string, rank: string): Observable<boolean> {
    const data = {
      username: username,
      userlevel: rank
    };
    return this.utils.craftHttpPostUser('approve', data)
                    .mergeMap((res: HttpResponse<null>) => {
                      if (res.status === 204) {
                        this.notif.addNotification('User ' + username + ' was accepted as ' + rank + '.', 1);
                        return Observable.of(true);
                      }
                      return Observable.of(false);
                    }).catch((res: HttpErrorResponse) => {
                      return Observable.of(false);
                    });
  }

  public denyPendingUser(username: string): Observable<boolean> {
    const data = {
      username: username,
      userlevel: 'delete'
    };
    return this.utils.craftHttpPostUser('approve', data)
                  .mergeMap((res: HttpResponse<null>) => {
                    if (res.status === 204) {
                      return Observable.of(true);
                    }
                    return Observable.of(false);
                  }).catch((res: HttpErrorResponse) => {
                    return Observable.of(false);
                  });
  }












  public submitPitScout(data: PitTeam): void {
    // TODO: idk bro JS is weird, if you dont create a copy of the data then by the time the notification pops up,
    //        angular would have cleared the textboxes meaning that the 'data' variable wont point to anything.
    data = Object.assign({}, data);
    if (this.is_online) {
      this.utils.craftHttpPostPit('create', data)
                .mergeMap((res: HttpResponse<Object>) => {
                  if (res.status === 204) {
                    this.notif.addNotification('Pit Scout for team ' + data.teamnumber + ' submitted.', 1);
                    return Observable.of(true);
                  }
                  return Observable.of(false);
                }).catch((res: HttpErrorResponse) => {
                  return Observable.of(false);
                }).subscribe((val: boolean) => {
                  if (val) {
                    console.log('Submitted Success');
                  } else {
                    console.log('Submit Failed.');
                  }
                });
    } else {
      const ps = {
        type: 'pitscout',
        data: data // This copies the data.  If you dont all backlog items will have the reference to the same variable.
      };
      this.addToBacklog(ps);
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

  private addToBacklog(item: BacklogItem): void {
    this.backlog.push(item);
    this.updateLocalStorage();
  }

  private publishBacklog(): void {
    let log: BacklogItem;
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
    localStorage.setItem('team_data', JSON.stringify(this.team_data));
  }

  public getTeamData(): PitTeam[] {
    return this.team_data;
  }

  public getBacklog(): BacklogItem[] {
    return this.backlog;
  }

  public isOnline(): boolean {
    return this.is_online;
  }
}

interface BacklogItem {
  type: string;
  data: any;
}
