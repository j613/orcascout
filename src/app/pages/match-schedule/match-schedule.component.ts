import { Component, OnInit, OnDestroy } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { BackendUpdateService } from '../../services/backend-update.service';

@Component({
  selector: 'app-match-schedule',
  templateUrl: './match-schedule.component.html',
  styleUrls: ['./match-schedule.component.less']
})
export class MatchScheduleComponent implements OnInit, OnDestroy {
  private checker;
  public title = 'Match Schedule';

  constructor(public auth: AuthService) { }

  ngOnInit() {
    this.auth.refreshRegionalData();
    this.checker = setInterval(() => {
      this.refreshSchedule();
    }, 30000);
  }

  ngOnDestroy() {
    clearInterval(this.checker);
  }

  public getMatches() {
    return this.auth.getSession().regional.data.matches;
  }

  public refreshSchedule() {
    this.auth.refreshRegionalData();
  }

}
