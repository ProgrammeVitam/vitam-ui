import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurisationComponent } from './securisation.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import { ActivatedRoute } from '@angular/router';
import { GlobalEventService, InjectorModule, LoggerModule } from 'ui-frontend-common';
import { MatDialog, MatSidenavModule, MatDatepickerModule, MatNativeDateModule } from '@angular/material';
import { FormBuilder } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('SecurisationComponent', () => {
  let component: SecurisationComponent;
  let fixture: ComponentFixture<SecurisationComponent>;

  beforeEach(async(() => {
    const activatedRouteMock = {
      params: of( { tenantIdentifier: 1 } ),
      data: of({ appId: 'SECURISATION_APP'})
    };

    TestBed.configureTestingModule({
      imports:[
        MatSidenavModule,
        MatDatepickerModule,
        MatNativeDateModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        InjectorModule,
        LoggerModule.forRoot()
      ],
      declarations: [ SecurisationComponent ],
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
