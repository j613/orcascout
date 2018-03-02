import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-header',
  styleUrls: ['./header.component.less'],
  template: `
  <div class="title"><b>{{ title }}</b></div>
  <hr>
  `
})
export class HeaderComponent implements OnInit {
  @Input() title: string;

  constructor() { }

  ngOnInit() {
  }

}
