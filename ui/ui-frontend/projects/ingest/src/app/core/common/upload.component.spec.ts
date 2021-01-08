/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadComponent } from './upload.component';
import { MatProgressBarModule, MAT_DIALOG_DATA, MatDialogRef, MatSnackBarModule } from '@angular/material';
import { of, EMPTY } from 'rxjs';
import { UploadService } from './upload.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { LoggerModule, ConfirmDialogService } from 'ui-frontend-common';

fdescribe('UploadComponent', () => {
  let component: UploadComponent;
  let fixture: ComponentFixture<UploadComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
  matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const uploadServiceSpy = jasmine.createSpyObj('UploadService', { uploadFile: of({}) });

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatProgressBarModule,
        MatSnackBarModule,
        LoggerModule.forRoot()
      ],
      declarations: [UploadComponent],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: UploadService, useValue: uploadServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('initContextIdentifier', () => {

    beforeEach( () => {
      spyOn(console, 'error');
    });

    it('should adapt the message with Holding scheme', () => {
      component.initContextIdentifier('HOLDING_SCHEME');
      expect(component.messageImportType).toEqual('Importer un arbre de positionnement');
      expect(component.messageLabelImportType).toEqual('Nouvel arbre de positionnement');
    });

    it('should adapt the message with Filling scheme', () => {
      component.initContextIdentifier('FILING_SCHEME');
      expect(component.messageImportType).toEqual('Importer un plan de classement');
      expect(component.messageLabelImportType).toEqual('Nouveau plan de classement');
    });

    it('should adapt the message with default ingest', () => {
      component.initContextIdentifier('DEFAULT_WORKFLOW');
      expect(component.messageImportType).toEqual('Verser un SIP');
      expect(component.messageLabelImportType).toEqual('Nouveau versement');
    });

    it('should log error for unknown context', () => {
      component.initContextIdentifier('XYZ');
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('checkFileExtension', () => {

    it('should return true when extension zip is correct', () => {
      expect(component.checkFileExtension('correct.zip')).toBeTruthy();
    });

    it('should return true when extension tar is correct', () => {
      expect(component.checkFileExtension('correct.tar')).toBeTruthy();
    });

    it('should return true when extension .tar.gz is correct', () => {
      expect(component.checkFileExtension('correct.tar.gz')).toBeTruthy();
    });

    it('should return true when extension .tar.bz2 is correct', () => {
      expect(component.checkFileExtension('correct.tar.bz2')).toBeTruthy();
    });

    it('should return true when extension is bad', () => {
      expect(component.checkFileExtension('bad.json')).toBeFalsy();
    });
  });
});
