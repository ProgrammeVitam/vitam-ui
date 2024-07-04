import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { BASE_URL } from 'vitamui-library';
import { PastisConfiguration } from '../../core/classes/pastis-configuration';

import { PastisPopupOptionComponent } from './pastis-popup-option.component';

const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });
const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

describe('PastisPopupOptionComponent', () => {
  let component: PastisPopupOptionComponent;
  let fixture: ComponentFixture<PastisPopupOptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        ToastrModule.forRoot({
          positionClass: 'toast-bottom-full-width',
          preventDuplicates: false,
          timeOut: 3000,
          closeButton: false,
          easeTime: 0,
        }),
        PastisPopupOptionComponent,
      ],
      providers: [
        PastisConfiguration,
        { provide: BASE_URL, useValue: '/pastis-api' },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PastisPopupOptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
