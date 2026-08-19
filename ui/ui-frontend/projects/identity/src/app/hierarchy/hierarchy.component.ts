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
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { GlobalEventService, Profile, SidenavPage, VitamuiBannerComponent, VitamuiTitleBreadcrumbComponent } from 'vitamui-library';
import { HierarchyCreateComponent } from './hierarchy-create/hierarchy-create.component';
import { HierarchyListComponent } from './hierarchy-list/hierarchy-list.component';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { HierarchyDetailComponent } from './hierarchy-detail/hierarchy-detail.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-hierarchy',
  templateUrl: './hierarchy.component.html',
  styleUrls: ['./hierarchy.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    HierarchyDetailComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    HierarchyListComponent,
    TranslatePipe,
  ],
})
export class HierarchyComponent extends SidenavPage<Profile> implements OnInit {
  dialog = inject(MatDialog);
  route: ActivatedRoute;
  override globalEventService: GlobalEventService;

  public profiles: Profile[];
  public search: string;
  private tenantIdentifier: number;

  @ViewChild(HierarchyListComponent, { static: true }) hierarchyListComponent: HierarchyListComponent;

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);

    this.route = route;
    this.globalEventService = globalEventService;
  }

  ngOnInit() {
    this.route.paramMap.subscribe((paramMap) => {
      this.tenantIdentifier = +paramMap.get('tenantIdentifier');
    });
  }

  openHierarchyDuplicateDialog(): void {
    this.dialog
      .open(HierarchyCreateComponent, {
        disableClose: true,
        data: { tenantId: this.tenantIdentifier },
      })
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.refreshList();
        }
      });
  }

  onSearchSubmit(search: string): void {
    this.search = search;
  }

  private refreshList(): void {
    if (this.hierarchyListComponent) {
      this.hierarchyListComponent.search();
    }
  }
}
