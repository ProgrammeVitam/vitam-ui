import {HttpClientTestingModule} from '@angular/common/http/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder} from '@angular/forms';
import {MatDatepickerModule, MatDialog, MatNativeDateModule, MatSidenavModule} from '@angular/material';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {GlobalEventService, InjectorModule, LoggerModule} from 'ui-frontend-common';

import {SecurisationComponent} from './securisation.component';

describe('SecurisationComponent', () => {
  let component: SecurisationComponent;
  let fixture: ComponentFixture<SecurisationComponent>;

  beforeEach(async(() => {
    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'SECURISATION_APP'})
    };

    TestBed.configureTestingModule({
      imports: [
        MatSidenavModule,
        MatDatepickerModule,
        MatNativeDateModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        InjectorModule,
        LoggerModule.forRoot()
      ],
      declarations: [SecurisationComponent],
      providers: [
        FormBuilder,
        GlobalEventService,
        {provide: MatDialog, useValue: {}},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ],
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
