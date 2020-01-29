import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextInformationTabComponent } from './context-information-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('AgencyInformationTabComponent', () => {
  let component: ContextInformationTabComponent;
  let fixture: ComponentFixture<ContextInformationTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContextInformationTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextInformationTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
