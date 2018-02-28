import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';
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
  rank: string;
}

@Injectable()
export class AuthService {
  public isLoggedIn = false;
  public session: Session;

  private url_endpoint = environment.api_endpoint;

  constructor(private backend_update: BackendUpdateService, private router: Router, private utils: UtilsService) {
    console.log('AuthService Constructed');
    if (this.utils.parseCookies()['AuthToken'] && localStorage.getItem('session')) { // If there is a session stored in LocalStorage and there is an AuthToken cookie saved
      this.isLoggedIn = true;
      this.session = JSON.parse(localStorage.getItem('session'));
    } else { // This is called if session(localStorage) or AuthToken(is notpresent).  If one of them isnt set, erase them both.
      localStorage.removeItem('session');
      document.cookie = 'AuthToken=; expires=' + new Date(0).toUTCString();
    }
  }

  public login(username: string, password: string): Observable<boolean> {
    if (this.isLoggedIn && this.session) {
      return Observable.of(true);
    }
    // TODO: Replace with actual request to backend server.
    return Observable.of(true).delay(1000).do((val) => {
      this.session = {
        user: { username: username,
                firstname: 'Zamboni',
                lastname: 'Macaroni',
                rank: 'regular',
              },
      };
      localStorage.setItem('session', JSON.stringify(this.session));
      document.cookie = 'AuthToken=SoMeToKeNlMaO; expires=' + new Date(Date.now() + 30000).toUTCString();
      this.isLoggedIn = val;
    });
  }

  public logout() {
    this.isLoggedIn = false;
    localStorage.removeItem('session');
    document.cookie = 'AuthToken=; expires=' + new Date(0).toUTCString();
    // TODO: Make logout request to server to logout.
  }

}
