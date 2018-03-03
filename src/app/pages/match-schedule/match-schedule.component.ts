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

  constructor(public auth: AuthService) {
    this.auth.refreshRegionalData();
  }

  ngOnInit() {
    this.checker = setInterval(() => {
      this.refreshSchedule();
    }, 10000);
  }

  ngOnDestroy() {
    clearInterval(this.checker);
  }

  private refreshSchedule() {
    this.auth.refreshRegionalData();
  }

}
