import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchScoutingComponent } from './match-scouting.component';

describe('MatchScoutingComponent', () => {
  let component: MatchScoutingComponent;
  let fixture: ComponentFixture<MatchScoutingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MatchScoutingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MatchScoutingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
