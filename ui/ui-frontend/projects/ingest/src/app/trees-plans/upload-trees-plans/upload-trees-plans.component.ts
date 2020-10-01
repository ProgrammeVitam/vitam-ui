import { Component, OnInit, ViewChild, Inject } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatSnackBar } from '@angular/material';
import { UploadService } from '../../core/common/upload.service';
import { Logger, BytesPipe } from 'ui-frontend-common';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';

@Component({
  selector: 'app-upload-trees-plans',
  templateUrl: './upload-trees-plans.component.html',
  styleUrls: ['./upload-trees-plans.component.scss']
})
export class UploadTreesPlansComponent implements OnInit {

  treePlanForm: FormGroup;
  hasTreePlan: boolean;
  hasDropZoneOver = false;
  treePlanToUpload: File = null;
  hasError = false;
  message: string;
  fileName: string;
  fileSize = 0;
  fileSizeString: string;
  extensions: string[];
  contextId: string;
  action = 'RESUME';
  HOLDING_SCHEME = 'HOLDING_SCHEME';
  tree = 'arbre de positionnement';
  plan = 'plan de classement';
  messageImportType: string;
  tenantIdentifier: string;

  uploadComplete = false;

  @ViewChild('fileSearch', { static: false }) fileSearch: any;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<UploadTreesPlansComponent>,
    private formBuilder: FormBuilder,
    private uploadService: UploadService,
    private snackBar: MatSnackBar,
    public logger: Logger
  ) {
    this.treePlanForm = this.formBuilder.group({
      hasTreePlan: null
    });

    this.tenantIdentifier = data.tenantIdentifier;
  }

  ngOnInit() {
    this.contextId = this.data.givenContextId;
    console.log('contextId : ', this.contextId);
    this.messageImportType = (this.contextId === this.HOLDING_SCHEME) ? this.tree : this.plan;
    this.extensions = ['.zip', '.tar', '.tar.gz', '.tar.bz2'];
    this.treePlanForm.get('hasTreePlan').setValue(true);
    this.hasTreePlan = this.treePlanForm.get('hasTreePlan').value;
  }

  onTreePlanDragOver(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onTreePlanDragLeave(inDropZone: boolean) {
    this.hasDropZoneOver = inDropZone;
  }

  onTreePlanDropped(files: FileList) {
    this.hasDropZoneOver = false;
    this.handleTreePlanFile(files);
  }

  handleTreePlanFile(files: FileList) {
    this.hasError = false;
    this.message = null;
    this.treePlanToUpload = files.item(0);

    this.fileName = this.treePlanToUpload.name;
    this.fileSize = this.treePlanToUpload.size;

    const transformer = new BytesPipe(this.logger);
    this.fileSizeString = transformer.transform(this.fileSize);

    if (!this.checkFileExtension(this.fileName)) {
      this.message = 'Le fichier déposé n\'est pas au bon format';
      this.hasError = true;
      return;
    }
  }

  addTreeOrPlan() {
    this.fileSearch.nativeElement.click();
  }

  handleFileInput(files: FileList) {
    this.handleTreePlanFile(files);
  }

  upload() {
    if (!this.isValidTreePlan) { return; }

    this.uploadService.uploadFile(this.treePlanToUpload, this.contextId, this.action, this.tenantIdentifier)
      .subscribe(
        (res: boolean) => {
          this.uploadComplete = res;
          if (this.uploadComplete) {
            this.dialogRef.close();
            this.displaySnackBar(res);
          }
        },
        (error) => {
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

  isValidTreePlan() {
    return this.treePlanForm.get('hasTreePlan').value === false ||
      (this.treePlanForm.get('hasTreePlan').value === true);
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
