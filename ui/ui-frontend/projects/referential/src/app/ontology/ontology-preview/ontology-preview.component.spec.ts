import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material';

import {OntologyService} from '../ontology.service';
import {OntologyPreviewComponent} from './ontology-preview.component';

// TODO : fix test
xdescribe('OntologyPreviewComponent', () => {
  let component: OntologyPreviewComponent;
  let fixture: ComponentFixture<OntologyPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [OntologyPreviewComponent],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: OntologyService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OntologyPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
