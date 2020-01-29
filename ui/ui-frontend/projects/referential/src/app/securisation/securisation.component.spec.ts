import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurisationComponent } from './securisation.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('SecurisationComponent', () => {
  let component: SecurisationComponent;
  let fixture: ComponentFixture<SecurisationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurisationComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
