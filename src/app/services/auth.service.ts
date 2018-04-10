import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';
import 'rxjs/add/operator/finally';
import { BackendUpdateService } from './backend-update.service';
import { Router } from '@angular/router';
import { UtilsService } from './utils.service';
import { Regional, RegionalData } from '../classes/regional';
import { User } from '../classes/user';
import { GameTemplate } from '../classes/gametemplate';

interface Session {
  user: User;
  regional?: Regional;
}

@Injectable()
export class AuthService {
  private isLoggedIn = false;
  private session: Session;

  constructor(private router: Router, private utils: UtilsService, private backend_update: BackendUpdateService) {
    console.log('AuthService Constructed');
    if (localStorage.getItem('session')) { // If there is a session stored in LocalStorage and there is an AuthToken cookie saved
      this.isLoggedIn = true;
      this.session = JSON.parse(localStorage.getItem('session'));
    } else { // This is called if session(localStorage) or AuthToken(is notpresent).  If one of them isnt set, erase them both.
      localStorage.removeItem('session');
    }
  }

  public userLoggedIn(): boolean {
    if (this.isLoggedIn && this.session != null) {
      return true;
    }
    return false;
  }

  public getSession(): Session {
    return this.session;
  }

  public getMatchTemplate(): GameTemplate {
    return this.utils.getMatchTemplate();
  }

  public getRegional(): Regional {
    if (this.session.regional) {
      return this.session.regional;
    }
    return null;
  }

  public login(username: string, password: string, regional_id: string): Observable<boolean> {
    return this.utils.craftHttpPostUser('login', { username: username, password: password }) // Login request
              .mergeMap<any, any>((res: HttpResponse<null>) => {
                if (res.status === 204) { // If valid login, make request for userinfo
                  return Observable.forkJoin(
                    this.utils.craftHttpGetUser('getinfo'),
                    this.utils.craftHttpGetGameTemplate()
                  );
                }
                return Observable.of(false);
              })
              .mergeMap((res: [HttpResponse<User>|Boolean, HttpResponse<Object>|Boolean]) => {
                if (!res) {
                  return Observable.of(false);
                }
                const info = <HttpResponse<User>>(res[0]);
                const template = <HttpResponse<Object>>(res[1]);
                if (info.status !== 200 && template.status !== 200) {
                  return Observable.of(false);
                }
                this.isLoggedIn = true;
                this.session = {
                  user: info.body
                };
                this.setRegional(regional_id.split('-')[0], regional_id.split('-')[1]);
                console.log(template);
                localStorage.setItem('match-template', JSON.stringify(template.body));
                this.saveSession();
                return Observable.of(true);
              }).catch((res: HttpErrorResponse) => {
                if (res.status === 401) {
                  console.log('Error logging in.');
                  this.logout();
                  return Observable.of(false);
                }
                return Observable.of(false);
              });
  }

  public register(firstname: string, lastname: string, username: string, password: string): Observable<boolean> {
    const user_data = {
      firstname: firstname,
      lastname: lastname,
      username: username,
      password: password
    };
    return this.utils.craftHttpPostUser('create', user_data)
                  .mergeMap((res: HttpResponse<null>) => {
                    if (res.status === 204) {
                      return Observable.of(true);
                    }
                    return Observable.of(false);
                  }).catch((res: HttpErrorResponse) => {
                    if (res.status === 401) {
                      return Observable.of(false);
                    }
                    return Observable.of(false);
                  });
  }

  private setRegional(reg_id: string, reg_name: string) {
    this.utils.craftHttpPostUser('setcomp', {comp_id: reg_id})
              .mergeMap((res: HttpResponse<null>) => {
                if (res.status === 204) {
                  this.session.regional = {
                    key: reg_id,
                    name: reg_name
                  };
                  this.refreshRegionalData();
                  return Observable.of(true);
                } else {
                  return Observable.of(false);
                }
              }).catch((res: HttpErrorResponse) => {
                if (res.status === 400) {
                  switch (res.headers.get('X-Error-Code')) {
                    case '1':
                      console.log('Competition doesnt exist in DB');
                      break;
                    case '2':
                      console.log('SQL Error');
                      break;
                  }
                }
                this.logout();
                return Observable.of(false);
              }).subscribe((res: boolean) => {
                if (res) {
                  console.log('Set regional in the backend.');
                } else {
                  console.log('Error setting backend regional');
                }
              });
  }

  public refreshRegionalData() {
    this.backend_update.getRegionalData(this.session.regional.key)
                      .subscribe((reg: RegionalData|Boolean) => {
                        if (reg) {
                          reg = <RegionalData>reg;
                          this.session.regional.data = reg;
                          // this.setRegionalBackend();
                          this.saveSession();
                        }
                      });
  }

  public logout() {
    this.utils.craftHttpGetUser('logout').subscribe((val) => {
      console.log('User Logged Out');
    });

    this.isLoggedIn = false;
    localStorage.removeItem('session');
    localStorage.removeItem('offline-backlog');
    localStorage.removeItem('team_data');
    localStorage.removeItem('match-template');
    this.router.navigate(['login']);
  }

  private saveSession(): void {
    localStorage.setItem('session', JSON.stringify(this.session));
  }
}
