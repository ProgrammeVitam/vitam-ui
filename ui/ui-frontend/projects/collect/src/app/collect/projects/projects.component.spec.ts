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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { of, Subject } from 'rxjs';
import { LoggerModule } from 'vitamui-library';
import { ProjectsService } from './projects.service';
import { ProjectsComponent } from './projects.component';

describe('ProjectsComponent', () => {
  let component: ProjectsComponent;
  let fixture: ComponentFixture<ProjectsComponent>;

  const projectsServiceStub = {
    pageEvent: new Subject<string>(),
    tenantEvent: new Subject<string>(),
    customerEvent: new Subject<string>(),
  };

  const activatedRouteStub = {
    params: of({ tenantIdentifier: '1' }),
  };

  const matDialogStub = {
    open: vi.fn().mockName('MatDialog.open'),
  };

  const transactionsToProcessListComponentStub = {
    searchTransactions: vi.fn().mockName('TransactionsToProcessListComponent.searchTransactions'),
    searchByQuery: vi.fn().mockName('TransactionsToProcessListComponent.searchByQuery'),
  };

  const projectListComponentStub = {
    search: vi.fn().mockName('ProjectListComponent.search'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [ProjectsComponent, LoggerModule.forRoot()],
      providers: [
        { provide: ProjectsService, useValue: projectsServiceStub },
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: MatDialog, useValue: matDialogStub },
      ],
    })
      // The template renders the real project/transaction list components, which have their own heavy
      // dependency trees; it is irrelevant to the business logic under test, so it is stubbed out, and
      // the viewChild signals it drives are stubbed directly instead.
      .overrideComponent(ProjectsComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(ProjectsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    (component as unknown as { projectListComponent: () => unknown }).projectListComponent = () => projectListComponentStub;
    (component as unknown as { transactionsToProcessListComponent: () => unknown }).transactionsToProcessListComponent = () =>
      transactionsToProcessListComponentStub;
    // CreateProjectComponent (opened as the dialog's content) pulls in MatDialogModule, which registers its own
    // MatDialog provider in a standalone-component environment injector closer than the TestBed override: assign
    // the stub directly rather than relying on DI to pick it up.
    (component as unknown as { dialog: unknown }).dialog = matDialogStub;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('openCreateProjectDialog', () => {
    it('should re-search the transactions-to-process tab once the create dialog is closed', () => {
      matDialogStub.open.mockReturnValue({ afterClosed: () => of(undefined) });

      component.openCreateProjectDialog();

      expect(transactionsToProcessListComponentStub.searchTransactions).toHaveBeenCalled();
    });

    it('should not fail when the transactions-to-process tab is not rendered', () => {
      matDialogStub.open.mockReturnValue({ afterClosed: () => of(undefined) });
      (component as unknown as { transactionsToProcessListComponent: () => unknown }).transactionsToProcessListComponent = () => undefined;

      expect(() => component.openCreateProjectDialog()).not.toThrow();
    });
  });

  describe('onSearch', () => {
    it('should forward the query to the transactions-to-process tab', () => {
      component.onSearch('my query');

      expect(transactionsToProcessListComponentStub.searchByQuery).toHaveBeenCalledWith('my query');
    });

    it('should not fail when the transactions-to-process tab is not rendered', () => {
      (component as unknown as { transactionsToProcessListComponent: () => unknown }).transactionsToProcessListComponent = () => undefined;

      expect(() => component.onSearch('my query')).not.toThrow();
    });
  });
});
