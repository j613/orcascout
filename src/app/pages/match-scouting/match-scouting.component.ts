import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { GameTemplate } from '../../classes/gametemplate';
import { FormGroup } from '@angular/forms';
import { UtilsService } from '../../services/utils.service';
import { BackendUpdateService } from '../../services/backend-update.service';

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

  constructor(private utils: UtilsService, private backend_update: BackendUpdateService) { }

  ngOnInit() {
    this.match_template = this.utils.getMatchTemplate();
    this.form = this.utils.createFormGroup();
  }

  public submitMatchScout() {
    const old_data = this.form.value;
    const new_data = {
      'autonomous': {},
      'teleop': {}
    };
    for (const key in old_data) {
      if (!old_data.hasOwnProperty(key)) {
        continue;
      }
      if (key.startsWith('teleop_')) {
        new_data.teleop[key.substr(7)] = old_data[key];
      } else if (key.startsWith('auto_')) {
        new_data.autonomous[key.substr(5)] = old_data[key];
      } else {
        new_data[key] = old_data[key];
      }
    }
    this.backend_update.submitMatchScout(new_data);
  }

  public getAutoFields() {
    return this.match_template.autonomous.fields;
  }

  public getTeleopFields() {
    return this.match_template.teleop.fields;
  }
}
