import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatLegacyFormFieldModule as MatFormFieldModule } from '@angular/material/legacy-form-field';
import { MatLegacyInputModule as MatInputModule } from '@angular/material/legacy-input';
import { MatLegacySelectModule as MatSelectModule } from '@angular/material/legacy-select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { Ontology } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { OntologyService } from '../../ontology.service';
import { OntologyInformationTabComponent } from './ontology-information-tab.component';

describe('OntologyInformationTabComponent', () => {
  let component: OntologyInformationTabComponent;
  let fixture: ComponentFixture<OntologyInformationTabComponent>;

  const ontologyServiceMock = {
    patch: (_data: any) => of(null),
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
    description: 'Mon Ontologie',
    typeDetail: 'string',
    stringSize: 'MEDIUM',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, VitamUICommonTestModule, NoopAnimationsModule, MatFormFieldModule, MatInputModule, MatSelectModule],
      declarations: [OntologyInformationTabComponent],
      providers: [FormBuilder, { provide: OntologyService, useValue: ontologyServiceMock }],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

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
