import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';

@Injectable()
export class AuthService {
  public isLoggedIn = false;

  constructor(private http: HttpClient) { }

  public login(): Observable<boolean> {
    // TODO: Replace with actual request to backend server.
    return Observable.of(true).delay(3000).do((val) => this.isLoggedIn = val);
  }

}
