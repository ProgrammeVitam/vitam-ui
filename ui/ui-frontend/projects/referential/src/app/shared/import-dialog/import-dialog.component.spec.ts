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
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { FileSelectorComponent, FileTypes, LoggerModule, SnackBarService } from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from './import-dialog-param.interface';
import { ImportDialogComponent } from './import-dialog.component';
import { ReferentialImportService } from './referential-import.service';

describe('ImportDialogComponent', () => {
  let fixture: ComponentFixture<ImportDialogComponent>;
  let component: ImportDialogComponent;
  let referentialImportService: { importReferential: ReturnType<typeof vi.fn>; getCSVCheckResults: ReturnType<typeof vi.fn> };

  const fileSelector = (): FileSelectorComponent =>
    fixture.debugElement.query((debugElement) => debugElement.componentInstance instanceof FileSelectorComponent).componentInstance;

  const importButton = (): HTMLButtonElement => fixture.nativeElement.querySelector('mat-dialog-actions .btn.primary');

  async function createComponent(referential: ReferentialTypes) {
    const params: ImportDialogParam = {
      title: 'Title',
      subtitle: 'Subtitle',
      allowedFiles: [FileTypes.CSV],
      referential,
      successMessage: 'Success',
      iconMessage: 'icon',
    };
    referentialImportService = { importReferential: vi.fn(() => of(undefined)), getCSVCheckResults: vi.fn(() => of(undefined)) };
    await TestBed.configureTestingModule({
      imports: [ImportDialogComponent, LoggerModule.forRoot()],
      providers: [
        provideTranslateService(),
        { provide: MAT_DIALOG_DATA, useValue: params },
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        { provide: ReferentialImportService, useValue: referentialImportService },
        { provide: SnackBarService, useValue: { open: vi.fn() } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ImportDialogComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  }

  it('should disable the import button while no file is selected', async () => {
    await createComponent(ReferentialTypes.AGENCY);

    expect(component.uploadForm().invalid()).toBe(true);
    expect(importButton().disabled).toBe(true);
  });

  it('should enable the import button with a valid file and import it', async () => {
    await createComponent(ReferentialTypes.AGENCY);
    const file = new File(['a;b'], 'agencies.csv', { type: 'text/csv' });

    await fileSelector().handleFilesSelection([file]);
    await fixture.whenStable();

    expect(component.uploadForm().valid()).toBe(true);
    expect(importButton().disabled).toBe(false);

    importButton().click();
    expect(referentialImportService.importReferential).toHaveBeenCalledWith(ReferentialTypes.AGENCY, file);
  });

  it('should keep the import button disabled with a file having a wrong extension', async () => {
    await createComponent(ReferentialTypes.AGENCY);

    await fileSelector().handleFilesSelection([new File(['text'], 'agencies.txt', { type: 'text/plain' })]);
    await fixture.whenStable();

    expect(component.uploadForm.files().value().length).toBe(1);
    expect(component.uploadForm().invalid()).toBe(true);
    expect(importButton().disabled).toBe(true);
  });

  it('should keep the import button disabled with a CSV file rejected by the backend check', async () => {
    await createComponent(ReferentialTypes.ACCESS_CONTRACTS);
    referentialImportService.getCSVCheckResults.mockReturnValue(throwError(() => ({ error: {} })));

    await fileSelector().handleFilesSelection([new File(['a;b'], 'contracts.csv', { type: 'text/csv' })]);
    await fixture.whenStable();

    expect(referentialImportService.getCSVCheckResults).toHaveBeenCalled();
    expect(component.uploadForm().invalid()).toBe(true);
    expect(importButton().disabled).toBe(true);
  });
});
