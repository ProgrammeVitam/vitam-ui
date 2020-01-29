import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OntologyInformationTabComponent } from './ontology-information-tab.component';
import {of} from "rxjs";
import {FormBuilder} from "@angular/forms";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {OntologyService} from "../../ontology.service";

describe('OntologyInformationTabComponent', () => {
  let component: OntologyInformationTabComponent;
  let fixture: ComponentFixture<OntologyInformationTabComponent>;

  const ontologyServiceMock = {
    patch: (_data: any) => of(null)
  };

  const ontologyValue = {
    shortName: 'Name',
    identifier: 'SP-000001',
    type: 'EXTERNAL',
    collections: [''],
    description: 'Mon Ontologie'
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OntologyInformationTabComponent ],
      providers: [
        FormBuilder,
        { provide: OntologyService, useValue: ontologyServiceMock }
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OntologyInformationTabComponent);
    component = fixture.componentInstance;
    component.form.setValue(ontologyValue);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
