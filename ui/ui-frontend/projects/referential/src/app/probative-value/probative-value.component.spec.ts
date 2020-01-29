import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProbativeValueComponent } from './probative-value.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('AuditComponent', () => {
  let component: ProbativeValueComponent;
  let fixture: ComponentFixture<ProbativeValueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProbativeValueComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
