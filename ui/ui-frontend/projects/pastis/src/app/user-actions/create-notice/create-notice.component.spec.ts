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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, LoggerModule, MiscValidators, WINDOW_LOCATION } from 'vitamui-library';
import { PastisConfiguration } from '../../core/classes/pastis-configuration';
import { ProfileService } from '../../core/services/profile.service';

import { PopupService } from '../../core/services/popup.service';
import { CreateNoticeComponent } from './create-notice.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

const matDialogData = jasmine.createSpyObj('MAT_DIALOG_DATA', ['open']);
matDialogData.open.and.returnValue({ afterClosed: () => of(true) });
const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });
const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

describe('CreateNoticeComponent', () => {
  let component: CreateNoticeComponent;
  let fixture: ComponentFixture<CreateNoticeComponent>;

  const popupServiceMock = {
    externalIdentifierEnabled: true,
    btnYesShoudBeDisabled: of(true),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateNoticeComponent],
      imports: [RouterTestingModule, LoggerModule.forRoot(), TranslateModule.forRoot()],
      providers: [
        FormBuilder,
        ProfileService,
        PopupService,
        PastisConfiguration,
        { provide: BASE_URL, useValue: '/pastis-api' },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: matDialogData },
        { provide: PopupService, useValue: popupServiceMock },
        { provide: WINDOW_LOCATION, useValue: window.location },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateNoticeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('applies MiscValidators.requiredIdentifier to identifier control', () => {
    expect(component.form.get('identifier').hasValidator(MiscValidators.requiredIdentifier)).toBeTruthy();
  });
});
