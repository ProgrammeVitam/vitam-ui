import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SecurityProfilePreviewComponent } from './security-profile-preview.component';
import { NO_ERRORS_SCHEMA } from "@angular/core";

describe('SecurityProfilePreviewComponent', () => {
  let component: SecurityProfilePreviewComponent;
  let fixture: ComponentFixture<SecurityProfilePreviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SecurityProfilePreviewComponent ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SecurityProfilePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
