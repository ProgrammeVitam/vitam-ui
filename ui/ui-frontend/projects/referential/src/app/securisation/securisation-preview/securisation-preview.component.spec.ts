import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

import {AccessContractService} from '../../access-contract/access-contract.service';
import {SecurisationService} from '../securisation.service';
import {SecurisationPreviewComponent} from './securisation-preview.component';

describe('SecurisationPreviewComponent', () => {
  let component: SecurisationPreviewComponent;
  let fixture: ComponentFixture<SecurisationPreviewComponent>;

  const accessContractServiceMock = {
    getAllForTenant: () => of([])
  };

  const activatedRouteMock = {
    params: of({tenantIdentifier: 1})
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [SecurisationPreviewComponent],
      providers: [
        {provide: SecurisationService, useValue: {}},
        {provide: AccessContractService, useValue: accessContractServiceMock},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
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
