import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';
import { AuthService } from './auth.service';
import { FormControl, FormGroup } from '@angular/forms';
import { GameTemplate } from '../classes/gametemplate';

interface Cookie {
  name: string;
  data: string;
}

@Injectable()
export class UtilsService {

  private url_endpoint = environment.api_endpoint;

  constructor(private http: HttpClient) { }

  public parseCookies(): Object {
    const cookies: Object = {};
    for (const c of document.cookie.split('; ')) {
      const cookie = c.split('=');
      cookies[cookie[0]] = cookie[1];
    }
    return cookies;
  }

  public getMatchTemplate(): GameTemplate {
    return JSON.parse(localStorage.getItem('match-template'));
  }

  public createFormGroup(): FormGroup {
    const group: any = {};
    const questions = this.getMatchTemplate();

    group['team_number'] = new FormControl('');

    questions.autonomous.fields.forEach(q => {
      group[q.name] = new FormControl(q.default_value !== null ? q.default_value : '');
    });

    questions.teleop.fields.forEach(q => {
      group[q.name] = new FormControl(q.default_value !== null ? q.default_value : '');
    });

    return new FormGroup(group); // Need to create a form group based off of data
  }

  public craftHttpPostPit(endpoint: string, data: Object) {
    return this.http.post(this.url_endpoint + 'submitPit?method=' + endpoint, data, {observe: 'response', withCredentials: true});
  }

  public craftHttpGetPit(endpoint: string) {
    return this.http.get(this.url_endpoint + 'submitPit?method=' + endpoint, {observe: 'response', withCredentials: true});
  }

  public craftHttpPostComp(endpoint: string, data: Object) {
    return this.http.post(this.url_endpoint + 'submitComp?method=' + endpoint, data, {observe: 'response', withCredentials: true});
  }

  public craftHttpGetComp(endpoint: string) {
    return this.http.get(this.url_endpoint + 'submitComp?method=' + endpoint, {observe: 'response', withCredentials: true});
  }

  public craftHttpPostUser(endpoint: string, data: Object): Observable<HttpResponse<Object>> {
    return this.http.post(this.url_endpoint + 'submitUser?method=' + endpoint, data, {observe: 'response', withCredentials: true});
  }

  public craftHttpGetUser(endpoint: string): Observable<HttpResponse<Object>> {
    return this.http.get(this.url_endpoint + 'submitUser?method=' + endpoint, {observe: 'response', withCredentials: true});
  }

  public craftHttpGetGameTemplate(): Observable<HttpResponse<Object>> {
    return this.http.get(this.url_endpoint + 'gametemplates/' + environment.year + '.json', {observe: 'response', withCredentials: true});
  }

}
