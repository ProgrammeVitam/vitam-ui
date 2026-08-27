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
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { BASE_URL, Transaction, TransactionStatus, WINDOW_LOCATION } from 'vitamui-library';
import { UpdateUnitsMetadataComponent } from './update-units-metadata.component';

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

  const matDialogRefSpy = {
    close: vi.fn().mockName('MatDialogRef.close'),
  };
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateUnitsMetadataComponent],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: { tenantIdentifier: '15', selectedTransaction } },
        { provide: WINDOW_LOCATION, useValue: window.location },
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
