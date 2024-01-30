import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpBackend } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { VitamuiMissingTranslationHandler } from 'ui-frontend-common';
import { PurgedPersistentOperationType } from '../../../core/api/persistent-identifier-response-dto.interface';
import { PurgedPersistentIdentifierModalComponent } from './purged-persistent-identifier-modal.component';

class FakeTranslateLoader implements TranslateLoader {
  getTranslation(lang: string): Observable<any> {
    if (lang === 'fr') {
      return of({
        // Add wanted translations
      });
    }

    return of({
      // Add wanted translations
    });
  }
}

describe('ErrorResponseModalComponent', () => {
  let component: PurgedPersistentIdentifierModalComponent;
  let fixture: ComponentFixture<PurgedPersistentIdentifierModalComponent>;
  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'keydownEvents']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot({
          missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
          defaultLanguage: 'fr',
          loader: {
            provide: TranslateLoader,
            useClass: FakeTranslateLoader,
            deps: [HttpBackend],
          },
        }),
      ],
      declarations: [PurgedPersistentIdentifierModalComponent],
      providers: [
        {
          provide: MatDialogRef,
          useValue: matDialogRefSpy,
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            ark: '',
            purgedPersistentIdentifier: {
              id: '',
              tenant: 0,
              version: 0,
              type: '',
              operationId: '',
              operationType: PurgedPersistentOperationType.ELIMINATION_ACTION,
              operationLastPersistentDate: '',
              objectGroupId: '',
              persistentIdentifier: [],
            },
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PurgedPersistentIdentifierModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
