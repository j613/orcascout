import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { GameTemplate } from '../../classes/gametemplate';
import { FormGroup } from '@angular/forms';
import { UtilsService } from '../../services/utils.service';

@Component({
  selector: 'app-match-scouting',
  templateUrl: './match-scouting.component.html',
  styleUrls: ['./match-scouting.component.less']
})
export class MatchScoutingComponent implements OnInit {
  public title = 'Match Scouting';
  // public match_template = {
  //   team_name: null,
  //   team_number: null,
  //   locations: [
  //     // {
  //     //   x: 0,
  //     //   y: 0,
  //     //   action: ''
  //     // }
  //   ],
  //   auto: {
  //     crossed_baseline: null,
  //     switch: 0,
  //     scale: 0,
  //     comments: null
  //   },
  //   teleop: {
  //     teams_switch: 0,
  //     scale: 0,
  //     opponents_switch: 0,
  //     cube_exchange: 0,
  //     climb: 'noclimb',
  //     comments: null,
  //     penalties: null
  //   },
  // };

  public form: FormGroup;
  public match_template: GameTemplate;

  constructor(private utils: UtilsService) { }

  ngOnInit() {
    this.match_template = this.utils.getMatchTemplate();
    this.form = this.utils.createFormGroup();
  }

  public submitMatchScout() {
    console.log(this.form.value);
  }

  public getAutoFields() {
    return this.match_template.autonomous.fields;
  }

  public getTeleopFields() {
    return this.match_template.teleop.fields;
  }
}
