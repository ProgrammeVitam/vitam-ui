import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL } from 'ui-frontend-common';
import { PastisConfiguration } from '../../core/classes/pastis-configuration';
import { ProfileService } from '../../core/services/profile.service';

import { CreateNoticeComponent } from './create-notice.component';

const matDialogData = jasmine.createSpyObj('MAT_DIALOG_DATA', ['open']);
  matDialogData.open.and.returnValue({ afterClosed: () => of(true) });
const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });
const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

describe('CreateNoticeComponent', () => {
  let component: CreateNoticeComponent;
  let fixture: ComponentFixture<CreateNoticeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateNoticeComponent ],
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        FormBuilder,
        ProfileService,
        PastisConfiguration,
        { provide: BASE_URL, useValue: '/pastis-api' },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: matDialogData }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateNoticeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});