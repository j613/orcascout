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

interface Session {
  user: User;
  regional?: Regional;
}

@Injectable()
export class AuthService {
  public isLoggedIn = false;
  public session: Session;

  constructor(private router: Router, private utils: UtilsService, private backend_update: BackendUpdateService) {
    console.log('AuthService Constructed');
    if (this.utils.parseCookies()['AuthToken'] && localStorage.getItem('session')) { // If there is a session stored in LocalStorage and there is an AuthToken cookie saved
      this.isLoggedIn = true;
      this.session = JSON.parse(localStorage.getItem('session'));
    } else { // This is called if session(localStorage) or AuthToken(is notpresent).  If one of them isnt set, erase them both.
      localStorage.removeItem('session');
      document.cookie = 'AuthToken=; expires=' + new Date(0).toUTCString();
    }
  }

  public userLoggedIn(): boolean {
    if (this.isLoggedIn && this.session != null) {
      return true;
    }
    return false;
  }

  public login(username: string, password: string, regional_id: string): Observable<boolean> {
    return this.utils.craftHttpPostUser('login', { username: username, password: password }) // Login request
              // TODO: the 2 template parameters are 'input' and 'expected return' types.
              .mergeMap<any, any>((res: HttpResponse<null>) => {
                if (res.status === 204) { // If valid login, make request for userinfo
                  return this.utils.craftHttpGetUser('getinfo');
                }
                return Observable.of(false);
              })
              .mergeMap((res: HttpResponse<User>|Boolean) => {
                if (!res) {
                  return Observable.of(false);
                }
                res = <HttpResponse<User>>res;
                if (res.status !== 200) {
                  return Observable.of(false);
                }
                this.isLoggedIn = true;
                this.session = {
                  user: res.body
                };
                this.session.regional = {
                  key: regional_id.split('-')[0],
                  name: regional_id.split('-')[1]
                };
                this.refreshRegionalData();
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

  private setRegionalBackend() {
    this.utils.craftHttpPostUser('setcomp', {comp_id: this.session.regional.key})
              .mergeMap((res: HttpResponse<null>) => {
                if (res.status === 204) {
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
                // TODO: Uncomment below line when set competition endpoint is made
                // this.logout();
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
                      .subscribe((reg: RegionalData) => {
                        this.session.regional.data = reg;
                        this.setRegionalBackend();
                        this.saveSession();
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
    this.router.navigate(['login']);
  }

  private saveSession(): void {
    localStorage.setItem('session', JSON.stringify(this.session));
  }

  public getSessionData(): Session {
    return JSON.parse(localStorage.getItem('session'));
  }

}
