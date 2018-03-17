import { Component, OnInit, OnDestroy } from '@angular/core';
import { Team } from '../../classes/team';
import { AuthService } from '../../services/auth.service';
import { BackendUpdateService } from '../../services/backend-update.service';
import { PitTeam } from '../../classes/pit-team';
import { Match } from '../../classes/match';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.less']
})
export class ListComponent implements OnInit, OnDestroy {
  public title = 'List';
  public teams: Team[];

  private checker;

  constructor(private auth: AuthService, private backend_update: BackendUpdateService) { }

  ngOnInit() {
    this.teams = this.auth.getSessionData().regional.data.teams.sort((t1, t2) => {
      return t1.team_number > t2.team_number ? 1 : -1;
    });
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
    return this.backend_update.team_data.find(tm => tm.teamnumber == team_num);
  }

  public getWinStats(team_num: number): string {
    const matches = this.getMatches(team_num).filter((m) => m.alliances.red.score !== -1);
    const wins = matches.filter((m) => {
      if (m.alliances.red.team_keys.indexOf('frc' + team_num) >= 0) { // If on red alliance
        if (m.alliances.red.score > m.alliances.blue.score) {
          return true;
        }
        return false;
      } else { // Must be on blue alliance
        if (m.alliances.blue.score > m.alliances.red.score) {
          return true;
        }
        return false;
      }
    }).length;
    const ties = matches.filter((m) => {
      if (m.alliances.red.score === m.alliances.blue.score && m.alliances.red.score !== -1) {
        return true;
      }
      return false;
    }).length;
    const losses = matches.length - wins - ties;
    return wins + '-' + losses + '-' + ties;
  }

  public getMatches(team_num: number): Match[] {
    return this.auth.getSessionData().regional.data.matches.filter((m) => {
      if (m.alliances.blue.team_keys.indexOf('frc' + team_num) >= 0 || m.alliances.red.team_keys.indexOf('frc' + team_num) >= 0) {
        return true;
      }
      return false;
    });
  }
}
