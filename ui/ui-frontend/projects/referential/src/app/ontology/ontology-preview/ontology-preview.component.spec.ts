import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OntologyPreviewComponent } from './ontology-preview.component';
import {NO_ERRORS_SCHEMA} from "@angular/core";

describe('OntologyPreviewComponent', () => {
  let component: OntologyPreviewComponent;
  let fixture: ComponentFixture<OntologyPreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OntologyPreviewComponent ],
      schemas: [ NO_ERRORS_SCHEMA ]
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
