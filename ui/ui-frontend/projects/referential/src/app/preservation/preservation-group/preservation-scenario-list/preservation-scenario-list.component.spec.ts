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

import { HttpErrorResponse, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatDialog } from '@angular/material/dialog';
import { EMPTY, of, throwError } from 'rxjs';
import type { Mock, MockInstance } from 'vitest';
import { BASE_URL, PreservationScenario, PreservationScenariosService, SnackBarService } from 'vitamui-library';
import { LoggerModule } from 'vitamui-library';

import { PreservationScenarioListComponent } from './preservation-scenario-list.component';

const OPERATION_ID = 'aeeaaaaaacaaaaaaabcdefghijklmnopq';

const SCENARIO: PreservationScenario = {
  Identifier: 'PSC-000001',
  Name: 'Scenario 1',
  Description: '',
  CreationDate: new Date(),
  LastUpdate: new Date(),
  ActionList: [],
  GriffinByFormat: [],
  DefaultGriffin: null,
  TransformationRules: '',
};

describe('PreservationScenarioListComponent', () => {
  let component: PreservationScenarioListComponent;
  let fixture: ComponentFixture<PreservationScenarioListComponent>;

  let preservationScenariosService: { list: Mock; delete: Mock };
  let snackBarService: { open: Mock; buildSnackBarForOperationsLog: Mock };
  let matDialogOpen: MockInstance<MatDialog['open']>;

  /** Makes the confirmation dialog close with the given result. */
  function whenDialogClosesWith(confirmed: boolean) {
    matDialogOpen.mockReturnValue({ afterClosed: () => of(confirmed) } as any);
  }

  beforeEach(async () => {
    preservationScenariosService = {
      list: vi.fn().mockReturnValue(of([SCENARIO])),
      delete: vi.fn().mockReturnValue(of({ operationId: OPERATION_ID })),
    };

    snackBarService = { open: vi.fn(), buildSnackBarForOperationsLog: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [PreservationScenarioListComponent, LoggerModule.forRoot()],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: PreservationScenariosService, useValue: preservationScenariosService },
        { provide: SnackBarService, useValue: snackBarService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PreservationScenarioListComponent);
    component = fixture.componentInstance;

    // The component imports VitamUICommonModule, which pulls in MatDialogModule and its own MatDialog provider.
    // That provider shadows any testing-module one, so the real instance is spied on instead of being replaced.
    matDialogOpen = vi.spyOn(fixture.debugElement.injector.get(MatDialog), 'open');
    whenDialogClosesWith(true);

    component.searchText = '';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the delete button whatever the current tenant is', () => {
    expect(fixture.debugElement.query(By.css('.delete-button button'))).not.toBeNull();
  });

  describe('deletePreservationScenarioDialog', () => {
    it('should delete the scenario once the deletion is confirmed', () => {
      component.deletePreservationScenarioDialog(SCENARIO);

      expect(preservationScenariosService.delete).toHaveBeenCalledExactlyOnceWith(SCENARIO);
    });

    it('should not delete the scenario when the deletion is cancelled', () => {
      whenDialogClosesWith(false);

      component.deletePreservationScenarioDialog(SCENARIO);

      expect(preservationScenariosService.delete).not.toHaveBeenCalled();
    });

    it('should notify the user and reload the scenarios on success', () => {
      preservationScenariosService.list.mockClear();

      component.deletePreservationScenarioDialog(SCENARIO);

      expect(snackBarService.buildSnackBarForOperationsLog).toHaveBeenCalledExactlyOnceWith(
        'PRESERVATION.SCENARIO.SNACKBAR.DELETE_REQUEST_ACCEPTED',
        OPERATION_ID,
      );
      expect(preservationScenariosService.list).toHaveBeenCalledTimes(1);
    });

    it('should clear the selection when the deleted scenario was selected', () => {
      component.selectedPreservationScenario.set(SCENARIO);

      component.deletePreservationScenarioDialog(SCENARIO);

      expect(component.selectedPreservationScenario()).toBeNull();
    });

    it('should notify the user and keep the list untouched on failure', () => {
      preservationScenariosService.delete.mockReturnValue(
        throwError(() => new HttpErrorResponse({ status: 400, error: { operationId: OPERATION_ID } })),
      );
      preservationScenariosService.list.mockClear();

      component.deletePreservationScenarioDialog(SCENARIO);

      expect(snackBarService.buildSnackBarForOperationsLog).toHaveBeenCalledExactlyOnceWith(
        'PRESERVATION.SCENARIO.SNACKBAR.DELETE_FAILED',
        OPERATION_ID,
      );
      expect(snackBarService.open).toHaveBeenCalledTimes(1);
      expect(preservationScenariosService.list).not.toHaveBeenCalled();
    });

    it('should not call the backend when the dialog is dismissed without a result', () => {
      matDialogOpen.mockReturnValue({ afterClosed: () => EMPTY } as any);

      component.deletePreservationScenarioDialog(SCENARIO);

      expect(preservationScenariosService.delete).not.toHaveBeenCalled();
    });
  });
});
