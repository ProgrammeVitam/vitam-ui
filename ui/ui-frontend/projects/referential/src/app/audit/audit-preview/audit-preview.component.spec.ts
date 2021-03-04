import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import {of} from 'rxjs';
import {AuditService} from '../audit.service';
import {AuditPreviewComponent} from './audit-preview.component';
import {ExternalParameters, ExternalParametersService} from 'ui-frontend-common';

describe('AuditPreviewComponent', () => {
  let component: AuditPreviewComponent;
  let fixture: ComponentFixture<AuditPreviewComponent>;

  beforeEach(waitForAsync(() => {
    const parameters: Map<string, string> = new Map<string, string>();
    parameters.set(ExternalParameters.PARAM_ACCESS_CONTRACT, '1');
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters)
    };

    TestBed.configureTestingModule({
      declarations: [AuditPreviewComponent],
      providers: [
        {provide: AuditService, useValue: {}},
        {provide: ExternalParametersService, useValue: externalParametersServiceMock}
      ],
      imports: [
        MatSnackBarModule
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuditPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
