import { Component } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';

@Component({
  selector: 'design-system-sample-dialog',
  templateUrl: './sample-dialog.component.html',
  styleUrls: ['./sample-dialog.component.scss'],
})
export class SampleDialogComponent {
  public stepIndex = 0;
  public stepCount = 2;

  constructor(public dialogRef: MatDialogRef<SampleDialogComponent>) {}

  cancel() {
    this.dialogRef.close();
  }

  onSubmit() {
    this.dialogRef.close();
  }
}
