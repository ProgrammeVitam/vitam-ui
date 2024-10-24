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
 *
 *
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL, BytesPipe, InjectorModule, LoggerModule, Transaction, TransactionStatus, WINDOW_LOCATION } from 'vitamui-library';
import { UpdateUnitsMetadataComponent } from './update-units-metadata.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DecimalPipe } from '@angular/common';

const translations: any = { TEST: 'Mock translate test' };
class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

const selectedTransaction: Transaction = {
  id: 'transactionId',
  projectId: 'projectId',
  status: TransactionStatus.OPEN,
  archivalAgreement: 'archivalAgreement',
  messageIdentifier: 'messageIdentifier',
  archivalAgencyIdentifier: 'archivalAgencyIdentifier',
  transferringAgencyIdentifier: 'transferringAgencyIdentifier',
  originatingAgencyIdentifier: 'originatingAgencyIdentifier',
  submissionAgencyIdentifier: 'submissionAgencyIdentifier',
  archiveProfile: 'archivalProfile',
  comment: 'comment',
  legalStatus: 'A legal status',
  acquisitionInformation: 'Protocol',
};

describe('UpdateUaMetadataComponent', () => {
  let component: UpdateUnitsMetadataComponent;
  let fixture: ComponentFixture<UpdateUnitsMetadataComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

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
      ],
      declarations: [UpdateUnitsMetadataComponent],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: { tenantIdentifier: '15', selectedTransaction } },
        { provide: WINDOW_LOCATION, useValue: window.location },
        DecimalPipe,
        BytesPipe,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateUnitsMetadataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  describe('DOM', () => {
    it('should have 1 cdk step', () => {
      const elementCdkStep = fixture.nativeElement.querySelectorAll('cdk-step');
      expect(elementCdkStep.length).toBe(1);
    });

    it('should call close for all open dialogs', () => {
      const matDialogSpyTest = TestBed.inject(MatDialogRef);
      component.onConfirmAction();
      expect(matDialogSpyTest.close).toHaveBeenCalled();
    });

    it('should have an vitamui-file-selector', () => {
      const nativeElement = fixture.nativeElement;
      const el = nativeElement.querySelector('vitamui-file-selector');
      expect(el).toBeTruthy();
    });

    it('should call MatDialogRef.close', () => {
      const matDialogSpyTest = TestBed.inject(MatDialogRef);
      component.onCloseAction();
      expect(matDialogSpyTest.close).toHaveBeenCalled();
    });

    it('should have 2 buttons ', () => {
      const elementBtn = fixture.nativeElement.querySelectorAll('button[type=button]');
      expect(elementBtn.length).toBe(2);
    });
  });
});
