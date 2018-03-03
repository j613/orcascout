import { Component } from '@angular/core';
import { Page } from './../classes/page';
import { Router } from '@angular/router';
import { BackendUpdateService } from './../services/backend-update.service';
import { AuthService } from './../services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  public _opened = false;
  public _show_backdrop = true;
  public _current_icon: string;
  public pages: Page[] = [
    { name: 'Home', path: ''},
    { name: 'Match Scouting', path: 'match-scouting' },
    { name: 'Edit Match Scouting', path: 'edit-match-scouting' },
    { name: 'Pit Scouting', path: 'pit-scouting' },
    { name: 'Graph', path: 'graph' },
    { name: 'List', path: 'list' },
    { name: 'Coach', path: 'coach' },
    { name: 'Match Schedule', path: 'match-schedule' },
    { name: 'Notes', path: 'notes' },
    { name: 'Alliance Selection', path: 'alliance-selection' },
    { name: 'Settings', path: 'settings' }
  ];

  // TODO: Implement methods to access backend_update and auth from the HTML instead of making them public
  constructor(private router: Router, public backend_update: BackendUpdateService, public auth: AuthService) {
    this._current_icon = this._opened ? 'angle-left' : 'angle-right';
  }

  public _toggleSidebar(o: boolean = !this._opened) {
    this._opened = o;
    this._current_icon = this._opened ? 'angle-left' : 'angle-right';
  }

  public logout() {
    // TODO: Should manually logging out clear offline backlog?
    this.auth.logout();
  }
}
