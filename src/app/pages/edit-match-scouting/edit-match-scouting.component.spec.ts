import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditMatchScoutingComponent } from './edit-match-scouting.component';

describe('EditMatchScoutingComponent', () => {
  let component: EditMatchScoutingComponent;
  let fixture: ComponentFixture<EditMatchScoutingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditMatchScoutingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditMatchScoutingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
