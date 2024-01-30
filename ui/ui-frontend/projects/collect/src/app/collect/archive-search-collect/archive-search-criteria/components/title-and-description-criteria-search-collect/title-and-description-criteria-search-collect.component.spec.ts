/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule } from 'ui-frontend-common';

import { TitleAndDescriptionCriteriaSearchCollectComponent } from './title-and-description-criteria-search-collect.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

const translations: any = { TEST: 'Mock translate test' };
class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('TitleAndDescriptionCriteriaSearchCollectComponent', () => {
  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

  let component: TitleAndDescriptionCriteriaSearchCollectComponent;
  let fixture: ComponentFixture<TitleAndDescriptionCriteriaSearchCollectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatSnackBarModule,
        HttpClientTestingModule,
        RouterTestingModule,
      ],
      declarations: [TitleAndDescriptionCriteriaSearchCollectComponent],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialog, useValue: matDialogSpy },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TitleAndDescriptionCriteriaSearchCollectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should be created', () => {
    expect(component).toBeTruthy();
  });
});
