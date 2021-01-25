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
import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';

import {FileFormat} from 'projects/vitamui-library/src/lib/models/file-format';
import {GlobalEventService, SidenavPage} from 'ui-frontend-common';
import {Referential} from '../shared/vitamui-import-dialog/referential.enum';
import {VitamUIImportDialogComponent} from '../shared/vitamui-import-dialog/vitamui-import-dialog.component';
import {FileFormatCreateComponent} from './file-format-create/file-format-create.component';
import {FileFormatListComponent} from './file-format-list/file-format-list.component';

@Component({
  selector: 'app-file-format',
  templateUrl: './file-format.component.html',
  styleUrls: ['./file-format.component.scss']
})
export class FileFormatComponent extends SidenavPage<FileFormat> implements OnInit {

  search = '';

  @ViewChild(FileFormatListComponent, {static: true}) fileFormatListComponentListComponent: FileFormatListComponent;

  constructor(public dialog: MatDialog, route: ActivatedRoute, globalEventService: GlobalEventService) {
    super(route, globalEventService);
  }

  openCreateFileFormatDialog() {
    const dialogRef = this.dialog.open(FileFormatCreateComponent, {panelClass: 'vitamui-modal', disableClose: true});
    dialogRef.afterClosed().subscribe((result) => {
      if (result.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.fileFormatListComponentListComponent) {
      return;
    }
    this.fileFormatListComponentListComponent.searchFileFormatOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {
  }

  showFileFormat(item: FileFormat) {
    this.openPanel(item);
  }

  openFileFormatImportDialog() {
    const dialogRef = this.dialog.open(
      VitamUIImportDialogComponent, {
        panelClass: 'vitamui-modal',
        data: Referential.FILE_FORMAT,
        disableClose: true
      });
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success) {
        this.refreshList();
      }
    });
  }
}
