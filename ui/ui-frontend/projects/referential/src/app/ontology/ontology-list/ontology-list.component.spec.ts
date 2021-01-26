import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Ontology} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {AuthService, BASE_URL} from 'ui-frontend-common';
import {OntologyService} from '../ontology.service';
import {OntologyListComponent} from './ontology-list.component';

describe('OntologyListComponent', () => {
  let component: OntologyListComponent;
  let fixture: ComponentFixture<OntologyListComponent>;

  const ontologyServiceMock = {
    // tslint:disable-next-line:variable-name
    delete: (_item: Ontology) => of(null),
    search: () => of(null)
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [OntologyListComponent],
      providers: [
        {provide: BASE_URL, useValue: ''},
        {provide: MatDialog, useValue: {}},
        {provide: OntologyService, useValue: ontologyServiceMock},
        {provide: AuthService, useValue: {user: {proofTenantIdentifier: '1'}}}
      ],
      schemas: [NO_ERRORS_SCHEMA]
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
