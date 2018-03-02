import { Component, OnInit } from '@angular/core';
import { BackendUpdateService } from './../../services/backend-update.service';
import { PitTeam } from '../../classes/pit-team';

@Component({
  selector: 'app-pit-scouting',
  templateUrl: './pit-scouting.component.html',
  styleUrls: ['./pit-scouting.component.less']
})
export class PitScoutingComponent {
  public title = 'Pit Scouting';
  public drivetrains: string[] = ['Six Wheel', 'Eight Wheel', 'Four Wheel', 'Swerve/Crab', 'Mechanum', 'Tank Tread', 'Other'];
  public team: PitTeam = {
    name: null,
    number: null,
    photo: null,
    comments: null,
    drivetrain: 'Six Wheel'
  };

  private fr = new FileReader();

  constructor(private backend_update: BackendUpdateService) { }

  public submitPit() {
    this.backend_update.submitPitScout(this.team);
  }

  public updatePhotos(event) {
    this.fr.onload = () => {
      this.team.photo = this.fr.result;
    };
    this.fr.readAsDataURL(event.target.files[0]);
  }

}
