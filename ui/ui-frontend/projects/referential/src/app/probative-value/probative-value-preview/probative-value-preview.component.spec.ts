import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';

import {AccessContractService} from '../../access-contract/access-contract.service';
import {ProbativeValueService} from '../probative-value.service';
import {ProbativeValuePreviewComponent} from './probative-value-preview.component';

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

    TestBed.configureTestingModule({
      declarations: [ProbativeValuePreviewComponent],
      providers: [
        {provide: AccessContractService, useValue: accessContractServiceMock},
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
