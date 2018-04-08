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

  constructor() { }

  ngOnInit() {
    console.log(this.question);
  }

  inc() {
    // This is kinda hacky...
    const ele: HTMLFormElement = <HTMLFormElement>document.getElementById(this.question.name);
    if (ele.value < this.question.max_value) {
      ele.value = ++this.form.value[this.question.name];
    }
  }

  dec() {
    // Also kinda hacky...
    const ele: HTMLFormElement = <HTMLFormElement>document.getElementById(this.question.name);
    if (ele.value > this.question.min_value) {
      ele.value = --this.form.value[this.question.name];
    }
  }

}
