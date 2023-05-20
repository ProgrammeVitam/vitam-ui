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
import { Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL, InjectorModule, LoggerModule, StartupService, WINDOW_LOCATION } from 'ui-frontend-common';
import { ArchiveService } from '../../archive.service';
import { TransferAcknowledgmentComponent } from './transfer-acknowledgment.component';

const translations: any = { TEST: 'Mock translate test' };
class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

@Pipe({ name: 'dateTime' })
export class MockDateTimePipe implements PipeTransform {
  transform(value: string = ''): any {
    return value;
  }
}

describe('TransferAcknowledgmentComponent', () => {
  let component: TransferAcknowledgmentComponent;
  let fixture: ComponentFixture<TransferAcknowledgmentComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

  const startupServiceStub = jasmine.createSpyObj('StartupService', {
    getPortalUrl: () => '',
    getConfigStringValue: () => '',
    getReferentialUrl: () => '',
  });

  const archiveSearchServiceStub = jasmine.createSpyObj('ArchiveService', {
    transferAcknowledgment: of('operationId'),
    openSnackBarForWorkflow: of({}),
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        MatSnackBarModule,
        HttpClientTestingModule,
      ],

      declarations: [TransferAcknowledgmentComponent, MockDateTimePipe],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: ArchiveService, useValue: archiveSearchServiceStub },
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

  it('All the parameters must be initialized', () => {
    // When
    component.initializeParameters();

    // Then
    expect(component.transfertDetailsCode).toBeNull();
    expect(component.isDisabled).toBeFalsy();
    expect(component.hasFileSizeError).toBeFalsy();
    expect(component.isAtrNotValid).toBeFalsy();
    expect(component.transfertDetails).toEqual({});
    expect(component.hasError).toBeFalsy();
    expect(component.message).toBeNull();
  });

  it('Should return true when the file extension is xml ', () => {
    // Given
    const fileName = 'ATR_aeeaaaaaaghduohdabkogamdgezb2taaaaaq.xml';

    // When
    const response: boolean = component.checkFileExtension(fileName);

    // Then
    expect(response).toBeTruthy();
  });

  it('Should return false when the file extension is not xml ', () => {
    // Given
    const fileName = 'SIP-recherche-perf.zip';
    // When
    const response: boolean = component.checkFileExtension(fileName);
    // Then
    expect(response).toBeFalsy();
  });

  it('Should have a tenant identifier ', () => {
    expect(component.data.tenantIdentifier).not.toBeNull();
  });

  it('should the next step be the step number 3', () => {
    // Given
    component.transfertDetails = { archiveTransferReply: 'OK' };
    component.stepIndex = 2;

    // When
    component.goToNextStep();

    // Then
    expect(component.stepIndex).toEqual(3);
  });

  it('should the previous step be the step number 2', () => {
    // Given
    component.stepIndex = 3;
    // When
    component.backToPreviousStep();
    // Then
    expect(component.stepIndex).toEqual(2);
  });

  it('Should return true if the extension file is xml ', () => {
    const contents = 'text for test';
    const blob = new Blob([contents], { type: 'text/plain' });
    const file = new File([blob], 'fileExample.xml', { type: 'text/plain' });
    expect(component.checkFileExtension(file.name)).toBeTruthy();
  });

  it('should call transferAcknowledgment()', () => {
    const archiveService = TestBed.inject(ArchiveService);
    const matDialogRef = TestBed.inject(MatDialogRef);
    component.applyTransferAcknowledgment();
    expect(archiveService.transferAcknowledgment).toHaveBeenCalled();
    expect(matDialogRef.close).toHaveBeenCalled();
  });

  it('should return KO as status()', () => {
    // Given
    component.transfertDetails = { archiveTransferReply: '\n\t\tKO\n\t' };

    // When
    component.goToNextStep();

    // Then
    expect(component.transfertDetailsCode).not.toBeNull();
    expect(component.transfertDetailsCode).toEqual('KO');
  });

  describe('DOM', () => {
    it('should have 7 lines in the second step', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.detail-text');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(7);
      expect(formTitlesHtmlElements[0].textContent).toContain('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.OPERATION_MESSAGE_IDENTIFIER ');
    });

    it('should emit handleFileInput function when a file is added ', () => {
      // Given
      const contents = '<a>test</a>';
      const blob = new Blob([contents], { type: 'text/plain' });
      const file = new File([blob], 'atrFile.xml', { type: 'text/plain' });
      component.fileToUpload = file;
      spyOn(component, 'handleFileInput');
      const nativeElement = fixture.nativeElement;
      const checkBox = nativeElement.querySelector('input[type=file]');

      // When
      checkBox.dispatchEvent(new Event('change'));
      fixture.detectChanges();

      // Then
      expect(component.handleFileInput).toHaveBeenCalled();
    });

    it('should have 8 buttons ', () => {
      const elementBtn = fixture.nativeElement.querySelectorAll('button[type=button]');
      expect(elementBtn.length).toBe(8);
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

    it('should have 3 cdk steps', () => {
      const elementCdkStep = fixture.nativeElement.querySelectorAll('cdk-step');
      expect(elementCdkStep.length).toBe(3);
    });

    it('should call close for all open dialogs', () => {
      const matDialogSpyTest = TestBed.inject(MatDialogRef);
      component.onConfirm();
      expect(matDialogSpyTest.close).toHaveBeenCalled();
    });

    it('should have 2 lines in the last step', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.text-size');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(2);
      expect(formTitlesHtmlElements[0].textContent).toContain('ARCHIVE_SEARCH.TRANSFER_ACKNOWLEDGMENT.ACKNOWLEDGMENT_TRANSFER_REPLY_CODE ');
    });
  });
});
