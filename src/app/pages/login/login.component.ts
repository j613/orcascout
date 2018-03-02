import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { BackendUpdateService } from '../../services/backend-update.service';
import { Regional } from '../../classes/regional';

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

  constructor(private auth: AuthService, private route: Router, private backend_update: BackendUpdateService) {
    if (!this.backend_update.is_online) {
      this.message = 'Currently offline, please go online to login.';
    }
    // Call backend_update to get regional list
    this.backend_update.getRegionalList().subscribe((regionals: Regional[]) => this.regional_list = regionals);
  }

  ngOnInit() {
  }

  public login(): void {
    this.message = 'Logging in...';
    // TODO: Implement form to get username and password
    this.auth.login(this.login_fields.username, this.login_fields.password, this.login_fields.regional_id).subscribe((val) => {
      if (val) {
        this.message = 'Logged In';
      } else {
        this.message = 'Error Logging In';
      }
      // setTimeout(() => {
      //   this.route.navigate(['']);
      // }, 500);
    });
  }

}
