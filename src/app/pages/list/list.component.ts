import { Component, OnInit } from '@angular/core';
import { Team } from '../../classes/team';
import { AuthService } from '../../services/auth.service';
import { BackendUpdateService } from '../../services/backend-update.service';
import { PitTeam } from '../../classes/pit-team';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.less']
})
export class ListComponent implements OnInit {
  public title = 'List';
  public teams: Team[];
  public team_data: PitTeam[];

  constructor(private auth: AuthService, private backend_update: BackendUpdateService) { }

  ngOnInit() {
    this.teams = this.auth.getSessionData().regional.data.teams;
    this.auth.refreshRegionalData();
    this.team_data = this.backend_update.getTeamData();
  }

  public getTeamData(team_number: number): PitTeam {
    return this.team_data.find((t) => t.teamnumber === team_number);
  }
}
