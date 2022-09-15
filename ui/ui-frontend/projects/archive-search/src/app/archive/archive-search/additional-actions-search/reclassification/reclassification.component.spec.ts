import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { of } from 'rxjs';
import {
  BASE_URL,
  InjectorModule,
  LoggerModule,
  StartupService,
  VitamuiMissingTranslationHandler,
  WINDOW_LOCATION,
} from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { ArchiveUnitValidatorService } from '../../../validators/archive-unit-validator.service';

import { ReclassificationComponent } from './reclassification.component';

export function httpLoaderFactory(httpClient: HttpClient): MultiTranslateHttpLoader {
  return new MultiTranslateHttpLoader(httpClient, [
    { prefix: './assets/shared-i18n/', suffix: '.json' },
    { prefix: './assets/i18n/', suffix: '.json' },
  ]);
}

const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

const archiveUnitValidatorServiceMock = {
  alreadyExistParents: () => of(),
  existArchiveUnit: () => of(),
};

const startupServiceStub = {
  getPortalUrl: () => '',
  getConfigStringValue: () => '',
};

describe('ReclassificationComponent', () => {
  let component: ReclassificationComponent;
  let fixture: ComponentFixture<ReclassificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReclassificationComponent],
      imports: [
        InjectorModule,
        VitamUICommonTestModule,
        LoggerModule.forRoot(),
        HttpClientTestingModule,
        TranslateModule.forRoot({
          missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
          defaultLanguage: 'fr',
          loader: {
            provide: TranslateLoader,
            useFactory: httpLoaderFactory,
            deps: [HttpClient],
          },
        }),
        RouterTestingModule,
        BrowserAnimationsModule,
        MatSnackBarModule,
      ],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ArchiveUnitValidatorService, useValue: archiveUnitValidatorServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReclassificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it(' component should be created', () => {
    expect(component).toBeTruthy();
  });
});
