import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, CanLoad, Route, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthGuard implements CanActivate, CanLoad {
  constructor(private auth: AuthService, private router: Router) {}

  // TODO: Create actual auth checking
  private canView(): boolean {
    return this.auth.userLoggedIn() && this.auth.getSession().user.level.toLowerCase() !== 'limited';
  }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    console.log('CanActivate Called');

    if (this.canView()) {
      return true;
    } else if (this.auth.getSession().user.level.toLowerCase() === 'limited') {
      this.router.navigate(['limited']);
      return false;
    }

    console.log('Login not verified, forwarding to login page.');
    this.router.navigate(['login']);
    return false;
  }

  canLoad(route: Route) {
    return this.canView() && this.auth.getSession().user.level.toLowerCase() !== 'limited';
  }
}