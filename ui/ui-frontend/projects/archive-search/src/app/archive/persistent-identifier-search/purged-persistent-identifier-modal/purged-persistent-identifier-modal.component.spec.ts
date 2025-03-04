/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { HttpBackend, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
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
      declarations: [PurgedPersistentIdentifierModalComponent],
      imports: [
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
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
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
