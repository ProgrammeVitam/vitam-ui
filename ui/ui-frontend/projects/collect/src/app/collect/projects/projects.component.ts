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
import { Component, OnDestroy, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { DEFAULT_PAGE_SIZE, PageRequest } from '../../../../../vitamui-library/src/app/modules/vitamui-table/page-request.model';
import { Direction } from '../../../../../vitamui-library/src/app/modules/vitamui-table/direction.enum';
import { SidenavPage } from '../../../../../vitamui-library/src/app/modules/sidenav-page.class';
import { CreateProjectComponent } from './create-project/create-project.component';
import { ProjectListComponent } from './project-list/project-list.component';
import { ProjectsService } from './projects.service';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss'],
  standalone: false,
})
export class ProjectsComponent extends SidenavPage<any> implements OnDestroy {
  private dialog = inject(MatDialog);

  tenantIdentifier: string;
  projectId: string;
  isLPExtended = false;
  createDialogSub: Subscription;

  @ViewChild(ProjectListComponent, { static: true }) projectListComponent: ProjectListComponent;

  constructor() {
    const projectsService = inject(ProjectsService);
    const route = inject(ActivatedRoute);

    super(route, projectsService);
    route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.createDialogSub?.unsubscribe();
  }

  openCreateProjectDialog() {
    const dialogRef = this.dialog.open(CreateProjectComponent, {
      disableClose: true,
      data: {
        tenantIdentifier: this.tenantIdentifier,
      },
    });

    this.createDialogSub = dialogRef.afterClosed().subscribe(() => {
      this.projectListComponent.search(new PageRequest(0, DEFAULT_PAGE_SIZE, 'archivalAgreement', Direction.ASCENDANT));
    });
  }

  openProjectDetailsPanel(selectedProjectId: string) {
    this.projectId = selectedProjectId;
    this.openPanel(selectedProjectId);
  }

  showExtendedLateralPanel() {
    this.isLPExtended = true;
  }

  backToNormalLateralPanel() {
    this.isLPExtended = false;
  }

  onSearch(event: string) {
    const criteria: string = event
      ? JSON.stringify({
          query: event,
        })
      : null;

    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'archivalAgreement', Direction.ASCENDANT, criteria);
    this.projectListComponent.search(pageRequest);
  }
}
