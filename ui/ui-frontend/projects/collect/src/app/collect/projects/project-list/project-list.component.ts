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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { DEFAULT_PAGE_SIZE, Direction, getProjectIcon, InfiniteScrollTable, PageRequest, Project } from 'vitamui-library';
import { ProjectsService } from '../projects.service';

@Component({
  selector: 'app-project-list',
  templateUrl: './project-list.component.html',
  styleUrls: ['./project-list.component.css'],
  standalone: false,
})
export class ProjectListComponent extends InfiniteScrollTable<Project> implements OnDestroy, OnInit {
  @Input() tenantIdentifier: string;
  @Output() previewProjectDetailsPanel: EventEmitter<any> = new EventEmitter();
  selectedProjectId$: Subject<string> = this.projectsService.selectedProjectId$;
  projectUpdated: Subscription;
  getProjectIcon = getProjectIcon;
  // sort columns :
  messageIdentifier: keyof Project = 'messageIdentifier';
  submissionAgencyIdentifier: keyof Project = 'submissionAgencyIdentifier';
  createdOn: keyof Project = 'createdOn';
  lastModifyOn: keyof Project = 'lastModifyOn';
  column = this.messageIdentifier;
  direction = Direction.DESCENDANT;

  constructor(
    public projectsService: ProjectsService,
    private router: Router,
  ) {
    super(projectsService);
  }

  ngOnInit(): void {
    this.searchProject();

    this.projectUpdated = this.projectsService.getUpdatedProject$().subscribe((projectUpdated) => {
      for (let i = 0; i < this.dataSource.length; i++) {
        if (this.dataSource[i].id === projectUpdated.id) {
          this.dataSource[i] = { ...projectUpdated };
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.projectUpdated.unsubscribe();
  }

  changeDirection(direction: Direction) {
    this.direction = direction;
  }

  changeColumn(column: string) {
    this.column = column as keyof Project;
  }

  sortTable() {
    const direction: number = this.direction === Direction.ASCENDANT ? -1 : 1;
    this.dataSource.sort((a, b) => {
      return a[this.column] === b[this.column] ? 0 : a[this.column] > b[this.column] ? direction : -direction;
    });
  }

  searchProject() {
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.column, this.direction);
    super.search(pageRequest);
  }

  onScroll() {
    this.loadMore();
  }

  searchArchiveUnitsByProject(project: Project) {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', project.id]);
  }

  searchTransactions(project: Project) {
    this.router.navigate(['collect/transactions', project.id]);
  }

  showProjectDetails(projectId: string) {
    this.selectedProjectId$.next(projectId);
    this.previewProjectDetailsPanel.emit(projectId);
  }

  stopPropagation(event: Event) {
    event.stopPropagation();
  }
}
