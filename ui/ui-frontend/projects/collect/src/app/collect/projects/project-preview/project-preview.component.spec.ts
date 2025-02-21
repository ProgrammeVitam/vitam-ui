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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { EMPTY, of } from 'rxjs';
import { ProjectsService } from '../projects.service';

import { FormBuilder } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { PaginatedResponse, Project, ProjectStatus, Transaction, TransactionStatus } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { ProjectsApiService } from '../../core/api/project-api.service';
import { ProjectPreviewComponent } from './project-preview.component';

describe('ProjectPreviewComponent', () => {
  let component: ProjectPreviewComponent;
  let fixture: ComponentFixture<ProjectPreviewComponent>;

  const transaction: Transaction = {
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    archivalAgreement: 'archivalAgreement',
    archiveProfile: 'archiveProfile',
    comment: 'comment',
    id: 'id',
    legalStatus: 'legalStatus',
    messageIdentifier: 'messageIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    projectId: 'projectId',
    status: TransactionStatus.OPEN,
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
  };
  const transactionPaginatedResponse: PaginatedResponse<Transaction> = {
    pageNum: 0,
    pageSize: 1,
    totalElements: 1,
    hasMore: false,
    values: [transaction],
  };

  const project: Project = {
    id: 'newId',
    archivalAgreement: 'archivalAgreement',
    messageIdentifier: 'test test',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archivalProfile: 'string',
    unitUp: '878dfdfd',
    comment: 'hello',
    status: ProjectStatus.CLOSE,
  } as Project;

  const projectAfterUpdate = {
    project,
    ...{ messageIdentifier: 'test' },
  };

  const projectServiceMock = {
    getBaseUrl: () => '/fake-api',
    getProjectById: () => of(project),
    updateProject: () => of(projectAfterUpdate),
    getLegalStatusList: () => [
      { id: 'Public Archive', value: 'Public archives' },
      { id: 'Private Archive', value: 'Private archives' },
      { id: 'Public and Private Archive', value: 'Public and private archives' },
    ],
    getAcquisitionInformationsList: () => [
      'Payment',
      'Protocol',
      'Purchase',
      'Copy',
      'Dation',
      'Deposit',
      'Devolution',
      'Donation',
      'Bequest',
      'Reinstatement',
      'Other',
      'Unknown',
    ],
  };

  beforeEach(async () => {
    const projectApiServiceMock = {
      getTransactionsByProjectId: () => of(transactionPaginatedResponse),
      updateTransaction: () => of(transaction),
    };

    await TestBed.configureTestingModule({
      declarations: [ProjectPreviewComponent],
      imports: [MatDialogModule, VitamUICommonTestModule, MatSnackBarModule, BrowserModule, BrowserAnimationsModule, MatButtonToggleModule],
      providers: [
        FormBuilder,
        { provide: ProjectsService, useValue: projectServiceMock },
        {
          provide: MatDialogRef,
          useValue: {
            close: () => {},
          },
        },
        { provide: ProjectsApiService, useValue: projectApiServiceMock },
        { provide: ActivatedRoute, useValue: { params: of('11') } },
        { provide: TranslateService, useValue: { instant: () => EMPTY } },
        { provide: Router, useValue: {} },
      ],
    }).compileComponents();
  });

  beforeEach(async () => {
    fixture = TestBed.createComponent(ProjectPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get project', waitForAsync(() => {
    fixture.whenStable().then(() => {
      // Make assertions about the component
      expect(component.project).toEqual(project);
    });
  }));

  it('should get project when update', waitForAsync(() => {
    component.showEditProject();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.form.value.messageIdentifier).toEqual(project.messageIdentifier);
      expect(component.form.value.legalStatus).toEqual(project.legalStatus);
      expect(component.form.value.archivalAgreement).toEqual(project.archivalAgreement);
      expect(component.form.value.acquisitionInformation).toEqual(project.acquisitionInformation);
      expect(component.form.value.submissionAgencyIdentifier).toEqual(project.submissionAgencyIdentifier);
      expect(component.form.value.originatingAgencyIdentifier).toEqual(project.originatingAgencyIdentifier);
    });
  }));

  it('should update project without transactions', waitForAsync(() => {
    spyOn(projectServiceMock, 'updateProject').and.returnValue(of(projectAfterUpdate));
    component.showEditProject();
    fixture.detectChanges();
    component.form.get('messageIdentifier').setValue(projectAfterUpdate.messageIdentifier);
    component.launchUpdate();
    fixture.detectChanges();
    component.selectedValue = 'NON';
    component.onConfirm();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(projectServiceMock.updateProject).toHaveBeenCalled();
    });
  }));

  it('should update project with transactions', waitForAsync(() => {
    spyOn(projectServiceMock, 'updateProject').and.returnValue(of(projectAfterUpdate));
    component.showEditProject();
    fixture.detectChanges();
    component.form.get('messageIdentifier').setValue(projectAfterUpdate.messageIdentifier);
    component.launchUpdate();
    fixture.detectChanges();
    component.selectedValue = 'YES';
    component.onConfirm();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(projectServiceMock.updateProject).toHaveBeenCalled();
    });
  }));
});
