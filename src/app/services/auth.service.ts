import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
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
    // if (this.isLoggedIn && this.session) {
    //   return Observable.of(true);
    // }

    // TODO: Replace with actual request to backend server (below).
    // return Observable.of(true).delay(1000).do((val) => {
    //   this.session = {
    //     user: { username: username,
    //             firstname: 'Zamboni',
    //             lastname: 'Macaroni',
    //             level: 'regular',
    //           }
    //   };
    //   this.session.regional = {
    //     key: regional_id.split('-')[0],
    //     name: regional_id.split('-')[1]
    //   };
    //   this.refreshRegionalData();
    //   this.saveSession();
    //   document.cookie = 'AuthToken=SoMeToKeNlMaO; expires=' + new Date(Date.now() + (30 * 60 * 1000)).toUTCString();
    //   this.isLoggedIn = val;
    // });

    // Actual HTTP request when backend is hosted.
    return this.utils.craftHttpPost('login', { username: username, password: password }) // Login request
              // TODO: the 2 template parameters are 'input' and 'expected return' types.
              .mergeMap<any, any>((res: HttpResponse<null>) => {
                if (res.status === 204) { // If valid login, make request for userinfo
                  return this.utils.craftHttpGet('getinfo');
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
                this.backend_update.getRegionalData(this.session.regional.key).subscribe((reg: RegionalData) => {
                  this.session.regional.data = reg;
                  this.saveSession();
                });
                this.saveSession();
                return Observable.of(true);
              });
  }

  public register(firstname: string, lastname: string, username: string, password: string): Observable<boolean> {
    const user_data = {
      firstname: firstname,
      lastname: lastname,
      username: username,
      password: password
    };
    return this.utils.craftHttpPost('create', user_data)
                  .mergeMap((res: HttpResponse<null>) => {
                    if (res.status === 204) {
                      return Observable.of(true);
                    }
                    return Observable.of(false);
                  }).catch(() => {
                    return Observable.of(false);
                  });
  }

  public refreshRegionalData() {
    this.backend_update.getRegionalData(this.session.regional.key).subscribe((reg: RegionalData) => {
      this.session.regional.data = reg;
      this.saveSession();
    });
  }

  public logout() {
    this.utils.craftHttpGet('logout').subscribe((val) => {
      console.log('User Logged Out');
    });

    this.isLoggedIn = false;
    localStorage.removeItem('session');
    this.router.navigate(['login']);
  }

  private saveSession(): void {
    localStorage.setItem('session', JSON.stringify(this.session));
  }

}
