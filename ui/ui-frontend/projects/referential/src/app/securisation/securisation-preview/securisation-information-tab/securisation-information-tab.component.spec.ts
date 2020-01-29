import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurisationInformationTabComponent } from './securisation-information-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('SecurisationInformationTabComponent', () => {
  let component: SecurisationInformationTabComponent;
  let fixture: ComponentFixture<SecurisationInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurisationInformationTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
