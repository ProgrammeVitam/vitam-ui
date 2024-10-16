/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, fakeAsync, flush, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import {
  BASE_URL,
  ENVIRONMENT,
  FlowType,
  InjectorModule,
  LoggerModule,
  Project,
  ProjectStatus,
  Transaction,
  TransactionStatus,
  WINDOW_LOCATION,
  Workflow,
} from 'vitamui-library';
import { CollectZippedUploadFile } from '../../shared/collect-upload/collect-upload-file';
import { CollectUploadService } from '../../shared/collect-upload/collect-upload.service';
import { ProjectsService } from '../projects.service';
import { TransactionsService } from '../transactions.service';
import { CreateProjectComponent } from './create-project.component';
import SpyObj = jasmine.SpyObj;

@Pipe({ name: 'fileSize' })
export class MockFileSizePipe implements PipeTransform {
  transform(value: string = ''): any {
    return value;
  }
}

describe('CreateProjectComponent', () => {
  let component: CreateProjectComponent;
  let fixture: ComponentFixture<CreateProjectComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  const defaultProject: Project = {
    id: '',
    name: '',
    archivalAgreement: '',
    messageIdentifier: '',
    archivalAgencyIdentifier: '',
    transferringAgencyIdentifier: '',
    originatingAgencyIdentifier: '',
    submissionAgencyIdentifier: '',
    archivalProfile: '',
    archiveProfile: '',
    acquisitionInformation: '',
    legalStatus: '',
    unitUp: '',
    unitUps: [],
    comment: '',
    status: ProjectStatus.OPEN,
    createdOn: new Date(),
    lastModifyOn: new Date(),
    facets: [],
    tenant: '',
    automaticIngest: false,
  };
  const defaultTransation: Transaction = {
    id: '',
    status: TransactionStatus.OPEN,
    projectId: '',
    archivalAgreement: '',
    messageIdentifier: '',
    archivalAgencyIdentifier: '',
    transferringAgencyIdentifier: '',
    originatingAgencyIdentifier: '',
    submissionAgencyIdentifier: '',
    archiveProfile: '',
    legalStatus: '',
    comment: '',
    acquisitionInformation: '',
    creationDate: new Date(),
    lastUpdate: new Date(),
  };

  let projectsServiceMock: SpyObj<ProjectsService>;
  let transactionServiceMock: SpyObj<TransactionsService>;
  let uploadServiceMock: SpyObj<CollectUploadService>;

  beforeEach(async () => {
    projectsServiceMock = jasmine.createSpyObj<ProjectsService>('ProjectsService', {
      create: of(defaultProject),
      updateProject: of(defaultProject),
    });

    transactionServiceMock = jasmine.createSpyObj<TransactionsService>('TransactionsService', {
      create: of(defaultTransation),
    });

    uploadServiceMock = jasmine.createSpyObj<CollectUploadService>('UploadService', {
      uploadZip: of(of({})).toPromise(), // FIXME: Maybe change promise of observable chain call...
      getUploadingFiles: of([]),
      getZipFile: of({} as CollectZippedUploadFile),
      reinitializeZip: null,
    });

    await TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        InjectorModule,
        TranslateModule.forRoot(),
        MatButtonToggleModule,
        HttpClientTestingModule,
        MatSnackBarModule,
        LoggerModule.forRoot(),
      ],
      declarations: [CreateProjectComponent, MockFileSizePipe],
      providers: [
        FormBuilder,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: WINDOW_LOCATION, useValue: window.location },
        { provide: ProjectsService, useValue: projectsServiceMock },
        { provide: TransactionsService, useValue: transactionServiceMock },
        { provide: CollectUploadService, useValue: uploadServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('stepIndex should be 2', () => {
    component.stepIndex = 1;
    component.moveToNextStep();
    expect(component.stepIndex).toEqual(2);
  });

  it('stepIndex should be 0', () => {
    component.stepIndex = 1;
    component.backToPreviousStep();
    expect(component.stepIndex).toEqual(0);
  });

  it('should call close for all close dialog', () => {
    const matDialogSpyTest = TestBed.inject(MatDialogRef);
    component.onClose();
    expect(matDialogSpyTest.close).toHaveBeenCalled();
  });

  it('should call "create" with project having unitUp and no automatic ingest', fakeAsync(() => {
    // Given
    const form = {
      messageIdentifier: 'abcd',
      linkParentIdControl: {
        included: ['inc'],
      },
    };

    component.selectedWorkflow = Workflow.MANUAL;
    component.selectedFlowType = FlowType.FIX;
    component.projectForm.patchValue(form);

    // When
    component.validateAndCreateProject();

    // Then
    expect(projectsServiceMock.create).toHaveBeenCalled();
    expect(transactionServiceMock.create).toHaveBeenCalled();
    const arg = projectsServiceMock.create.calls.mostRecent().args[0] as Project;
    expect(arg.name).toBe(form.messageIdentifier);
    expect(arg.messageIdentifier).toBe(form.messageIdentifier);
    expect(arg.unitUp).toBe(form.linkParentIdControl.included[0]);
    expect(arg.unitUps).toBeUndefined();
    expect(arg.automaticIngest).toBeFalsy();

    flush();
  }));

  it('should call "create" with project having no unitUp and automatic ingest', () => {
    // Given
    const form = {
      messageIdentifier: 'abcd',
      linkParentIdControl: {
        included: ['inc'],
      },
      automaticIngest: true,
    };

    component.selectedWorkflow = Workflow.FLOW;
    component.selectedFlowType = FlowType.RULES;
    component.projectForm.patchValue(form);

    // When
    component.validateAndCreateProject();

    // Then
    expect(projectsServiceMock.create).toHaveBeenCalled();
    expect(transactionServiceMock.create).not.toHaveBeenCalled();
    const arg = projectsServiceMock.create.calls.mostRecent().args[0] as Project;
    expect(arg.name).toBe(form.messageIdentifier);
    expect(arg.messageIdentifier).toBe(form.messageIdentifier);
    expect(arg.unitUp).toBeUndefined();
    expect(arg.automaticIngest).toBeTruthy();
  });

  describe('DOM', () => {
    it('should have an input file', () => {
      const nativeElement = fixture.nativeElement;
      const elInput = nativeElement.querySelector('input[type=file]');
      expect(elInput).toBeTruthy();
    });

    it('should have 3 cdk steps', () => {
      const elementCdkStep = fixture.nativeElement.querySelectorAll('cdk-step');
      expect(elementCdkStep.length).toBe(6);
    });

    it('should have 13 VitamUI Common Input', () => {
      const elementCdkStep = fixture.nativeElement.querySelectorAll('vitamui-common-input');
      expect(elementCdkStep.length).toBe(13);
    });
  });
});
