import {NO_ERRORS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import {MatSidenavModule} from '@angular/material/sidenav';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ActivatedRoute, Router} from '@angular/router';
import {of} from 'rxjs';
import {InjectorModule, LoggerModule} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {ProbativeValueComponent} from './probative-value.component';

describe('ProbativeValueComponent', () => {
  let component: ProbativeValueComponent;
  let fixture: ComponentFixture<ProbativeValueComponent>;

  beforeEach(waitForAsync(() => {
    const activatedRouteMock = {
      params: of({tenantIdentifier: 1}),
      data: of({appId: 'PROBATIVE_VALUE_APP'})
    };
    TestBed.configureTestingModule({
      imports:
        [
          ReactiveFormsModule,
          VitamUICommonTestModule,
          NoopAnimationsModule,
          MatSelectModule,
          InjectorModule,
          MatSidenavModule,
          LoggerModule.forRoot()
        ],
      declarations: [ProbativeValueComponent],
      providers: [
        FormBuilder,
        {provide: MatDialog, useValue: {}},
        {provide: ActivatedRoute, useValue: activatedRouteMock},
        { provide: Router, useValue: { navigate: () => {} } }
      ],
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
