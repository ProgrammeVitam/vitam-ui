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
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { provideTranslateService } from '@ngx-translate/core';
import { FileSelectorComponent, LoggerModule } from 'vitamui-library';
import { LoadProfileComponent, LoadProfileConfig } from './load-profile.component';

describe('LoadProfileComponent', () => {
  let fixture: ComponentFixture<LoadProfileComponent>;
  let component: LoadProfileComponent;

  const config: LoadProfileConfig = {
    title: 'Title',
    subTitle: 'Subtitle',
    okLabel: 'OK',
    cancelLabel: 'Cancel',
    extensions: ['.json', '.rng'],
  };

  const fileSelector = (): FileSelectorComponent =>
    fixture.debugElement.query((debugElement) => debugElement.componentInstance instanceof FileSelectorComponent).componentInstance;

  const okButton = (): HTMLButtonElement => fixture.nativeElement.querySelector('mat-dialog-actions .btn.primary');

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadProfileComponent, LoggerModule.forRoot()],
      providers: [provideTranslateService(), { provide: MAT_DIALOG_DATA, useValue: config }],
    }).compileComponents();
    fixture = TestBed.createComponent(LoadProfileComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should disable the OK button while no file is selected', () => {
    expect(component['files']().valid()).toBe(true);
    expect(okButton().disabled).toBe(true);
  });

  it('should keep the form valid with a file having an allowed extension', async () => {
    const profile = new File(['{}'], 'profile.json', { type: 'application/json' });
    await fileSelector().handleFilesSelection([profile]);
    await fixture.whenStable();

    expect(component['files']().value()).toEqual([profile]);
    expect(component['files']().valid()).toBe(true);
    expect(okButton().disabled).toBe(false);
  });

  it('should make the form invalid with a file having a wrong extension', async () => {
    await fileSelector().handleFilesSelection([new File(['text'], 'profile.txt', { type: 'text/plain' })]);
    await fixture.whenStable();

    expect(component['files']().invalid()).toBe(true);
    expect(
      component['files']()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['invalidFiles']);
  });
});
