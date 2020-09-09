import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {Rule} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {RuleService} from '../../rule.service';
import {RuleInformationTabComponent} from './rule./rule-information-tab.component

describe('RuleInformationTabComponent', () => {
  let component: RuleInformationTabComponent;
  let fixture: ComponentFixture<RuleInformationTabComponent>;

  const ruleServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const ruleValue = {
    puid: 'EXTERNAL_puid',
    name: 'Name',
    mimeType: 'application/puid',
    version: '1.0',
    versionPronom: '3.0',
    extensions: ['.puid']
  };

  const previousValue: Rule = {
    id: 'vitam_id',
    documentVersion: 0,
    version: '1.0',
    versionPronom: '3.0',
    puid: 'EXTERNAL_puid',
    name: 'Name',
    description: 'Format de Fichier',
    mimeType: 'application/puid',
    hasPriorityOverRuleIDs: [],
    group: 'test',
    alert: false,
    comment: 'No Comment',
    extensions: ['.puid'],
    createdDate: '20/02/2020'
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RuleInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: RuleService, useValue: ruleServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(ruleValue);
    component.previousValue = (): Rule => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
