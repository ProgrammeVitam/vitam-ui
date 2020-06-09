import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuditPreviewComponent } from './audit-preview.component';
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { AuditService } from '../audit.service';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { ActivatedRoute } from "@angular/router";
import { of } from 'rxjs';

describe('AuditPreviewComponent', () => {
  let component: AuditPreviewComponent;
  let fixture: ComponentFixture<AuditPreviewComponent>;

  beforeEach(async(() => {
    const accessContractServiceMock = {
      getAllForTenant: ()=> of([])
    };
    const activatedRouteMock = {
      params: of( { tenantIdentifier: 1 } )
    };

    TestBed.configureTestingModule({
      declarations: [ AuditPreviewComponent ],
      providers: [
        { provide: AuditService, useValue: { } },
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
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
