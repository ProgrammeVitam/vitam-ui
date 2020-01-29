import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import {AuthService, BASE_URL} from 'ui-frontend-common';
import { OntologyListComponent } from "./ontology-list.component";
import {Ontology} from "vitamui-library";
import {of} from "rxjs";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {OntologyService} from "../ontology.service";

describe('OntologyListComponent', () => {
  let component: OntologyListComponent;
  let fixture: ComponentFixture<OntologyListComponent>;

  const ontologyServiceMock = {
    delete: (_item: Ontology) => of(null),
    search: () => of(null)
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [OntologyListComponent],
      providers: [
        { provide: BASE_URL, useValue: "" },
        { provide: OntologyService, useValue: ontologyServiceMock},
        { provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}}
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OntologyListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
