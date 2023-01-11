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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/archive-search/src/environments/environment';
import { of } from 'rxjs';
import { BASE_URL, ConfirmDialogService, InjectorModule, LoggerModule, StartupService, WINDOW_LOCATION } from 'ui-frontend-common';
import { ArchiveApiService } from '../../../../core/api/archive-api.service';
import { DipRequestCreateComponent } from './dip-request-create.component';

describe('DipRequestCreateComponent', () => {
  let component: DipRequestCreateComponent;
  let fixture: ComponentFixture<DipRequestCreateComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close', 'keydownEvents']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

  const archiveServiceMock = {
    archive: () => of('test archive'),
    search: () => of([]),
    exportDIPService: () => of([]),
    getAccessContractById: () => of({}),
  };

  const confirmDialogServiceMock = {
    confirm: () => of(true),
    listenToEscapeKeyPress: () => of({}),
    confirmBeforeClosing: () => of(),
  };

  const startupServiceStub = {
    getPortalUrl: () => '',
    getConfigStringValue: () => '',
    getReferentialUrl: () => '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DipRequestCreateComponent],
      imports: [
        InjectorModule,
        TranslateModule.forRoot(),
        MatButtonToggleModule,
        HttpClientTestingModule,
        MatSnackBarModule,
        LoggerModule.forRoot(),
      ],
      providers: [
        FormBuilder,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            itemSelected: 30,
            searchCriteria: [],
            accessContract: 'ContratTNR',
            tenantIdentifier: '1',
            selectedItemCountKnown: true,
          },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: environment, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ConfirmDialogService, useValue: confirmDialogServiceMock },
        { provide: ArchiveApiService, useValue: archiveServiceMock },
        { provide: StartupService, useValue: startupServiceStub },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DipRequestCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('items Selected should be grather than 0 ', () => {
    expect(component.itemSelected).toBeGreaterThan(0);
    expect(component.itemSelected).toEqual(30);
  });

  it('should not call exportDIPService of archiveService when exportDIPform is invalid', () => {
    // Given
    spyOn(archiveServiceMock, 'exportDIPService').and.callThrough();

    // When
    component.onSubmit();

    // Then
    expect(archiveServiceMock.exportDIPService).not.toHaveBeenCalled();
  });

  it('Should have an accessContract ', () => {
    expect(component.data.accessContract).toBeDefined();
    expect(component.data.accessContract).not.toBeNull();
    expect(component.data.accessContract).toEqual('ContratTNR');
  });

  it('Should have a tenant identifier ', () => {
    expect(component.data.tenantIdentifier).toBeDefined();
    expect(component.data.tenantIdentifier).not.toBeNull();
    expect(component.data.tenantIdentifier).toEqual('1');
  });

  describe('DOM', () => {
    it('should have 2 text titles', () => {
      const formTitlesHtmlElements = fixture.nativeElement.querySelectorAll('.text-title');

      expect(formTitlesHtmlElements).toBeTruthy();
      expect(formTitlesHtmlElements.length).toBe(2);
      expect(formTitlesHtmlElements[0].textContent).toContain('ARCHIVE_SEARCH.DIP.REQUEST_DIP_EXPORT');
    });

    it('should have 6 vitamui input', () => {
      const elementVitamuiInput = fixture.nativeElement.querySelectorAll('vitamui-common-input');
      expect(elementVitamuiInput.length).toBe(6);
    });
  });
});