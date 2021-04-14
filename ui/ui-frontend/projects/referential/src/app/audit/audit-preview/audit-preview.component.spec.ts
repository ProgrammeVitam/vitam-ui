import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
import {AccessContractService} from '../../access-contract/access-contract.service';
import {AuditService} from '../audit.service';
import {AuditPreviewComponent} from './audit-preview.component';

describe('AuditPreviewComponent', () => {
  let component: AuditPreviewComponent;
  let fixture: ComponentFixture<AuditPreviewComponent>;

  beforeEach(waitForAsync(() => {
    const accessContractServiceMock = {
      getAllForTenant: () => of([])
    };
    const activatedRouteMock = {
      params: of({tenantIdentifier: 1})
    };

    TestBed.configureTestingModule({
      declarations: [AuditPreviewComponent],
      providers: [
        {provide: AuditService, useValue: {}},
        {provide: AccessContractService, useValue: accessContractServiceMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
