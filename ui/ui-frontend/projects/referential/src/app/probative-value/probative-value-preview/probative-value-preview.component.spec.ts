import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProbativeValuePreviewComponent } from './probative-value-preview.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { AccessContractService } from '../../access-contract/access-contract.service';
import { ProbativeValueService } from '../probative-value.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('ProbativeValuePreviewComponent', () => {
  let component: ProbativeValuePreviewComponent;
  let fixture: ComponentFixture<ProbativeValuePreviewComponent>;

  beforeEach(async(() => {

    const accessContractServiceMock = {
      getAllForTenant: ()=> of([])
    };

    const activatedRouteMock = {
      params: of( { tenantIdentifier: 1 } )
    };

    TestBed.configureTestingModule({
      declarations: [ ProbativeValuePreviewComponent ],
      providers:[
        { provide: AccessContractService, useValue: accessContractServiceMock },
        { provide: ProbativeValueService, useValue: {} },
        { provide: ActivatedRoute, useValue: activatedRouteMock }
      ],
      schemas: [ CUSTOM_ELEMENTS_SCHEMA ]
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
