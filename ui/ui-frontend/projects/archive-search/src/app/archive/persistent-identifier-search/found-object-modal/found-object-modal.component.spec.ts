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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { TranslateLoader } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { LoggerModule, ObjectQualifierType } from 'vitamui-library';
import { FoundObjectModalComponent } from './found-object-modal.component';

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
  let component: FoundObjectModalComponent;
  let fixture: ComponentFixture<FoundObjectModalComponent>;
  const matDialogRefSpy = {
    close: vi.fn().mockName('MatDialogRef.close'),
    keydownEvents: vi.fn().mockName('MatDialogRef.keydownEvents'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot(), FoundObjectModalComponent],
      providers: [
        {
          provide: MatDialogRef,
          useValue: matDialogRefSpy,
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            ark: 'ark_identifier_for_test',
            object: {
              '#id': '',
              '#tenant': '',
              '#unitups': [],
              '#allunitups': [],
              '#operations': [],
              '#opi': '',
              '#originating_agency': '',
              '#originating_agencies': [],
              '#storage': {},
              '#nbobjects': {},
              FileInfo: {},
              '#qualifiers': [
                {
                  qualifier: ObjectQualifierType.BINARYMASTER,
                  '#nbc': 0,
                  versions: [
                    {
                      '#id': 'version_1',
                      DataObjectVersion: 'BinaryMaster_1',
                      PersistentIdentifier: [
                        {
                          PersistentIdentifierContent: 'ark_identifier_for_test',
                        },
                      ],
                    },
                  ],
                },
              ],
              '#approximate_creation_date': '',
              '#approximate_update_date': '',
            },
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FoundObjectModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
