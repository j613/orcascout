import { Injectable } from '@angular/core';


interface Cookie {
  name: string;
  data: string;
}

@Injectable()
export class UtilsService {

  constructor() { }

  public parseCookies(): Object {
    const cookies: Object = {};
    for (const c of document.cookie.split('; ')) {
      const cookie = c.split('=');
      cookies[cookie[0]] = cookie[1];
    }
    return cookies;
  }

}
