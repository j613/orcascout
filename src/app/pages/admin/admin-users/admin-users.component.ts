import { Component, OnInit } from '@angular/core';
import { BackendUpdateService } from '../../../services/backend-update.service';
import { User } from '../../../classes/user';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.less']
})
export class AdminUsersComponent implements OnInit {
  public pending_users: User[];

  constructor(private backend_update: BackendUpdateService) { }

  ngOnInit() {
    this.updatePendingUsers();
  }

  private updatePendingUsers() {
    this.backend_update.getPendingUsers().subscribe((val: User[]|Boolean) => {
      if (val) {
        this.pending_users = <User[]>val;
      }
    });
  }

  public acceptUser(username: string, rank: string) {
    this.backend_update.acceptPendingUser(username, rank).subscribe((res: boolean) => {
      console.log(res);
    });
    this.pending_users = this.pending_users.filter(user => user.username !== username);
  }

  public denyUser(username: string) {
    this.backend_update.denyPendingUser(username).subscribe((res: boolean) => {
      console.log(res);
    });
    this.pending_users = this.pending_users.filter(user => user.username !== username);
  }

}
