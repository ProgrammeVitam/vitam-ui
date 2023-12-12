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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subscription } from 'rxjs';
import { BreadCrumbData, Logger } from 'ui-frontend-common';
import { CollectUploadFile, CollectZippedUploadFile } from '../../collect/shared/collect-upload/collect-upload-file';
import { CollectUploadService } from '../../collect/shared/collect-upload/collect-upload.service';
import { GetorixDeposit } from '../core/model/getorix-deposit.interface';
import { GetorixDepositService } from '../getorix-deposit.service';

@Component({
  selector: 'getorix-deposit-upload-object',
  templateUrl: './getorix-deposit-upload-object.component.html',
  styleUrls: ['./getorix-deposit-upload-object.component.scss'],
})
export class GetorixDepositUploadObjectComponent implements OnInit, OnDestroy {
  operationIdentifierSubscription: Subscription;
  operationId: string;
  dataBreadcrumb: BreadCrumbData[];
  getorixDepositDetails: GetorixDeposit;
  myList: string[] = ['test1', 'test2', 'test3', 'hello', 'world'];
  isIndeterminate: boolean;
  isAllChecked: boolean;

  // upload
  uploadFiles$: Observable<CollectUploadFile[]>;
  zippedFile$: Observable<CollectZippedUploadFile>;
  @ViewChild('fileSearch', { static: false }) fileSearch: any;
  hasDropZoneOver = false;
  hasError = false;

  constructor(
    private route: ActivatedRoute,
    private getorixDepositService: GetorixDepositService,
    private router: Router,
    private translateService: TranslateService,
    private loggerService: Logger,
    private uploadService: CollectUploadService,
    private snackBar: MatSnackBar,
    private translationService: TranslateService,
    private logger: Logger
  ) {}

  ngOnInit(): void {
    this.uploadFiles$ = this.uploadService.getUploadingFiles();
    this.zippedFile$ = this.uploadService.getZipFile();

    this.operationIdentifierSubscription = this.route.params.subscribe((params) => {
      this.operationId = params.operationIdentifier;
      this.dataBreadcrumb = [
        {
          redirectUrl: this.router.url.replace('/create', '').replace('upload-object', '').replace(this.operationId, ''),
          label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.ARCHIVAL_SPACE'),
        },
        {
          label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.NEW_PROJECT'),
          redirectUrl: this.router.url.replace('/upload-object/', '').replace(this.operationId, ''),
        },
        { label: this.translateService.instant('GETORIX_DEPOSIT.BREAD_CRUMB.UPLOAD_ARCHIVES') },
      ];
      this.getorixDepositService.getGetorixDepositById(params.operationIdentifier).subscribe(
        (data: GetorixDeposit) => {
          this.getorixDepositDetails = data;
          // search units of the transactionId
          // create unit-list component
        },
        (error) => {
          this.loggerService.error('error while searching for this operation', error);
          this.router.navigate([this.router.url.replace('/create', '').replace('upload-object', '').replace(this.operationId, '')]);
        }
      );
    });
  }

  goToUpdateOperation() {
    this.router.navigate([this.router.url.replace('/upload-object/', '').replace(this.operationId, '')], {
      queryParams: { operationId: this.operationId },
    });
  }
  showComments() {
    console.log('show comments');
  }

  ngOnDestroy() {
    this.operationIdentifierSubscription?.unsubscribe();
  }

  addNewObject() {
    console.log('add new object');
  }

  editUnits() {
    console.log('edit units');
  }

  deleteUnits() {
    console.log('delete units');
  }

  reclassifyUnits() {
    console.log('reclassify units');
  }

  notifyArchivist() {
    console.log('send notification to the archivist');
  }

  checkParentBoxChange(event: any) {
    if (event.target.checked) {
      this.isAllChecked = true;
    } else {
      this.isAllChecked = false;
    }
  }

  checkChildrenBoxChange(id: string, event: any) {
    console.log('element', id);
    const action = event.target.checked;

    if (this.isAllChecked && !action) {
      this.isIndeterminate = true;
    } else {
      if (action) {
        this.isIndeterminate = false;
      }
    }
  }

  onDragOver(event: any) {
    event.preventDefault();
    this.hasDropZoneOver = true;
  }

  onDragLeave(event: any) {
    event.preventDefault();
    this.hasDropZoneOver = false;
  }

  async onDropped(event: any) {
    this.hasDropZoneOver = false;
    event.preventDefault();
    const items = event.dataTransfer.items;
    const exists = this.uploadService.directoryExistInZipFile(items, true);
    if (exists) {
      this.snackBar.open(this.translationService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.UPLOAD_FILE_ALREADY_IMPORTED'), null, {
        duration: 3000,
      });
      return;
    }
    await this.uploadService.handleDragAndDropUpload(items).then(() => {
      return this.sendFilesToVitam();
    });
  }

  async handleFile(event: any) {
    event.preventDefault();
    const items = event.target.files;
    const exists = this.uploadService.directoryExistInZipFile(items, false);
    if (exists) {
      this.snackBar.open(this.translationService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.UPLOAD_FILE_ALREADY_IMPORTED'), null, {
        duration: 3000,
      });
      return;
    }
    await this.uploadService
      .handleUpload(items)

      .then(() => {
        return this.sendFilesToVitam();
      });
  }

  addFolder() {
    this.fileSearch.nativeElement.click();
  }

  removeFolder(file: CollectUploadFile) {
    this.uploadService.removeFolder(file);
  }

  deleteAllFiles() {
    this.uploadFiles$.subscribe((data) => {
      data.forEach((file) => {
        this.removeFolder(file);
      });
    });
  }

  async sendFilesToVitam() {
    return this.uploadService
      .uploadZip(this.getorixDepositDetails.tenantIdentifier, this.getorixDepositDetails.transactionId)
      .then((uploadOperation) => {
        uploadOperation.subscribe(
          () => {},
          (error: any) => {
            this.logger.error('Error while uploading files to Vitam', error);
          },
          () => {
            this.snackBar.open(this.translationService.instant('GETORIX_DEPOSIT.UPLOAD_ARCHIVES.TERMINATED'), null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
            });
          }
        );
      })
      .catch((error) => {
        this.logger.error('Error while uploading files to Vitam', error);
      });
  }
}
