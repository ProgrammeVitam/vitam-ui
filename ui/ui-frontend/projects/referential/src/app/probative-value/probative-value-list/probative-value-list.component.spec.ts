import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProbativeValueListComponent } from './probative-value-list.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('ProbativeValueListComponent', () => {
  let component: ProbativeValueListComponent;
  let fixture: ComponentFixture<ProbativeValueListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProbativeValueListComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValueListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
