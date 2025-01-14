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

import { Component, Inject, OnInit, TemplateRef, ViewChild } from '@angular/core';
import {
  MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA,
  MatLegacyDialog as MatDialog,
  MatLegacyDialogRef as MatDialogRef,
} from '@angular/material/legacy-dialog';
import { finalize, from, switchMap } from 'rxjs';
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
} from 'vitamui-library';
import { ArchiveCollectService } from '../archive-collect.service';
import { FormControl } from '@angular/forms';
import { last, tap } from 'rxjs/operators';
import { HttpEventType } from '@angular/common/http';

@Component({
  selector: 'app-add-units',
  templateUrl: './add-units.component.html',
  styleUrls: ['./add-units.component.scss'],
})
export class AddUnitsComponent implements OnInit {
  protected readonly FilingPlanMode = FilingPlanMode;
  protected readonly maxSizeInBytes = 10 * Math.pow(1024, 3); // 10 Gb
  public stepIndex = 0;
  public stepCount = 2;

  isLoading = false;

  filesToUpload: File[] = [];
  linkParentIdControl = new FormControl({ included: [], excluded: [] });
  projectUnits: Unit[];

  @ViewChild('confirmCancelDialog', { static: true }) confirmCancelDialogTemplate: TemplateRef<AddUnitsComponent>;
  private confirmCancelDialog: MatDialogRef<AddUnitsComponent>;

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      transaction: Transaction;
    },
    private dialog: MatDialog,
    private addUnitsDialogRef: MatDialogRef<AddUnitsComponent>,
    private archiveCollectService: ArchiveCollectService,
  ) {}

  ngOnInit() {
    this.loadAttachementUnits();
  }

  private loadAttachementUnits() {
    const sortingCriteria = { criteria: 'Title', sorting: Direction.ASCENDANT };
    const criteriaWithId: SearchCriteriaEltDto = {
      criteria: 'DescriptionLevel',
      values: [{ id: 'RecordGrp', value: 'RecordGrp' }],
      category: SearchCriteriaTypeEnum.FIELDS,
      operator: CriteriaOperator.EQ,
      dataType: CriteriaDataType.STRING,
    };
    const searchCriteria = {
      criteriaList: Array.of(criteriaWithId),
      pageNumber: 0,
      size: 100,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: false,
    };
    this.archiveCollectService
      .searchArchiveUnitsByCriteria(searchCriteria, this.data.transaction.id)
      .subscribe((pagedResult: PagedResult) => {
        this.projectUnits = pagedResult.results;
      });
  }

  cancel() {
    if (this.filesToUpload.length > 0) {
      this.confirmCancelDialog = this.dialog.open(this.confirmCancelDialogTemplate, { panelClass: 'vitamui-dialog' });
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

  setFilesToUpload(files: File[]) {
    this.filesToUpload = files;
  }

  validateAndUpload() {
    this.isLoading = true;
    const zipFile = new ZipFile(this.data.transaction.id);
    from(zipFile.addFiles(this.filesToUpload).generateZip())
      .pipe(
        switchMap((content) =>
          this.archiveCollectService.uploadZip(content, this.data.transaction.id, this.linkParentIdControl.value.included[0]),
        ),
        tap((httpEvent) => zipFile.updateUploadingZipFileStatus(httpEvent)),
        last((httpEvent) => httpEvent.type === HttpEventType.Response),
        finalize(() => (this.isLoading = false)),
      )
      .subscribe({
        next: (_v) => {
          this.close(true);
        },
        error: (e) => {
          console.error(e);
        },
      });
  }
}
