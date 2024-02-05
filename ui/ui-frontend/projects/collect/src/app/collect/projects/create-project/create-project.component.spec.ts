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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { of } from 'rxjs';
import { BASE_URL, ENVIRONMENT, InjectorModule, LoggerModule, Project, ProjectStatus, WINDOW_LOCATION } from 'ui-frontend-common';
import { FlowType, Workflow } from '../../core/models/create-project.interface';
import { ProjectsService } from '../projects.service';
import { CreateProjectComponent } from './create-project.component';

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

  const projectsServiceMock = jasmine.createSpyObj('ProjectsService', {
    create: () => of({}),
    updateProject: () => of({}),
  });

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
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
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  }));

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

  it('should call create', () => {
    // Given
    const project: Project = {
      id: 'id',
      archivalAgreement: 'string',
      messageIdentifier: 'string',
      archivalAgencyIdentifier: 'id id id',
      transferringAgencyIdentifier: '787878dfdferer454dfd2fd1f21d',
      originatingAgencyIdentifier: 'originatingAgencyIdentifier',
      submissionAgencyIdentifier: 'string',
      archivalProfile: 'test',
      unitUp: 'sdfsdf54sd5f4ds5f4',
      comment: 'test test',
      status: ProjectStatus.OPEN,
    } as Project;

    // When
    component.createdProject = project;
    component.createProjectAndTransactionAndUpload();

    // Then
    expect(projectsServiceMock.create).toHaveBeenCalled();
  });

  it('should createProject with form', () => {
    // Given
    component.selectedWorkflow = Workflow.FLOW;
    component.selectedFlowType = FlowType.FIX;
    component.initForm();
    const project: Project = {
      id: 'id',
      archivalAgreement: 'string',
      messageIdentifier: 'string',
      archivalAgencyIdentifier: 'id id id',
      transferringAgencyIdentifier: '787878dfdferer454dfd2fd1f21d',
      originatingAgencyIdentifier: 'originatingAgencyIdentifier',
      submissionAgencyIdentifier: 'string',
      archivalProfile: 'test',
      unitUp: 'sdfsdf54sd5f4ds5f4',
      comment: 'test test',
      status: ProjectStatus.OPEN,
    } as Project;
    expect(component.stepIndex).toEqual(0);

    // When
    component.createdProject = project;
    component.validateAndCreateProject();

    // Then
    expect(projectsServiceMock.create).toHaveBeenCalled();
    expect(component.stepIndex).toEqual(1);
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
