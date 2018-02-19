import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PitScoutingComponent } from './pit-scouting.component';

describe('PitScoutingComponent', () => {
  let component: PitScoutingComponent;
  let fixture: ComponentFixture<PitScoutingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PitScoutingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PitScoutingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
