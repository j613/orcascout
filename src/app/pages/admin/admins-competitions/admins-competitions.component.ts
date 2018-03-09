import { Component, OnInit } from '@angular/core';
import { BackendUpdateService } from '../../../services/backend-update.service';
import { Regional } from '../../../classes/regional';

@Component({
  selector: 'app-admins-competitions',
  templateUrl: './admins-competitions.component.html',
  styleUrls: ['./admins-competitions.component.less']
})
export class AdminCompetitionsComponent implements OnInit {
  public comp_list: Regional[];

  constructor(private backend_update: BackendUpdateService) { }

  ngOnInit() {
    this.updateCompList();
  }

  public updateCompList() {
    console.log('pee');
    this.backend_update.getRegionalList().subscribe((val: Boolean|Regional[]) => {
      if (val) {
        this.comp_list = <Regional[]>val;
      }
    });
  }

}
