import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {of} from 'rxjs';

import {SecurisationService} from '../securisation.service';
import {SecurisationListComponent} from './securisation-list.component';

describe('SecurisationListComponent', () => {
  let component: SecurisationListComponent;
  let fixture: ComponentFixture<SecurisationListComponent>;

  const securisationServiceMock = {
    search: () => of(null)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SecurisationListComponent],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: SecurisationService, useValue: securisationServiceMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
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
