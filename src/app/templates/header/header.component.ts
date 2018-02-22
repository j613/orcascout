import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-header',
  styleUrls: ['./header.component.less'],
  template: `
  <h2>
    <b>{{ title }}</b>
  </h2>
  <hr>
  `
})
export class HeaderComponent implements OnInit {
  @Input() title: string;

  constructor() { }

  ngOnInit() {
  }

}
