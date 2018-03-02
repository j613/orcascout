import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { BackendUpdateService } from '../../services/backend-update.service';

@Component({
  selector: 'app-match-schedule',
  templateUrl: './match-schedule.component.html',
  styleUrls: ['./match-schedule.component.less']
})
export class MatchScheduleComponent implements OnInit {
  public title = 'Match Schedule';

  constructor(public auth: AuthService) {
    this.auth.refreshRegionalData();
  }

  ngOnInit() {
  }

  private refreshSchedule() {
    this.auth.refreshRegionalData();
  }

}
