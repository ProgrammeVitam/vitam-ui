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
import { Router } from '@angular/router';
import { BehaviorSubject, of } from 'rxjs';
import { AgencyService, Project, ProjectStatus } from 'vitamui-library';
import { ProjectsService } from '../projects.service';
import { ProjectListComponent } from './project-list.component';

describe('ProjectListComponent', () => {
  let component: ProjectListComponent;
  let fixture: ComponentFixture<ProjectListComponent>;

  const buildProject = (overrides: Partial<Project> = {}): Project => ({
    id: 'projectId',
    name: 'name',
    messageIdentifier: 'messageIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    archivalAgreement: 'archivalAgreement',
    archivalProfile: 'archivalProfile',
    unitUp: 'unitUp',
    unitUps: [],
    comment: 'comment',
    status: ProjectStatus.OPEN,
    legalStatus: 'A legal status',
    ...overrides,
  });

  const projectsServiceStub = {
    search: vi.fn().mockName('ProjectsService.search').mockReturnValue(of([])),
    loadMore: vi.fn().mockName('ProjectsService.loadMore').mockReturnValue(of([])),
    getTotalElements: 0,
    selectedProjectId$: new BehaviorSubject<string>(''),
    getUpdatedProject$: vi.fn().mockName('ProjectsService.getUpdatedProject$').mockReturnValue(of(null)),
  };

  const agencyServiceStub = {
    getAll: vi.fn().mockName('AgencyService.getAll').mockReturnValue(of([])),
  };

  const routerStub = {
    navigate: vi.fn().mockName('Router.navigate'),
  };

  beforeEach(async () => {
    projectsServiceStub.search.mockClear();
    projectsServiceStub.loadMore.mockClear();
    agencyServiceStub.getAll.mockClear();

    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [ProjectListComponent],
      providers: [
        { provide: ProjectsService, useValue: projectsServiceStub },
        { provide: AgencyService, useValue: agencyServiceStub },
        { provide: Router, useValue: routerStub },
      ],
    })
      // The template pulls in many vitamui-library components with their own heavy dependency
      // trees; it is irrelevant to the business logic under test, so it is stubbed out.
      .overrideComponent(ProjectListComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(ProjectListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load the agency names on init', () => {
    agencyServiceStub.getAll.mockReturnValueOnce(of([{ identifier: 'agencyId', name: 'Agency Name' }]));

    component.ngOnInit();

    expect(component.getAgencyName('agencyId')).toBe('Agency Name');
  });

  describe('getAgencyName / getAgencyFullLabel', () => {
    it('should combine the identifier and the agency name when it is known', () => {
      component.agencyNames.set(new Map([['agencyId', 'Agency Name']]));
      const label = component.getAgencyFullLabel(buildProject({ submissionAgencyIdentifier: 'agencyId' }));
      expect(label).toBe('agencyId - Agency Name');
    });

    it('should fall back to the identifier alone when the agency name is unknown', () => {
      component.agencyNames.set(new Map());
      const label = component.getAgencyFullLabel(buildProject({ submissionAgencyIdentifier: 'agencyId' }));
      expect(label).toBe('agencyId');
    });
  });
});
