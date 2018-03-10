import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { BackendUpdateService } from '../../services/backend-update.service';
import { Regional } from '../../classes/regional';
import { NotificationsService } from '../../services/notifications.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {
  public title = 'Login';
  public message = '';
  public regional_list: Regional[];
  public login_fields = {
    username: '',
    password: '',
    regional_id: ''
  };

  constructor(private auth: AuthService, private route: Router, private act_route: ActivatedRoute, public backend_update: BackendUpdateService, private notif: NotificationsService) { }

  ngOnInit() {
    this.act_route.queryParamMap.subscribe((params: Params) => {
      if (params.params.username) {
        this.login_fields.username = params.params.username;
      }
    });
    if (!this.backend_update.is_online) {
      this.message = 'Currently offline, please go online to login.';
    }
    // Call backend_update to get regional list
    this.backend_update.getRegionalList().subscribe((regionals: Regional[]) => this.regional_list = regionals);
  }

  public login(): void {
    this.notif.addNotification('Logging in...', 0);
    this.auth.login(this.login_fields.username, this.login_fields.password, this.login_fields.regional_id).subscribe((val) => {
      if (val) {
        this.notif.addNotification('Logged In!', 1);
        this.route.navigate(['']);
      } else {
        this.notif.addNotification('Error Logging In.', 3);
      }
    });
  }

}
