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
  public comp_list_tba: Regional[];

  constructor(private backend_update: BackendUpdateService) { }

  public regional_add_selected: string;

  ngOnInit() {
    this.updateCompList();
    this.updateCompListTBA();
  }

  public updateCompList() {
    this.backend_update.getRegionalList().subscribe((val: Boolean|Regional[]) => {
      if (val) {
        this.comp_list = <Regional[]>val;
      }
    });
  }

  public updateCompListTBA() {
    this.backend_update.getRegionalListTBA().subscribe((val: Boolean|Regional[]) => {
      if (val) {
        this.comp_list_tba = <Regional[]>val;
      }
    });
  }

  public addCompetition() {
    const key = this.regional_add_selected.split('-')[0];
    const name = this.regional_add_selected.split('-')[1];
    this.backend_update.addRegional(key, name).subscribe((res: Boolean) => {
      if (res) {
        console.log('Goo0d');
      } else {
        console.log('Bad');
      }
    });
  }

}
