import { HttpBackend } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { VitamuiMissingTranslationHandler } from 'vitamui-library';
import {
  ObjectPurgedPersistentOperationType,
  UnitPurgedPersistentOperationType,
} from '../../../core/api/persistent-identifier-response-dto.interface';
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

  async function init(type: any, operationType: any) {
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
        PurgedPersistentIdentifierModalComponent,
      ],
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
              type: type,
              operationType: operationType,
              operationId: '',
              operationLastPersistentDate: '',
              objectGroupId: '',
              persistentIdentifier: [],
            },
          },
        },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(PurgedPersistentIdentifierModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('init with Object/ELIMINATION_ACTION', () => {
    beforeEach(async () => await init('Object', ObjectPurgedPersistentOperationType.ELIMINATION_ACTION));

    it('should have a correct message key', () => {
      expect(component.messageKey).toBe('PERSISTENT_IDENTIFIER_SEARCH.MODAL.OBJECT_ELIMINATION_ACTION_MESSAGE');
    });
  });

  describe('init with Unit/ELIMINATION_ACTION', () => {
    beforeEach(async () => await init('Unit', UnitPurgedPersistentOperationType.ELIMINATION_ACTION));

    it('should have a correct message key', () => {
      expect(component.messageKey).toBe('PERSISTENT_IDENTIFIER_SEARCH.MODAL.UNIT_ELIMINATION_ACTION_MESSAGE');
    });
  });

  describe('init with incorrect operationType for Object', () => {
    beforeEach(async () => await init('Object', 'INCORRECT'));

    it('should have an "unknown message" message key', () => {
      expect(component.messageKey).toBe('PERSISTENT_IDENTIFIER_SEARCH.MODAL.OBJECT_UNKNOWN_MESSAGE');
    });
  });

  describe('init with incorrect operationType for Unit', () => {
    beforeEach(async () => await init('Unit', ObjectPurgedPersistentOperationType.DELETE_GOT_VERSIONS));

    it('should have an "unknown message" message key', () => {
      expect(component.messageKey).toBe('PERSISTENT_IDENTIFIER_SEARCH.MODAL.UNIT_UNKNOWN_MESSAGE');
    });
  });
});
