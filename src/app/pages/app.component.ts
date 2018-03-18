import { Component, OnInit } from '@angular/core';
import { Page } from './../classes/page';
import { Router } from '@angular/router';
import { BackendUpdateService } from './../services/backend-update.service';
import { AuthService } from './../services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent implements OnInit {
  public _opened = false;
  public _show_backdrop = true;
  public _current_icon: string;
  public pages: Page[] = [
    { name: 'Home', path: '', hide_ltd: false },
    { name: 'Match Scouting', path: 'match-scouting', hide_ltd: true },
    { name: 'Edit Match Scouting', path: 'edit-match-scouting', hide_ltd: true },
    { name: 'Pit Scouting', path: 'pit-scouting', hide_ltd: true },
    { name: 'Graph', path: 'graph', hide_ltd: true },
    { name: 'List', path: 'list', hide_ltd: true },
    { name: 'Coach', path: 'coach', hide_ltd: true },
    { name: 'Match Schedule', path: 'match-schedule', hide_ltd: true },
    { name: 'Notes', path: 'notes', hide_ltd: true },
    { name: 'Alliance Selection', path: 'alliance-selection', hide_ltd: true },
    { name: 'Settings', path: 'settings', hide_ltd: false }
  ];

  // TODO: Implement methods to access backend_update and auth from the HTML instead of making them public
  constructor(private router: Router, public backend_update: BackendUpdateService, public auth: AuthService) { }

  ngOnInit() {
    this._current_icon = this._opened ? 'angle-left' : 'angle-right';
  }

  public _toggleSidebar(o: boolean = !this._opened) {
    this._opened = o;
    this._current_icon = this._opened ? 'angle-left' : 'angle-right';
  }

  public getPages() {
    if (this.auth.getSession().user.level.toLowerCase() === 'limited') {
      return this.pages.filter((page) => !page.hide_ltd);
    }
    return this.pages;
  }

  public getSession() {
    return this.auth.getSession();
  }

  public isUserLoggedIn() {
    return this.auth.userLoggedIn();
  }

  public getBacklog() {
    return this.backend_update.getBacklog();
  }

  public isOnline() {
    return this.backend_update.isOnline();
  }

  public logout() {
    // TODO: Should manually logging out clear offline backlog?
    this.auth.logout();
  }
}
