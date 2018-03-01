import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';
import { BackendUpdateService } from './backend-update.service';
import { Router } from '@angular/router';
import { UtilsService } from './utils.service';

interface Session {
  user: User;
}

interface User {
  username: string;
  firstname: string;
  lastname: string;
  level: string;
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
    if (this.isLoggedIn && this.session) {
      return Observable.of(true);
    }

    // TODO: Replace with actual request to backend server (below).
    return Observable.of(true).delay(1000).do((val) => {
      this.session = {
        user: { username: username,
                firstname: 'Zamboni',
                lastname: 'Macaroni',
                level: 'regular',
              },
      };
      localStorage.setItem('session', JSON.stringify(this.session));
      document.cookie = 'AuthToken=SoMeToKeNlMaO; expires=' + new Date(Date.now() + (10 * 60 * 1000)).toUTCString();
      this.isLoggedIn = val;
      this.backend_update.regional_id = regional_id;
    });

    // // Actual HTTP request when backend is hosted.
    // return this.utils.craftHttpPost('login', { username: username, password: password }) // Login request
    //           .mergeMap((res: HttpResponse<null>) => {
    //             if (res.status === 204) { // If valid login, make request for userinfo
    //               return this.utils.craftHttpGet('getinfo');
    //             }
    //             return Observable.of(false);
    //           })
    //           .mergeMap((res: HttpResponse<User>) => {
    //             if (res.status !== 200) {
    //               return Observable.of(false);
    //             }
    //             this.isLoggedIn = true;
    //             this.session = {
    //               user: res.body
    //             };
    //             localStorage.setItem('session', JSON.stringify(this.session));
    //             this.backend_update.regional_id = regional_id;
    //             return Observable.of(true);
    //           });
  }

  public logout() {
    this.isLoggedIn = false;
    localStorage.removeItem('session');
    document.cookie = 'AuthToken=; expires=' + new Date(0).toUTCString();
    // TODO: Make logout request to server to logout.
    this.router.navigate(['login']);
  }

}
