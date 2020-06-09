import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurisationPreviewComponent } from './securisation-preview.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { SecurisationService } from '../securisation.service';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('SecurisationPreviewComponent', () => {
  let component: SecurisationPreviewComponent;
  let fixture: ComponentFixture<SecurisationPreviewComponent>;

  const accessContractServiceMock = {
    getAllForTenant: ()=> of([])
  };

  const activatedRouteMock = {
    params: of( { tenantIdentifier: 1 } )
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurisationPreviewComponent ],
      providers: [
        { provide: SecurisationService, useValue: { } },
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurisationPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
