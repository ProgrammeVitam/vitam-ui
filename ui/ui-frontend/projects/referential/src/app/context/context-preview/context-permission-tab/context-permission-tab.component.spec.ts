import {NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {Context, ContextPermission} from 'projects/vitamui-library/src/public-api';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';

import {ContextService} from '../../context.service';
import {ContextPermissionTabComponent} from './context-permission-tab.component';


// TODO fix permissions access problem
xdescribe('ContextPermissionTabComponent', () => {
  let component: ContextPermissionTabComponent;
  let fixture: ComponentFixture<ContextPermissionTabComponent>;

  const contextPermission: ContextPermission = {
    tenant: '1',
    accessContracts: ['AC-000001'],
    ingestContracts: ['IC-000001', 'IC-000002']
  };
  const contextValue = {
    permissions: contextPermission
  };

  const previousValue: Context = {
    id: 'id',
    name: 'name',
    identifier: 'identifier',
    status: 'ACTIVE',
    creationDate: '01-01-2020',
    lastUpdate: '01-01-2020',
    activationDate: '01-01-2020',
    deactivationDate: '01-01-2020',
    enableControl: 'true',
    securityProfile: 'SP-00001',
    permissions: [contextPermission]
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports:
        [
          ReactiveFormsModule,
          VitamUICommonTestModule
        ],
      declarations: [ContextPermissionTabComponent],
      providers: [
        FormBuilder,
        {provide: ContextService, useValue: {}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextPermissionTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(contextValue);
    component.previousValue = (): Context => previousValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
