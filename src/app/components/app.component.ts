import { Component } from '@angular/core';
import { Page } from './../classes/page';
import { Router } from '@angular/router';
import { BackendUpdateService } from './../services/backend-update.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  public _opened: boolean = false;
  public _show_backdrop: boolean = true;
  public _current_icon: string;
  public pages: Page[] = [
    { name: 'Home', path: '', title: 'asd' },
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

  constructor(private router: Router, private backend_update: BackendUpdateService) {
    this._current_icon = this._opened ? 'angle-left' : 'angle-right';
  }

  public _toggleSidebar() {
    this._opened = !this._opened;
    this._current_icon = this._opened ? 'angle-left' : 'angle-right';
  }
}
