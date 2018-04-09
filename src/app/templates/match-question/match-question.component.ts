import { Component, OnInit, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatchQuestion } from '../../classes/matchquestion';

@Component({
  selector: 'app-match-question',
  templateUrl: './match-question.component.html',
  styleUrls: ['./match-question.component.less']
})
export class MatchQuestionComponent implements OnInit {
  @Input() question: MatchQuestion;
  @Input() form: FormGroup;
  @Input() type: string;
  public name: string;

  constructor() { }

  ngOnInit() {
    this.name = this.type + '_' + this.question.name;
  }

  inc() {
    // This is kinda hacky... (maybe use 'controls' property of FormGroup??)
    const ele: HTMLFormElement = <HTMLFormElement>document.getElementById(this.name);
    if (ele.value < this.question.max_value) {
      ele.value = ++this.form.value[this.name];
    }
  }

  dec() {
    // Also kinda hacky...
    const ele: HTMLFormElement = <HTMLFormElement>document.getElementById(this.name);
    if (ele.value > this.question.min_value) {
      ele.value = --this.form.value[this.name];
    }
  }

  get isValid() {
    return this.form.controls[this.name].valid;
  }

}
