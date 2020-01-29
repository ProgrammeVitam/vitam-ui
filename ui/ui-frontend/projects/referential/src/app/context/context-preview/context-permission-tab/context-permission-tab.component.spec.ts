import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContextPermissionTabComponent } from './context-permission-tab.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('AgencyInformationTabComponent', () => {
  let component: ContextPermissionTabComponent;
  let fixture: ComponentFixture<ContextPermissionTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContextPermissionTabComponent ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextPermissionTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
