import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import {Rule} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {AuthService, BASE_URL, VitamUISnackBar} from 'ui-frontend-common';
import {RuleService} from '../rule.service';
import {RuleListComponent} from './rule-list.component';


describe('RuleListComponent', () => {
  let component: RuleListComponent;
  let fixture: ComponentFixture<RuleListComponent>;

  const ruleServiceMock = {
    // tslint:disable-next-line:variable-name
    delete: (_rule: Rule) => of(null),
    search: () => of(null)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [RuleListComponent],
      providers: [
        {provide: BASE_URL, useValue: ''},
        {provide: RuleService, useValue: ruleServiceMock},
        {provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}},
        {provide: VitamUISnackBar, useValue: {}},
        {provide: MatDialog, useValue: {}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
