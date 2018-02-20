import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-pit-scouting',
  templateUrl: './pit-scouting.component.html',
  styleUrls: ['./pit-scouting.component.less']
})
export class PitScoutingComponent implements OnInit {
  public title: string = "Pit Scouting";
  public drivetrains: string[] = ["Six Wheel", "Eight Wheel", "Four Wheel", "Swerve/Crab", "Mechanum", "Tank Tread", "Other"];

  public team: any = {
    name: "asd",
    number: "d",
    photo: "d",
    comments: "d",
    drivetrain: "Six Wheel"
  }

  constructor() { }

  ngOnInit() {
  }

}
