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
 */
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Project, SearchService } from 'vitamui-library';
import { ProjectsApiService } from '../core/api/project-api.service';

@Injectable({
  providedIn: 'root',
})
export class ProjectsService extends SearchService<Project> {
  pageEvent = new Subject<string>();
  tenantEvent = new Subject<string>();
  customerEvent = new Subject<string>();

  projectUpdated$ = new BehaviorSubject<Project>(null);

  acquisitionInformationsList = [
    this.translationService.instant('ACQUISITION_INFORMATION.PAYMENT'),
    this.translationService.instant('ACQUISITION_INFORMATION.PROTOCOL'),
    this.translationService.instant('ACQUISITION_INFORMATION.PURCHASE'),
    this.translationService.instant('ACQUISITION_INFORMATION.COPY'),
    this.translationService.instant('ACQUISITION_INFORMATION.DATION'),
    this.translationService.instant('ACQUISITION_INFORMATION.DEPOSIT'),
    this.translationService.instant('ACQUISITION_INFORMATION.DEVOLUTION'),
    this.translationService.instant('ACQUISITION_INFORMATION.DONATION'),
    this.translationService.instant('ACQUISITION_INFORMATION.BEQUEST'),
    this.translationService.instant('ACQUISITION_INFORMATION.REINSTATEMENT'),
    this.translationService.instant('ACQUISITION_INFORMATION.OTHER'),
    this.translationService.instant('ACQUISITION_INFORMATION.UNKNOWN'),
  ];

  legalStatusList = [
    { id: 'Public Archive', value: this.translationService.instant('LEGAL_STATUS.PUBLIC_ARCHIVE') },
    { id: 'Private Archive', value: this.translationService.instant('LEGAL_STATUS.PRIVATE_ARCHIVE') },
    { id: 'Public and Private Archive', value: this.translationService.instant('LEGAL_STATUS.PUBLIC_PRIVATE_ARCHIVE') },
  ];

  constructor(
    private projectsApiService: ProjectsApiService,
    private translationService: TranslateService,
  ) {
    super(projectsApiService, 'ALL');
  }

  public create(project: Project): Observable<any> {
    return this.projectsApiService.create(project);
  }

  public getLegalStatusList() {
    return this.legalStatusList;
  }

  public getAcquisitionInformationsList() {
    return this.acquisitionInformationsList;
  }

  public updateProject(project: Project) {
    return this.projectsApiService.update(project);
  }

  public getProjectById(projectId: string) {
    return this.projectsApiService.getById(projectId);
  }

  public deleteProjectId(projectId: string): Observable<void> {
    return this.projectsApiService.deletebyId(projectId);
  }

  getUpdatedProject$(): BehaviorSubject<Project> {
    return this.projectUpdated$;
  }

  nextUpdatedProject(project: Project) {
    this.projectUpdated$.next(project);
  }
}
