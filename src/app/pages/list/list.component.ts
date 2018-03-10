import { Component, OnInit } from '@angular/core';
import { Team } from '../../classes/team';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.less']
})
export class ListComponent implements OnInit {
  public title = 'List';
  public teams: Team[];

  constructor(private auth: AuthService) { }

  ngOnInit() {
    this.getTeams();
  }

  public getTeams() {
    this.teams = this.auth.getSessionData().regional.data.teams;
  }

}
