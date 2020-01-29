import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurisationListComponent } from './securisation-list.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('SecurisationListComponent', () => {
  let component: SecurisationListComponent;
  let fixture: ComponentFixture<SecurisationListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurisationListComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
