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
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { By } from '@angular/platform-browser';
import { EMPTY, of } from 'rxjs';
import { BytesPipe, ConfirmDialogService, FileSelectorComponent, LoggerModule, StartupService } from 'vitamui-library';

import { UploadComponent } from './upload.component';
import { UploadService } from './upload.service';
import { DecimalPipe } from '@angular/common';

describe('UploadComponent', () => {
  let component: UploadComponent;
  let fixture: ComponentFixture<UploadComponent>;

  const matDialogRefSpy = {
    open: vi.fn().mockName('MatDialogRef.open'),
  };
  matDialogRefSpy.open.mockReturnValue({ afterClosed: () => of(true) });

  const uploadServiceSpy = {
    uploadFile: vi.fn().mockName('UploadService.uploadFile').mockReturnValue(of({})),
  };

  const configureTestingModule = () =>
    TestBed.configureTestingModule({
      imports: [MatProgressBarModule, LoggerModule.forRoot(), UploadComponent],
      providers: [
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: ConfirmDialogService, useValue: { listenToEscapeKeyPress: () => EMPTY } },
        { provide: UploadService, useValue: uploadServiceSpy },
        { provide: StartupService, useValue: { getReferentialUrl: () => '' } },
        DecimalPipe,
        BytesPipe,
      ],
    });

  describe('without template', () => {
    beforeEach(async () => {
      await configureTestingModule().overrideTemplate(UploadComponent, '<div></div>').compileComponents();
      fixture = TestBed.createComponent(UploadComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });
  });

  describe('SIP file selection', () => {
    beforeEach(async () => {
      await configureTestingModule().compileComponents();
      fixture = TestBed.createComponent(UploadComponent);
      component = fixture.componentInstance;
      await fixture.whenStable();
    });

    const selectFile = async (file: File) => {
      const fileSelector: FileSelectorComponent = fixture.debugElement.query(By.directive(FileSelectorComponent)).componentInstance;
      await fileSelector.handleFilesSelection([file]);
      await fixture.whenStable();
    };
    const uploadButton = (): HTMLButtonElement => fixture.nativeElement.querySelector('mat-dialog-actions button.primary');

    it('should disable the upload button when no file is selected', () => {
      expect(component.sipField().invalid()).toBe(true);
      expect(uploadButton().disabled).toBe(true);
    });

    it('should enable the upload button when a valid SIP is selected', async () => {
      await selectFile(new File(['content'], 'sip.zip'));

      expect(component.sipField().valid()).toBe(true);
      expect(uploadButton().disabled).toBe(false);
    });

    it('should make the form invalid when the file has an invalid extension', async () => {
      await selectFile(new File(['content'], 'sip.txt'));

      expect(component.sipField().invalid()).toBe(true);
      expect(
        component
          .sipField()
          .errors()
          .map((error) => error.kind),
      ).toContain('invalidFiles');
      expect(uploadButton().disabled).toBe(true);
    });
  });
});
