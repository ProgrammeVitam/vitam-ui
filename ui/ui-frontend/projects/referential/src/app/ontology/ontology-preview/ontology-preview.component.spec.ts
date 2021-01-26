import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';

import {OntologyService} from '../ontology.service';
import {OntologyPreviewComponent} from './ontology-preview.component';

// TODO : fix test
xdescribe('OntologyPreviewComponent', () => {
  let component: OntologyPreviewComponent;
  let fixture: ComponentFixture<OntologyPreviewComponent>;

  beforeEach(waitForAsync(() => {
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
