import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

import {AccessContractService} from '../../access-contract/access-contract.service';
import {ProbativeValueService} from '../probative-value.service';
import {ProbativeValuePreviewComponent} from './probative-value-preview.component';
import {ExternalParametersService, ExternalParameters} from 'ui-frontend-common';

describe('ProbativeValuePreviewComponent', () => {
  let component: ProbativeValuePreviewComponent;
  let fixture: ComponentFixture<ProbativeValuePreviewComponent>;

  beforeEach(waitForAsync(() => {

    const accessContractServiceMock = {
      getAllForTenant: () => of([])
    };

    const activatedRouteMock = {
      params: of({tenantIdentifier: 1})
    };

    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters)
    };

    TestBed.configureTestingModule({
      declarations: [ProbativeValuePreviewComponent],
      imports: [
        MatSnackBarModule
      ],
      providers: [
        {provide: AccessContractService, useValue: accessContractServiceMock},
        {provide: ExternalParametersService, useValue: externalParametersServiceMock},
        {provide: ProbativeValueService, useValue: {}},
        {provide: ActivatedRoute, useValue: activatedRouteMock}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProbativeValuePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
