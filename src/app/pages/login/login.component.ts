import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {
  public title = 'Login';
  public message = 'Logged out';

  constructor(private auth: AuthService) { }

  ngOnInit() {
  }

  public login(): void {
    this.message = 'Logging in...';
    // TODO: Implement form to get username and password
    this.auth.login('username', 'password').subscribe((val) => {
      this.message = 'Logged In';
    });
  }

}
