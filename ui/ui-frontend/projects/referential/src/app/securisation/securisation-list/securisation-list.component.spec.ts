import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material';
import {of} from 'rxjs';

import {SecurisationService} from '../securisation.service';
import {SecurisationListComponent} from './securisation-list.component';

describe('SecurisationListComponent', () => {
  let component: SecurisationListComponent;
  let fixture: ComponentFixture<SecurisationListComponent>;

  const securisationServiceMock = {
    search: () => of(null)
  };

  beforeEach(async(() => {
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
