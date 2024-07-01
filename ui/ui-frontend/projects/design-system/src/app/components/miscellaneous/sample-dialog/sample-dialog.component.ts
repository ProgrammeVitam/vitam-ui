import { Component } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { TranslateModule } from '@ngx-translate/core';
import { CdkStepperModule } from '@angular/cdk/stepper';
import { CommonProgressBarComponent, StepperComponent } from 'vitamui-library';

@Component({
  selector: 'design-system-sample-dialog',
  templateUrl: './sample-dialog.component.html',
  styleUrls: ['./sample-dialog.component.scss'],
  standalone: true,
  imports: [CommonProgressBarComponent, StepperComponent, CdkStepperModule, TranslateModule],
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
