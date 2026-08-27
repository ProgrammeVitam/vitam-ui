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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { BASE_URL, BytesPipe, InjectorModule, LoggerModule, StartupService, WINDOW_LOCATION } from 'vitamui-library';
import { ArchiveService } from '../../archive.service';
import { TransferAcknowledgmentComponent } from './transfer-acknowledgment.component';
import { DecimalPipe } from '@angular/common';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { CdkStep } from '@angular/cdk/stepper';

@Pipe({ name: 'dateTime' })
export class MockDateTimePipe implements PipeTransform {
  transform(value: string = ''): any {
    return value;
  }
}

describe('TransferAcknowledgmentComponent', () => {
  let component: TransferAcknowledgmentComponent;
  let fixture: ComponentFixture<TransferAcknowledgmentComponent>;

  const matDialogRefSpy = {
    close: vi.fn().mockName('MatDialogRef.close'),
  };
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
  };

  const startupServiceStub = {
    getPortalUrl: vi
      .fn()
      .mockName('StartupService.getPortalUrl')
      .mockReturnValue(() => ''),
    getConfigStringValue: vi
      .fn()
      .mockName('StartupService.getConfigStringValue')
      .mockReturnValue(() => ''),
    getReferentialUrl: vi
      .fn()
      .mockName('StartupService.getReferentialUrl')
      .mockReturnValue(() => ''),
  };

  const archiveSearchServiceStub = {
    transferAcknowledgment: vi.fn().mockName('ArchiveService.transferAcknowledgment').mockReturnValue(of('operationId')),
    openSnackBarForWorkflow: vi.fn().mockName('ArchiveService.openSnackBarForWorkflow').mockReturnValue(of({})),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransferAcknowledgmentComponent, CdkStep, InjectorModule, LoggerModule.forRoot(), MockDateTimePipe],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: ArchiveService, useValue: archiveSearchServiceStub },
        DecimalPipe,
        BytesPipe,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TransferAcknowledgmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should call transferAcknowledgment()', () => {
    const archiveService = TestBed.inject(ArchiveService);
    const matDialogRef = TestBed.inject(MatDialogRef);

    const file = new File([''], '');
    component.atrControl.setValue([file]);
    component.data.tenantIdentifier = '42';

    component.applyTransferAcknowledgment();
    expect(archiveService.transferAcknowledgment).toHaveBeenCalledWith('42', file);
    expect(matDialogRef.close).toHaveBeenCalled();
  });

  it('should parseXmlToTransferDetails for valid XML', async () => {
    const xmlOK = new File(
      [
        `<?xml version="1.0" encoding="UTF-8"?>
<ArchiveTransferReply xmlns="fr:gouv:culture:archivesdefrance:seda:v2.3"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="fr:gouv:culture:archivesdefrance:seda:v2.3 seda-2.3/seda-2.3-main.xsd">
    <Date>2024-06-04T12:56:58.824Z</Date>
    <ArchivalAgreement>IC-000001</ArchivalAgreement>
    <ReplyCode>OK</ReplyCode>
    <MessageRequestIdentifier>SIP SEDA de test</MessageRequestIdentifier>
    <ArchivalAgency>
        <Identifier>Identifier4</Identifier>
    </ArchivalAgency>
    <TransferringAgency>
        <Identifier>Identifier5</Identifier>
    </TransferringAgency>
</ArchiveTransferReply>`,
      ],
      'ok.xml',
    );

    const errors = await component.atrContentValidator(xmlOK);
    expect(errors).toBeFalsy();
    expect(component.transfertDetails).toEqual({
      messageRequestIdentifier: 'SIP SEDA de test',
      date: '2024-06-04T12:56:58.824Z',
      archivalAgreement: 'IC-000001',
      archivalAgency: 'Identifier4',
      transferringAgency: 'Identifier5',
      archiveTransferReply: 'OK',
    });
  });

  it('should parseXmlToTransferDetails for XML without ArchiveTransferReply', async () => {
    const xmlNoArchiveTransferReply = new File(['<toto></toto>'], 'xmlNoArchiveTransferReply.xml');

    const errors = await component.atrContentValidator(xmlNoArchiveTransferReply);
    expect(errors).toBeTruthy();
    expect(errors.fileErrors['atrNotValid']).toBeTruthy();
    expect(errors.controlErrors['invalidFiles']).toBeTruthy();
  });

  it('should parseXmlToTransferDetails for invalid XML', async () => {
    const xmlBadFormat = new File(['This is not XML'], 'xmlBadFormat.xml');

    const errors = await component.atrContentValidator(xmlBadFormat);
    expect(errors).toBeTruthy();
    expect(errors.fileErrors['fileBadFormat']).toBeTruthy();
    expect(errors.controlErrors['invalidFiles']).toBeTruthy();
  });

  describe('DOM', () => {
    it('should have 7 lines in the second step', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.text.normal.bold.primary');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(7);
      expect(formTitlesHtmlElements[0].textContent).toContain('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.OPERATION_MESSAGE_IDENTIFIER ');
    });

    it('should have an input file', () => {
      const nativeElement = fixture.nativeElement;
      const elInput = nativeElement.querySelector('input[type=file]');
      expect(elInput).toBeTruthy();
    });

    it('should call MatDialogRef.close', () => {
      const matDialogSpyTest = TestBed.inject(MatDialogRef);
      component.onClose();
      expect(matDialogSpyTest.close).toHaveBeenCalled();
    });

    it('should call close for all open dialogs', () => {
      const matDialogSpyTest = TestBed.inject(MatDialogRef);
      component.onConfirm();
      expect(matDialogSpyTest.close).toHaveBeenCalled();
    });

    it('should have 2 lines in the last step', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.text.medium.bold');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(2);
      expect(formTitlesHtmlElements[0].textContent).toContain('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.ACKNOWLEDGMENT_TRANSFER_REPLY_CODE ');
    });
  });
});
