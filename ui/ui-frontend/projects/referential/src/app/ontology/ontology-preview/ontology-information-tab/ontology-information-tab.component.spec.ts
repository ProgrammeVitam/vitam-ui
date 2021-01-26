import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {Ontology} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {OntologyService} from '../../ontology.service';
import {OntologyInformationTabComponent} from './ontology-information-tab.component';

describe('OntologyInformationTabComponent', () => {
  let component: OntologyInformationTabComponent;
  let fixture: ComponentFixture<OntologyInformationTabComponent>;

  const ontologyServiceMock = {
    // tslint:disable-next-line:variable-name
    patch: (_data: any) => of(null)
  };

  const ontologyValue: Ontology = {
    id: 'id',
    tenant: 0,
    version: 1,
    creationDate: '01-01-2020',
    lastUpdate: '01-01-2020',
    sedaField: 'MyText',
    apiField: 'MyText',
    origin: 'origin',
    shortName: 'Name',
    identifier: 'SP-000001',
    type: 'EXTERNAL',
    collections: [''],
    description: 'Mon Ontologie'
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        VitamUICommonTestModule,
        NoopAnimationsModule,
        MatSelectModule
      ],
      declarations: [OntologyInformationTabComponent],
      providers: [
        FormBuilder,
        {provide: OntologyService, useValue: ontologyServiceMock}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OntologyInformationTabComponent);
    component = fixture.componentInstance;
    component.inputOntology = ontologyValue;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
