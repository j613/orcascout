import { Component, OnInit } from '@angular/core';
import { BackendUpdateService } from './../../services/backend-update.service';
import { PitTeam } from '../../classes/pit-team';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-pit-scouting',
  templateUrl: './pit-scouting.component.html',
  styleUrls: ['./pit-scouting.component.less']
})
export class PitScoutingComponent {
  public title = 'Pit Scouting';
  public drivetrains: string[] = ['Six Wheel', 'Eight Wheel', 'Four Wheel', 'Swerve/Crab', 'Mechanum', 'Tank Tread', 'Other'];
  public team: PitTeam = {
    teamname: null,
    teamnumber: null,
    image: null,
    notes: null,
    drivetrain: 'Six Wheel'
  };

  private fr = new FileReader();

  constructor(private backend_update: BackendUpdateService, private auth: AuthService) { }

  public submitPit() {
    this.backend_update.submitPitScout(this.team);
  }

  public updatePhotos(event) {
    this.fr.onload = () => {
      // this.team.image = this.fr.result;
      this.team.image = 'placeholder';
    };
    this.fr.readAsDataURL(event.target.files[0]);
  }

}
