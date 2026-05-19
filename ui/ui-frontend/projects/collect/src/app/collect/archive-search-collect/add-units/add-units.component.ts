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

import { Component, OnInit, TemplateRef, ViewChild, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { finalize, from, Observable, of, switchMap } from 'rxjs';
import {
  CriteriaDataType,
  CriteriaOperator,
  Direction,
  FilingPlanMode,
  PagedResult,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  Transaction,
  Unit,
  ZipFile,
  ZipFileStatus,
  ApplicationId,
  SnackBarService,
  StartupService,
} from 'vitamui-library';
import { ArchiveCollectService } from '../archive-collect.service';
import { FormControl, Validators } from '@angular/forms';
import { last, tap } from 'rxjs/operators';
import { HttpEventType } from '@angular/common/http';

export enum ImportType {
  DIRECTORIES_FILES = 'DIRECTORIES_FILES',
  COMPRESSED = 'COMPRESSED',
  SIP = 'SIP',
}

@Component({
  selector: 'app-add-units',
  templateUrl: './add-units.component.html',
  styleUrls: ['./add-units.component.scss'],
  standalone: false,
})
export class AddUnitsComponent implements OnInit {
  data = inject<{
    transaction: Transaction;
  }>(MAT_DIALOG_DATA);
  private startupService = inject(StartupService);
  private snackBarService = inject(SnackBarService);
  private dialog = inject(MatDialog);
  private addUnitsDialogRef = inject<MatDialogRef<AddUnitsComponent>>(MatDialogRef);
  private archiveCollectService = inject(ArchiveCollectService);

  protected readonly FilingPlanMode = FilingPlanMode;
  protected readonly uploadMaxSizeInBytes = Math.pow(1024, 3); // 1 Gb

  isLoading = false;
  stepIndex = 0;

  importTypeControl: FormControl<string | null> = new FormControl(null);
  filesToUploadControl: FormControl<File[]> = new FormControl([], [Validators.required]);
  zipFileStatus$: Observable<ZipFileStatus>;
  linkParentIdControl = new FormControl({ included: [], excluded: [] });
  projectUnits: Unit[];

  @ViewChild('confirmCancelDialog', { static: true }) confirmCancelDialogTemplate: TemplateRef<AddUnitsComponent>;
  private confirmCancelDialog: MatDialogRef<AddUnitsComponent>;

  ngOnInit() {
    this.loadAttachementUnits();
  }

  private loadAttachementUnits() {
    const sortingCriteria = { criteria: 'Title', sorting: Direction.ASCENDANT };

    // Exclude units with objects (folders)
    const noObjectCriteria: SearchCriteriaEltDto = {
      criteria: '#object',
      values: [],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.MISSING,
      dataType: CriteriaDataType.STRING,
    };

    // Exclude units with UpdateOperation (static/dynamic attachments or CSV UpdateOperation)
    const noUpdateOperationCriteria: SearchCriteriaEltDto = {
      criteria: '#management.UpdateOperation',
      values: [],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.MISSING,
      dataType: CriteriaDataType.STRING,
    };

    const searchCriteria = {
      criteriaList: Array.of(noObjectCriteria, noUpdateOperationCriteria),
      pageNumber: 0,
      size: 100,
      sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
    };
    this.archiveCollectService
      .searchArchiveUnitsByCriteria(searchCriteria, this.data.transaction.id)
      .subscribe((pagedResult: PagedResult) => {
        this.projectUnits = pagedResult.results;
      });
  }

  cancel() {
    if (this.filesToUploadControl.value.length > 0) {
      this.confirmCancelDialog = this.dialog.open(this.confirmCancelDialogTemplate);
    } else {
      this.addUnitsDialogRef.close(false);
    }
  }

  closeConfirmDialog() {
    this.confirmCancelDialog.close(false);
  }

  close(filesUploaded: boolean) {
    this.confirmCancelDialog?.close(true);
    this.addUnitsDialogRef.close(filesUploaded);
  }

  validateAndUpload() {
    this.isLoading = true;
    this.stepIndex++;
    const zipFile = new ZipFile(this.data.transaction.id);
    this.zipFileStatus$ = zipFile.zipFileStatus$;

    // For COMPRESSED and SIP types, use the file directly without zipping it
    let compressedZip: Blob;
    if ([ImportType.COMPRESSED, ImportType.SIP].includes(this.importType as ImportType)) {
      compressedZip = new Blob(this.filesToUploadControl.value, { type: 'application/zip' });
      zipFile.zipFileStatus.size = compressedZip.size;
      zipFile.zipFileStatus.currentFileUploadedSize = 100;
    }

    from(
      this.importType === ImportType.DIRECTORIES_FILES
        ? zipFile.addFiles(this.filesToUploadControl.value).generateZip()
        : of(compressedZip).toPromise(),
    )
      .pipe(
        switchMap((content) =>
          this.importType === ImportType.SIP
            ? this.archiveCollectService.uploadSip(content, this.data.transaction.id)
            : this.archiveCollectService.uploadZip(content, this.data.transaction.id, this.linkParentIdControl.value.included[0]),
        ),
        tap((httpEvent) => zipFile.updateUploadingZipFileStatus(httpEvent)),
        last((httpEvent) => httpEvent.type === HttpEventType.Response),
        tap((httpEvent) => {
          if (httpEvent.type === HttpEventType.Response) {
            if (this.importType === ImportType.SIP) {
              this.handleUploadSuccess(httpEvent.body);
            } else {
              // For ZIP and DIRECTORIES_FILES, show success banner without operation ID
              this.handleUploadSuccess(null);
            }
          }
        }),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        error: (e) => {
          console.error(e);
        },
      });
  }

  resetFilesToImportList() {
    this.filesToUploadControl.setValue([]);
    this.filesToUploadControl.clearValidators();
    this.filesToUploadControl.setValidators([Validators.required]);
    this.filesToUploadControl.updateValueAndValidity();
    this.filesToUploadControl.markAsUntouched();
  }

  private handleUploadSuccess(operationId: string | null): void {
    if (operationId) {
      // For SIP imports with operation ID
      const tenantId = this.startupService.getTenantIdentifier();
      this.snackBarService.open({
        message: 'COLLECT.MODAL.IMPORT_SIP_ARCHIVES_PACKAGE_WITH_SUCCESS',
        buttons: [
          {
            appId: ApplicationId.LOGBOOK_OPERATION_APP,
            path: `/tenant/${tenantId}?guid=${operationId}`,
            label: 'SNACK_BAR.TO_OPERATION_APP',
          },
        ],
        duration: 100_000,
      });
    } else {
      // For ZIP and DIRECTORIES_FILES imports without operation ID
      this.snackBarService.open({
        message: 'COLLECT.UPLOAD.TERMINATED',
        duration: 10_000,
      });
    }
  }

  get importType(): string | null {
    return this.importTypeControl.value;
  }

  protected ImportType = ImportType;
}
