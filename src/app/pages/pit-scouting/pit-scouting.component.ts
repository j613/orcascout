import { Component, OnInit } from '@angular/core';
import { BackendUpdateService } from './../../services/backend-update.service';
import { PitTeam } from '../../classes/pit-team';

@Component({
  selector: 'app-pit-scouting',
  templateUrl: './pit-scouting.component.html',
  styleUrls: ['./pit-scouting.component.less']
})
export class PitScoutingComponent {
  public title: string = "Pit Scouting";
  public drivetrains: string[] = ["Six Wheel", "Eight Wheel", "Four Wheel", "Swerve/Crab", "Mechanum", "Tank Tread", "Other"];

  public team: PitTeam = {
    name: "asd",
    number: 0,
    photo: "",
    comments: "d",
    drivetrain: "Six Wheel"
  }

  constructor(private backend_update: BackendUpdateService) { }

  public submitPit() {
    this.backend_update.submitPitScout(this.team);
  }

  public updatePhotos(event) {
    let fr = new FileReader();
    fr.onload = () => {
      this.team.photo = fr.result;
    }
    fr.readAsDataURL(event.target.files[0]);
  }

}
