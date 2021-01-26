import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatSnackBar} from '@angular/material/snack-bar';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {BASE_URL, TableFilterModule} from 'ui-frontend-common';
import {AccessContractListComponent} from './access-contract-list.component';

describe('AccessContractListComponent', () => {
  let component: AccessContractListComponent;
  let fixture: ComponentFixture<AccessContractListComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [AccessContractListComponent],
      imports: [
        VitamUICommonTestModule,
        MatProgressSpinnerModule,
        HttpClientTestingModule,
        TableFilterModule
      ],
      providers: [
        {provide: BASE_URL, useValue: ''},
        {provide: MatSnackBar, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
