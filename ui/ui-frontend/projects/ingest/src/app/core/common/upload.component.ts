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
import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { BytesPipe, StartupService, SnackBarService } from 'vitamui-library';

import { IngestType } from './ingest-type.enum';
import { UploadService } from './upload.service';
import { MatSnackBarRef } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss'],
  standalone: false,
})
export class UploadComponent implements OnInit {
  IngestType = IngestType;

  sipForm: FormGroup;
  hasSip: boolean;
  fileToUpload: File = null;
  hasError = false;
  message: string;
  fileName: string;
  fileSize = 0;
  fileSizeString: string;
  extensions: string[];
  contextId: IngestType;
  tenantIdentifier: string;
  isDisabled = true;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  private snackbarRef: MatSnackBarRef<unknown>;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<UploadComponent>,
    private formBuilder: FormBuilder,
    private uploadService: UploadService,
    private snackBarService: SnackBarService,
    private startupService: StartupService,
    private bytesPipe: BytesPipe,
    private translateService: TranslateService,
  ) {
    this.sipForm = this.formBuilder.group({
      hasSip: null,
    });

    this.tenantIdentifier = data.tenantIdentifier;
  }

  ngOnInit() {
    this.contextId = this.data.givenContextId;
    this.extensions = ['.zip', '.tar', '.tar.gz', '.tar.bz2'];
    this.sipForm.get('hasSip').setValue(true);
    this.hasSip = this.sipForm.get('hasSip').value;
  }

  private isFileList(files: FileList | File[]): files is FileList {
    return files instanceof FileList;
  }

  handleFile(files: FileList | File[]) {
    this.isDisabled = false;
    this.hasError = false;
    this.message = null;
    this.fileToUpload = this.isFileList(files) ? files.item(0) : files[0];

    this.fileName = this.fileToUpload.name;
    this.fileSize = this.fileToUpload.size;

    this.fileSizeString = this.bytesPipe.transform(this.fileSize);

    if (!this.checkFileExtension(this.fileName)) {
      this.message = "Le fichier déposé n'est pas au bon format";
      this.hasError = true;
      return;
    }
  }

  addSip() {
    this.fileSearch.nativeElement.click();
  }

  upload() {
    if (!this.isValidSIP) {
      return;
    }

    this.uploadService
      .uploadIngest(this.tenantIdentifier, this.fileToUpload, this.fileToUpload.name, this.contextId, async (operationId) => {
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
                url: `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${this.tenantIdentifier}?guid=${operationId}`,
                label: this.translateService.instant('SNACK_BAR.TO_OPERATION_APP'),
              },
            ],
          });
        }
      })
      .subscribe(
        async () => {
          this.dialogRef.close();
          this.snackbarRef = await this.snackBarService.open({
            icon: 'vitamui-icon-archive-ingest',
            message: 'INGEST_UPLOAD.ALERTE_MESSAGE',
            translate: true,
          });
        },
        (error: any) => {
          this.message = error.message;
        },
      );
  }

  isValidSIP() {
    return this.sipForm.get('hasSip').value === false || this.sipForm.get('hasSip').value === true;
  }

  onCancel() {
    this.dialogRef.close();
  }

  checkFileExtension(fileName: string): boolean {
    this.fileName = fileName;
    for (const extension of this.extensions) {
      if (fileName.endsWith(extension)) {
        return true;
      }
    }
    return false;
  }
}
