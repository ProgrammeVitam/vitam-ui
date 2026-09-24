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
import { FormBuilder } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { environment } from 'projects/archive-search/src/environments/environment';
import { of } from 'rxjs';
import { ConfirmDialogService, InjectorModule, LoggerModule, UsageVersionEnum, WINDOW_LOCATION } from 'vitamui-library';
import { ArchiveApiService } from '../../../../core/api/archive-api.service';
import { TransferRequestModalComponent } from './transfer-request-modal.component';

describe('TransferRequestModalComponent tests', () => {
  let component: TransferRequestModalComponent;
  let fixture: ComponentFixture<TransferRequestModalComponent>;

  const matDialogRefSpy = {
    close: vi.fn().mockName('MatDialogRef.close'),
    keydownEvents: vi.fn().mockName('MatDialogRef.keydownEvents'),
  };
  const matDialogSpy = {
    open: vi.fn().mockName('MatDialog.open'),
  };

  const archiveServiceMock = {
    archive: () => of('test archive'),
    search: () => of([]),
    getAccessContractById: () => of({}),
    transferRequestService: () => of({}),
  };

  const confirmDialogServiceMock = {
    confirm: () => of(true),
    listenToEscapeKeyPress: () => of({}),
    confirmBeforeClosing: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InjectorModule, MatButtonToggleModule, LoggerModule.forRoot(), TransferRequestModalComponent],
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
        { provide: environment, useValue: environment },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ArchiveApiService, useValue: archiveServiceMock },
        { provide: ConfirmDialogService, useValue: confirmDialogServiceMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TransferRequestModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should not call transferRequestService of archiveService when transferRequestFormGroup is invalid', () => {
    // Given
    vi.spyOn(archiveServiceMock, 'transferRequestService');

    // When
    component.onSubmit();

    // Then
    expect(archiveServiceMock.transferRequestService).not.toHaveBeenCalled();
  });

  it('should have correct default values for toggle buttons', () => {
    expect(component.formGroups[1].get('includeLifeCycleLogs').value).toBe(true);
    expect(component.formGroups[1].get('sedaVersion').value).toBe('2.3');
    expect(component.formGroups[1].get('includeObjects').value).toBe(UsageVersionEnum.ALL);
  });

  it('should have "Original numérique" usage with "Initiale" version by default', () => {
    const usage: {
      usage: string;
      version: string;
    } = component.formGroups[1].get('usages').value[0];
    expect(usage.usage).toBe('BinaryMaster');
    expect(usage.version).toBe('FIRST');
  });

  it('should add a usage when asked', () => {
    expect(component.formGroups[1].get('usages').value.length).toBe(1);
    component.addUsage();
    expect(component.formGroups[1].get('usages').value.length).toBe(2);
  });

  it('should remove a usage when asked', () => {
    component.addUsage();
    expect(component.formGroups[1].get('usages').value.length).toBe(2);
    component.removeUsage(1);
    expect(component.formGroups[1].get('usages').value.length).toBe(1);
  });

  describe('DOM', () => {
    it('should have 8 vitamui input', () => {
      const elementVitamuiInput = fixture.nativeElement.querySelectorAll('vitamui-input');
      expect(elementVitamuiInput.length).toBe(8);
    });

    it('should have 3 mat-button-toggle-group', () => {
      expect(fixture.nativeElement.querySelectorAll('mat-button-toggle-group').length).toBe(3);
    });
  });
});
