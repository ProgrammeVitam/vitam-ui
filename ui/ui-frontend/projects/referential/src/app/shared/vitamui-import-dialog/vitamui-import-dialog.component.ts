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
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';
import { ReferentialImportService } from './referential-import.service';
import { Referential } from './referential.enum';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-vitamui-import-dialog',
  templateUrl: './vitamui-import-dialog.component.html',
  styleUrls: ['./vitamui-import-dialog.component.scss']
})
export class VitamUIImportDialogComponent implements OnInit {
  stepIndex = 0;
  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 1;
  hasDropZoneOver = false;
  fileToUpload: File = null;
  isfileFormatValid = false;
  isImportInProgress = false;
  referentialEnum = Referential;
  referential: Referential;

  constructor(
    private referentialImportService: ReferentialImportService,
    private snackBar: MatSnackBar,
    public dialogRef: MatDialogRef<VitamUIImportDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Referential) {
  }

  ngOnInit() {
    this.referential = this.data;
  }

  onCancel() {
    this.dialogRef.close();
  }

  onFileDropped(files: FileList) {
    this.hasDropZoneOver = false;
    this.handleFile(files);
  }

  handleFile(files: FileList) {
    this.fileToUpload = files.item(0);

    // Check the file format according to the provided referential
    if ((this.referential === Referential.AGENCY && this.isCsvFile(this.fileToUpload.type)) ||
    (this.referential === Referential.RULE && this.isCsvFile(this.fileToUpload.type)) ||
    (this.referential === Referential.FILE_FORMAT && this.fileToUpload.type === 'text/xml') ||
    (this.referential === Referential.ONTOLOGY && this.fileToUpload.type === 'application/json')) {
      this.isfileFormatValid = true;
    } else {
      this.isfileFormatValid = false;
    }
  }

  private isCsvFile(type: string): boolean {
    return type === 'text/csv'
      || type === 'application/vnd.ms-excel'
      || type ===  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  }

  onFileDragOver(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onFileDragLeave(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  handleFileInput(files: FileList) {
    this.handleFile(files);
  }

  importFile() {
    this.isImportInProgress = true;

    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      duration: 10000,
      data: { type: this.referential + 'ImportInProgress' }
    });

    this.referentialImportService.importReferential(this.referential, this.fileToUpload).subscribe(response => {
      this.isImportInProgress = false;
      const importResult = JSON.parse(response);
      if (importResult.httpCode === 200 || importResult.httpCode === 201) {
        this.snackBar.openFromComponent(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
          data: { type: this.referential + 'ImportSuccessed' }
        });

        this.dialogRef.close({
          success: true
        });
      } else {
        this.snackBar.openFromComponent(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
          data: { type: this.referential + 'ImportFailed' }
        });
      }
    }, () => {
      this.isImportInProgress = false;
    });

  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  get fileSize() {
    if (this.fileToUpload !== null) {
      return (this.fileToUpload.size / 1024).toFixed(0);
    } else {
      return 0;
    }
  }

}
