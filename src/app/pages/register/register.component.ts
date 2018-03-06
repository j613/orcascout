import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.less']
})
export class RegisterComponent implements OnInit {
  public title = 'Register';
  public message = '';
  public register_fields = {
    firstname: '',
    username: '',
    lastname: '',
    password: ''
  };

  constructor(private router: Router, private auth: AuthService) { }

  ngOnInit() {
  }

  public register() {
    console.log(this.register_fields);
    this.auth.register(this.register_fields.firstname, this.register_fields.lastname, this.register_fields.username, this.register_fields.password).subscribe((val: boolean) => {
      if (val) {
        this.router.navigate(['login']);
      } else {
        this.message = 'Error creating account';
      }
    });
  }

}
