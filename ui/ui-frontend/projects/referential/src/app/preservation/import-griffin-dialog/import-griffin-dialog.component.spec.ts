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
import { MatDialogRef } from '@angular/material/dialog';
import { provideTranslateService } from '@ngx-translate/core';
import { FileSelectorComponent, GriffinsService, LoggerModule, SnackBarService } from 'vitamui-library';
import { ImportGriffinDialogComponent } from './import-griffin-dialog.component';

describe('ImportGriffinDialogComponent', () => {
  let fixture: ComponentFixture<ImportGriffinDialogComponent>;

  const fileSelector = (): FileSelectorComponent =>
    fixture.debugElement.query((debugElement) => debugElement.componentInstance instanceof FileSelectorComponent).componentInstance;

  const importButton = (): HTMLButtonElement => fixture.nativeElement.querySelector('mat-dialog-actions .btn.primary');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportGriffinDialogComponent, LoggerModule.forRoot()],
      providers: [
        provideTranslateService(),
        { provide: MatDialogRef, useValue: { close: vi.fn() } },
        { provide: GriffinsService, useValue: { put: vi.fn() } },
        { provide: SnackBarService, useValue: { open: vi.fn() } },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(ImportGriffinDialogComponent);
    await fixture.whenStable();
  });

  it('should disable the import button while no file is selected', () => {
    expect(importButton().disabled).toBe(true);
  });

  it('should enable the import button with a valid JSON file', async () => {
    await fileSelector().handleFilesSelection([new File(['[]'], 'griffins.json', { type: 'application/json' })]);
    await fixture.whenStable();

    expect(importButton().disabled).toBe(false);
  });

  it('should keep the import button disabled with an invalid JSON file', async () => {
    await fileSelector().handleFilesSelection([new File(['{ not json'], 'griffins.json', { type: 'application/json' })]);
    await fixture.whenStable();

    expect(importButton().disabled).toBe(true);
  });
});
