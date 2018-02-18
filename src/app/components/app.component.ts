import { Component } from '@angular/core';
import { Page } from './../classes/page';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  private _opened: boolean = true
  private _show_backdrop: boolean = true;
  private _current_icon: string;
  private pages: Page[] = [
    { name: "Match Scouting", path: "" },
    { name: "Edit Match Scouting", path: "" },
    { name: "Pit Scouting", path: "" },
    { name: "Graph", path: "" },
    { name: "List", path: "" },
    { name: "Coach", path: "" },
    { name: "Match Schedule", path: "" },
    { name: "Notes", path: "" },
    { name: "Alliance Selection", path: "" },
    { name: "Settings", path: "" }
  ];

  constructor() {
    this._current_icon = this._opened ? "angle-left" : "angle-right";
  }

  private _toggleSidebar() {
    this._opened = !this._opened;
    this._current_icon = this._opened ? "angle-left" : "angle-right";
  }
}
