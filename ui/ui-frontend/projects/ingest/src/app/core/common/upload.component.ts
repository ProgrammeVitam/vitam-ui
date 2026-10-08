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
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { DialogHeaderComponent, FileSelectorComponent, SnackBarService, StartupService } from 'vitamui-library';

import { IngestType } from './ingest-type.enum';
import { UploadService } from './upload.service';
import { MatSnackBarRef } from '@angular/material/snack-bar';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

const FILE_MAX_SIZE = 10737418240;

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
  imports: [DialogHeaderComponent, MatDialogContent, FileSelectorComponent, ReactiveFormsModule, MatDialogActions, TranslatePipe],
})
export class UploadComponent implements OnInit {
  data = inject(MAT_DIALOG_DATA);
  dialogRef = inject<MatDialogRef<UploadComponent>>(MatDialogRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly uploadService = inject(UploadService);
  private readonly snackBarService = inject(SnackBarService);
  private readonly startupService = inject(StartupService);
  private readonly translateService = inject(TranslateService);

  IngestType = IngestType;

  sipForm: FormGroup;
  extensions = ['.zip', '.tar', '.tar.gz', '.tar.bz2'];
  contextId: IngestType;

  sipControl = new FormControl<File[]>(undefined, [Validators.required]);

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  private snackbarRef: MatSnackBarRef<unknown>;

  constructor() {
    this.sipForm = this.formBuilder.group({
      hasSip: null,
    });
  }

  ngOnInit() {
    this.contextId = this.data.givenContextId;
    this.sipForm.get('hasSip').setValue(true);
  }

  upload() {
    if (!this.isValidSIP) {
      return;
    }

    this.uploadService
      .uploadIngest(
        this.data.tenantIdentifier,
        this.sipControl.value[0],
        this.sipControl.value[0].name,
        this.contextId,
        async (operationId) => {
          this.snackbarRef?.dismiss();
          if (
            this.contextId === IngestType.HOLDING_SCHEME ||
            this.contextId === IngestType.FILING_SCHEME ||
            this.contextId === IngestType.BLANK_TEST
          ) {
            this.snackbarRef = await this.snackBarService.open({
              icon: 'vitamui-icon-archive-ingest',
              message:
                this.contextId === IngestType.BLANK_TEST
                  ? 'INGEST_UPLOAD.BLANK_UPLOAD_COMPLETE_MESSAGE'
                  : 'INGEST_UPLOAD.UPLOAD_COMPLETE_MESSAGE',
              translate: true,
              buttons: [
                {
                  url: `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${this.data.tenantIdentifier}?guid=${operationId}`,
                  label: this.translateService.instant('SNACK_BAR.TO_OPERATION_APP'),
                },
              ],
            });
          }
        },
      )
      .subscribe(async () => {
        this.dialogRef.close();
        this.snackbarRef = await this.snackBarService.open({
          icon: 'vitamui-icon-archive-ingest',
          message: 'INGEST_UPLOAD.ALERTE_MESSAGE',
          translate: true,
        });
      });
  }

  isValidSIP() {
    return this.sipForm.get('hasSip').value === false || this.sipForm.get('hasSip').value === true;
  }

  onCancel() {
    this.dialogRef.close();
  }

  protected readonly FILE_MAX_SIZE = FILE_MAX_SIZE;
}
