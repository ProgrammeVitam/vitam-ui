import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {Rule} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {RuleService} from '../../rule.service';
import {RuleInformationTabComponent} from './rule-information-tab.component';

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
    version: 1,
    versionPronom: '3.0',
    extensions: ['.puid']
  };

  const previousValue: Rule = {
    id: 'vitam_id',
    tenant: 1,
    version: 1,
    ruleId: 'ruleId',
    ruleType: 'AppraisalRule',
    ruleValue: 'RuleValue',
    ruleDescription: 'Règle de gestion',
    ruleDuration: '10',
    ruleMeasurement: 'Day',
    creationDate: '20/02/2020',
    updateDate: '20/02/2020'
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
