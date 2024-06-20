import { GroupService } from './group.service';
/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';

import { DownloadUtils, GlobalEventService, Group, SidenavPage, VitamUISnackBarService } from 'vitamui-library';
import { GroupCreateComponent } from './group-create/group-create.component';
import { GroupListComponent } from './group-list/group-list.component';
import { DownloadSnackBarService } from 'projects/referential/src/app/core/service/download-snack-bar.service';
import moment, { now } from 'moment';

@Component({
  selector: 'app-group',
  templateUrl: './group.component.html',
  styleUrls: ['./group.component.scss'],
})
export class GroupComponent extends SidenavPage<Group> {
  public groups: Group[];
  public search: string;
  public exportButtonDisabled = false;

  @ViewChild(GroupListComponent, { static: true }) groupListComponent: GroupListComponent;

  constructor(
    public route: ActivatedRoute,
    public globalEventService: GlobalEventService,
    private dialog: MatDialog,
    private downloadSnackBarService: DownloadSnackBarService,
    private snackBarService: VitamUISnackBarService,
    private groupService: GroupService,
  ) {
    super(route, globalEventService);
  }

  public openCreateGroupDialog(): void {
    const dialogRef = this.dialog.open(GroupCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.refreshList();
      }
    });
  }

  public onSearchSubmit(search: string): void {
    this.search = search;
  }

  private refreshList(): void {
    if (!this.groupListComponent) {
      return;
    }
    this.groupListComponent.search();
  }

  public exportProfilesAsExcel() {
    this.exportButtonDisabled = true;
    this.downloadSnackBarService.openDownloadBar();

    let exportSub = this.groupService.export().subscribe(
      (response) => {
        const filename = this.getFileName(response.headers.get('Content-Disposition'));
        DownloadUtils.loadFromBlob(response, response.body.type, filename);
        this.downloadSnackBarService.close();
        this.snackBarService.open({ message: 'SHARED.SNACKBAR.GROUP_EXPORT_SUCCESS' });
        this.exportButtonDisabled = false;
      },
      () => {
        this.downloadSnackBarService.close();
        this.exportButtonDisabled = false;
      },
    );

    this.downloadSnackBarService.cancelDownload.subscribe(() => {
      this.exportButtonDisabled = false;
      exportSub.unsubscribe();
    });
  }

  private getFileName(contentDispositionHeader: string): string {
    const match = contentDispositionHeader?.match(/filename=(.+);?/);
    if (match?.length > 1) {
      return match[1];
    }

    const timestamps = moment(now()).format('YYYY-MM-DDTHH_mm_ss');
    return `lexport-groupes-${timestamps}.xlsx`;
  }
}
