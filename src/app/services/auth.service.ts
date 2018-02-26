import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';
import { BackendUpdateService } from './backend-update.service';
import { Router } from '@angular/router';

interface User {
  username: string;
}

interface Token {
  key: string;
  expiration: number;
}

interface Session {
  user: User;
  token: Token;
}

@Injectable()
export class AuthService {
  public isLoggedIn = false;
  public session: Session;

  private url_endpoint = environment.api_endpoint;

  // TODO: Move away from session and use the cookie thats sent from server when logging in.
  constructor(private backend_update: BackendUpdateService, private router: Router) {
    console.log('AuthService Constructed');
    if (localStorage.getItem('session')) {
      this.session = JSON.parse(localStorage.getItem('session'));

      // TODO: There is a bug where if you're online, and you request a page (eg. match-scouting), the CanActivate function
      //       will return false as this has not resolved yet, which will forward the user to the login page instead of the
      //       correct page.
      this.verifySession(this.session).subscribe((val) => {
        if (val) {
          this.isLoggedIn = true;
        } else {
          localStorage.removeItem('session');
        }
      });
    }
  }

  private verifySession(session: Session): Observable<boolean> {
    // If the client is offline, you need to trust that they didnt modify the session to extend it.
    // Requests to the server will still include a server-issued token so bad data cannot be pushed to the server.
    if (!this.backend_update.is_online) {
      if (this.session.token.expiration > Date.now()) { // If expiration is in the future
        return Observable.of(true);
      } else {
        this.isLoggedIn = false;
        console.log('Token expired, going to login');
        this.router.navigate(['login']);
        return Observable.of(false);
      }
    }
    console.log('Verify');
    if (this.session.token.expiration > Date.now()) {
      console.log('Token Time valid');
      this.isLoggedIn = true;
    }

    // TODO: Make request to backend server to verify if session is still valid.
    return Observable.of(true).delay(1000);
  }

  public login(username: string, password: string): Observable<boolean> {
    if (this.isLoggedIn) {
      return Observable.of(true);
    }
    // TODO: Replace with actual request to backend server.
    return Observable.of(true).delay(1000).do((val) => {
      this.session = {
        user: { username: username },
        token: { key: 'asd', expiration: Date.now() + 20000} // Make it expire in 100 seconds from logging in, this will be set from server
      };
      localStorage.setItem('session', JSON.stringify(this.session));
      this.isLoggedIn = val;
    });
  }

  public logout() {
    this.isLoggedIn = false;
    localStorage.removeItem('session');
  }

}
