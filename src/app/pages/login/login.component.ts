import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.less']
})
export class LoginComponent implements OnInit {
  public title = 'Login';
  public message = '';
  public login_fields = {
    username: '',
    password: ''
  };

  constructor(private auth: AuthService, private route: Router) { }

  ngOnInit() {
  }

  public login(): void {
    this.message = 'Logging in...';
    // TODO: Implement form to get username and password
    this.auth.login(this.login_fields.username, this.login_fields.password).subscribe((val) => {
      this.message = 'Logged In';
      // setTimeout(() => {
      //   this.route.navigate(['']);
      // }, 500);
    });
  }

}
