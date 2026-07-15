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

import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatDialog } from '@angular/material/dialog';
import { EMPTY, of, throwError } from 'rxjs';
import type { Mock, MockInstance } from 'vitest';
import { BASE_URL, Griffin, GriffinsService, LoggerModule, SnackBarService, StartupService, TenantSelectionService } from 'vitamui-library';

import { GriffinListComponent } from './griffin-list.component';

const ADMIN_TENANT_IDENTIFIER = 1;
const OTHER_TENANT_IDENTIFIER = 2;

const GRIFFIN: Griffin = {
  '#id': 'id-1',
  '#tenant': 1,
  '#version': 0,
  Identifier: 'GRI-000001',
  Name: 'Griffin 1',
  Description: '',
  ExecutableName: 'griffin',
  ExecutableVersion: '1.0.0',
  CreationDate: new Date(),
  LastUpdate: new Date(),
};

describe('GriffinListComponent', () => {
  let component: GriffinListComponent;
  let fixture: ComponentFixture<GriffinListComponent>;

  let griffinsService: { list: Mock; delete: Mock };
  let snackBarService: { open: Mock };
  let tenantSelectionService: { getSelectedTenant: Mock };
  let matDialogOpen: MockInstance<MatDialog['open']>;

  /** Makes the confirmation dialog close with the given result. */
  function whenDialogClosesWith(confirmed: boolean) {
    matDialogOpen.mockReturnValue({ afterClosed: () => of(confirmed) } as any);
  }

  function createComponent(currentTenantIdentifier: number) {
    tenantSelectionService.getSelectedTenant.mockReturnValue({ identifier: currentTenantIdentifier });

    fixture = TestBed.createComponent(GriffinListComponent);
    component = fixture.componentInstance;

    // The component imports VitamUICommonModule, which pulls in MatDialogModule and its own MatDialog provider.
    // That provider shadows any testing-module one, so the real instance is spied on instead of being replaced.
    matDialogOpen = vi.spyOn(fixture.debugElement.injector.get(MatDialog), 'open');
    whenDialogClosesWith(true);

    component.searchText = '';
    fixture.detectChanges();
  }

  beforeEach(async () => {
    griffinsService = {
      list: vi.fn().mockReturnValue(of([GRIFFIN])),
      delete: vi.fn().mockReturnValue(of(undefined)),
    };

    snackBarService = { open: vi.fn() };
    tenantSelectionService = { getSelectedTenant: vi.fn() };

    const startupService = { getConfigStringValue: vi.fn().mockReturnValue(`${ADMIN_TENANT_IDENTIFIER}`) };

    await TestBed.configureTestingModule({
      imports: [GriffinListComponent, LoggerModule.forRoot()],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: GriffinsService, useValue: griffinsService },
        { provide: SnackBarService, useValue: snackBarService },
        { provide: StartupService, useValue: startupService },
        { provide: TenantSelectionService, useValue: tenantSelectionService },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    createComponent(ADMIN_TENANT_IDENTIFIER);

    expect(component).toBeTruthy();
  });

  describe('delete button visibility', () => {
    it('should be displayed on the vitam administration tenant', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);

      expect(component.canDelete()).toBe(true);
      expect(fixture.debugElement.query(By.css('.delete-button button'))).not.toBeNull();
    });

    it('should be hidden on any other tenant', () => {
      createComponent(OTHER_TENANT_IDENTIFIER);

      expect(component.canDelete()).toBe(false);
      expect(fixture.debugElement.query(By.css('.delete-button button'))).toBeNull();
    });

    it('should be hidden while no tenant is selected yet', () => {
      tenantSelectionService.getSelectedTenant.mockReturnValue(null);

      fixture = TestBed.createComponent(GriffinListComponent);
      component = fixture.componentInstance;
      component.searchText = '';
      fixture.detectChanges();

      expect(component.canDelete()).toBe(false);
    });
  });

  describe('deleteGriffinDialog', () => {
    it('should delete the griffin once the deletion is confirmed', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);

      component.deleteGriffinDialog(GRIFFIN);

      expect(griffinsService.delete).toHaveBeenCalledExactlyOnceWith(GRIFFIN);
    });

    it('should not delete the griffin when the deletion is cancelled', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);
      whenDialogClosesWith(false);

      component.deleteGriffinDialog(GRIFFIN);

      expect(griffinsService.delete).not.toHaveBeenCalled();
    });

    it('should notify the user and reload the griffins on success', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);
      griffinsService.list.mockClear();

      component.deleteGriffinDialog(GRIFFIN);

      expect(snackBarService.open).toHaveBeenCalledExactlyOnceWith(
        expect.objectContaining({ message: 'PRESERVATION.GRIFFIN.SNACKBAR.DELETE_REQUEST_ACCEPTED' }),
      );
      expect(griffinsService.list).toHaveBeenCalledTimes(1);
    });

    it('should clear the selection when the deleted griffin was selected', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);
      component.selectedGriffin.set(GRIFFIN);

      component.deleteGriffinDialog(GRIFFIN);

      expect(component.selectedGriffin()).toBeNull();
    });

    it('should notify the user and keep the list untouched on failure', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);
      griffinsService.delete.mockReturnValue(throwError(() => new Error('Can not remove used griffin(s)')));
      griffinsService.list.mockClear();

      component.deleteGriffinDialog(GRIFFIN);

      expect(snackBarService.open).toHaveBeenCalledExactlyOnceWith(
        expect.objectContaining({ message: 'PRESERVATION.GRIFFIN.SNACKBAR.DELETE_FAILED' }),
      );
      expect(griffinsService.list).not.toHaveBeenCalled();
    });

    it('should not call the backend when the dialog is dismissed without a result', () => {
      createComponent(ADMIN_TENANT_IDENTIFIER);
      matDialogOpen.mockReturnValue({ afterClosed: () => EMPTY } as any);

      component.deleteGriffinDialog(GRIFFIN);

      expect(griffinsService.delete).not.toHaveBeenCalled();
    });
  });
});
