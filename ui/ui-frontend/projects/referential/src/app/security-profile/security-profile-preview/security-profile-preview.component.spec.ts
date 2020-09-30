import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';

import {SecurityProfileService} from '../security-profile.service';
import {SecurityProfilePreviewComponent} from './security-profile-preview.component';

describe('SecurityProfilePreviewComponent', () => {
  let component: SecurityProfilePreviewComponent;
  let fixture: ComponentFixture<SecurityProfilePreviewComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [SecurityProfilePreviewComponent],
      providers: [
        {provide: MatDialog, useValue: {}},
        {provide: SecurityProfileService, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
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
