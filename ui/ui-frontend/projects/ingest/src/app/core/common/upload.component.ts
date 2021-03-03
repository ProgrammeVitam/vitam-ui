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
import { Component, OnInit, ViewChild, Inject } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { BytesPipe, Logger } from 'ui-frontend-common';
import { UploadService } from './upload.service';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';


const LAST_STEP_INDEX = 2;
const action = 'RESUME';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})

export class UploadComponent implements OnInit {

  sipForm: FormGroup;
  hasSip: boolean;
  hasDropZoneOver = false;
  fileToUpload: File = null;
  hasError = false;
  message: string;
  fileName: string;
  fileSize = 0;
  fileSizeString: string;
  extensions: string[];
  contextId: string;
  messageImportType: string;
  messageLabelImportType: string;
  tenantIdentifier: string;
  uploadComplete = false;
  isDisabled = true;
  public stepIndex = 0;
  public stepCount = 2;
  

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<UploadComponent>,
    private formBuilder: FormBuilder,
    private uploadService: UploadService,
    private snackBar: MatSnackBar,
    public logger: Logger
  ) {
    this.sipForm = this.formBuilder.group({
      hasSip: null
    });

    this.tenantIdentifier = data.tenantIdentifier;
  }

  ngOnInit() {
    this.contextId = this.data.givenContextId;
    this.extensions = ['.zip', '.tar', '.tar.gz', '.tar.bz2'];
    this.sipForm.get('hasSip').setValue(true);
    this.hasSip = this.sipForm.get('hasSip').value;

  }
 

  onDragOver(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onDragLeave(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onDropped(files: FileList) {
    this.hasDropZoneOver = false;
    this.handleFile(files);
  }

  handleFile(files: FileList) {
    this.isDisabled = false;
    this.hasError = false;
    this.message = null;
    this.fileToUpload = files.item(0);

    this.fileName = this.fileToUpload.name;
    this.fileSize = this.fileToUpload.size;

    const transformer = new BytesPipe(this.logger);
    this.fileSizeString = transformer.transform(this.fileSize);

    if (!this.checkFileExtension(this.fileName)) {
      this.message = 'Le fichier déposé n\'est pas au bon format';
      this.hasError = true;
      return;
    }
    this.stepIndex = LAST_STEP_INDEX;
  }

  addSip() {
    this.fileSearch.nativeElement.click();
  }

  handleFileInput(files: FileList) {
    this.handleFile(files);
  }

  upload() {
    if (!this.isValidSIP) { return; }

    this.uploadService.uploadFile(this.fileToUpload, this.contextId, action, this.tenantIdentifier)
      .subscribe(
        () => {
          this.dialogRef.close();
          this.displaySnackBar(true);
        },
        (error: any) => {
          console.error(error);
          this.message = error.message;
        });
  }

  displaySnackBar(uploadComplete: boolean) {
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: { type: 'fileUploaded', name: uploadComplete },
      duration: 10000
    });
  }

  isValidSIP() {
    return this.sipForm.get('hasSip').value === false ||
      (this.sipForm.get('hasSip').value === true);
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
