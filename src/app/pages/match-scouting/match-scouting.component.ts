import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-match-scouting',
  templateUrl: './match-scouting.component.html',
  styleUrls: ['./match-scouting.component.less']
})
export class MatchScoutingComponent implements OnInit {
  public title = 'Match Scouting';
  public match_data = {
    team_name: null,
    team_number: null,
    locations: [
      // {
      //   x: 0,
      //   y: 0,
      //   action: ''
      // }
    ],
    auto: {
      crossed_baseline: null,
      switch: 0,
      scale: 0,
      comments: null
    },
    teleop: {
      teams_switch: 0,
      scale: 0,
      opponents_switch: 0,
      cube_exchange: 0,
      climb: 'noclimb',
      comments: null,
      penalties: null
    },
  };

  constructor() { }

  ngOnInit() {
  }

  public submitMatchScout() {
    console.log(this.match_data);
  }
}
