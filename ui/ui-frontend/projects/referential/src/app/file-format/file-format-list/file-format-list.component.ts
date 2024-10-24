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
import { Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subject, merge } from 'rxjs';
import { debounceTime, filter, takeUntil } from 'rxjs/operators';
import {
  AdminUserProfile,
  ConfirmActionComponent,
  DEFAULT_PAGE_SIZE,
  Direction,
  FILE_FORMAT_EXTERNAL_PREFIX,
  FileFormat,
  InfiniteScrollTable,
  PageRequest,
  StartupService,
  User,
  VitamUISnackBarService,
} from 'vitamui-library';
import { FileFormatService } from '../file-format.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-file-format-list',
  templateUrl: './file-format-list.component.html',
  styleUrls: ['./file-format-list.component.scss'],
})
export class FileFormatListComponent extends InfiniteScrollTable<FileFormat> implements OnDestroy, OnInit {
  // eslint-disable-next-line @angular-eslint/no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  private _searchText: string;

  @Output() fileFormatClick = new EventEmitter<FileFormat>();

  @ViewChild('filterTemplate', { static: false }) filterTemplate: TemplateRef<FileFormatListComponent>;
  @ViewChild('filterButton', { static: false }) filterButton: ElementRef;

  overridePendingChange: true;
  loaded = false;
  orderBy = 'Name';
  direction = Direction.ASCENDANT;
  vitamAdminTenant: number;

  private groups: Array<{ id: string; group: any }> = [];
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<void>();
  private destroy$ = new Subject<void>();

  @Input()
  get connectedUserInfo(): AdminUserProfile {
    return this._connectedUserInfo;
  }

  set connectedUserInfo(userInfo: AdminUserProfile) {
    this._connectedUserInfo = userInfo;
  }

  private _connectedUserInfo: AdminUserProfile;

  constructor(
    public fileFormatService: FileFormatService,
    private matDialog: MatDialog,
    private snackBarService: VitamUISnackBarService,
    private translateService: TranslateService,
    private startupService: StartupService,
  ) {
    super(fileFormatService);
  }

  ngOnInit() {
    this.vitamAdminTenant = +this.startupService.getConfigStringValue('VITAM_ADMIN_TENANT');

    this.fileFormatService
      .search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT))
      .subscribe((data: FileFormat[]) => {
        this.dataSource = data;
      });

    const searchCriteriaChange = merge(this.searchChange, this.orderChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildFileFormatCriteriaFromSearch();
      console.log('query: ', query);
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });

    this.replaceUpdatedFileFormat();
  }

  buildFileFormatCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText.length > 0) {
      criteria.Name = this._searchText;
      criteria.PUID = this._searchText;
    }

    return criteria;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.updatedData.unsubscribe();
  }

  getGroup(user: User) {
    const userGroup = this.groups.find((group) => group.id === user.groupId);
    return userGroup ? userGroup.group : undefined;
  }

  searchFileFormatOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  isInternal(fileFormat: FileFormat): boolean {
    return !fileFormat.puid.startsWith(FILE_FORMAT_EXTERNAL_PREFIX);
  }

  deleteFileFormatDialog(fileFormat: FileFormat) {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'vitamui-confirm-dialog' });

    dialog.componentInstance.objectType = this.translateService.instant('FILE_FORMATS.HOME.FILE_FORMAT');
    dialog.componentInstance.objectName = fileFormat.puid;

    dialog
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.snackBarService.open({
          message: 'SNACKBAR.FILE_FORMAT_CONTRACT_DELETING',
          translateParams: {
            param1: fileFormat.puid,
          },
          duration: 5000,
          icon: 'vitamui-icon-admin-key',
        });
        this.fileFormatService.delete(fileFormat).subscribe(() => {
          this.searchFileFormatOrdered();
        });
      });
  }

  private replaceUpdatedFileFormat(): void {
    this.fileFormatService.updated.pipe(takeUntil(this.destroy$)).subscribe((ffUpdated: FileFormat) => {
      const index = this.dataSource.findIndex((item: FileFormat) => item.id === ffUpdated.id);
      if (index !== -1) {
        this.dataSource[index] = ffUpdated;
      }
    });
  }
}
