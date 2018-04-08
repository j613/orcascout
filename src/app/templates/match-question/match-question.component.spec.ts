import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchQuestionComponent } from './match-question.component';

describe('MatchQuestionComponent', () => {
  let component: MatchQuestionComponent;
  let fixture: ComponentFixture<MatchQuestionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchQuestionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchQuestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
