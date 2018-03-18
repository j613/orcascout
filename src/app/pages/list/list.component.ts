import { Component, OnInit, OnDestroy } from '@angular/core';
import { Team } from '../../classes/team';
import { AuthService } from '../../services/auth.service';
import { BackendUpdateService } from '../../services/backend-update.service';
import { PitTeam } from '../../classes/pit-team';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.less']
})
export class ListComponent implements OnInit, OnDestroy {
  public _title = 'List';
  public teams: Team[];

  private checker;

  constructor(private auth: AuthService, private backend_update: BackendUpdateService) { }

  ngOnInit() {
    this.teams = this.auth.getSession().regional.data.teams;
    this.refreshData();
    this.checker = setInterval(() => {
      this.refreshData();
    }, 30000);
  }

  ngOnDestroy() {
    clearInterval(this.checker);
  }

  private refreshData() {
    this.auth.refreshRegionalData();
  }

  public getData(team_num: number): PitTeam {
    // TODO: Figure out why === doesnt work in this comparison.
    return this.backend_update.getTeamData().find(tm => tm.teamnumber == team_num);
  }
}
