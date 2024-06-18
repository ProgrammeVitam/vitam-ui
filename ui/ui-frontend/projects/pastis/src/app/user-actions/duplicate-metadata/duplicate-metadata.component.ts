import { Component, OnInit } from '@angular/core';
import { MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { PopupService } from '../../core/services/popup.service';
import { PastisDialogConfirmComponent } from '../../shared/pastis-dialog/pastis-dialog-confirm/pastis-dialog-confirm.component';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'duplicate-metadata',
  templateUrl: './duplicate-metadata.component.html',
  styleUrls: ['./duplicate-metadata.component.scss'],
})
export class DuplicateMetadataComponent implements OnInit {
  dataToSend: string;

  constructor(
    public dialogRef: MatDialogRef<PastisDialogConfirmComponent>,
    private popUpService: PopupService,
  ) {}

  ngOnInit(): void {
    this.popUpService.setPopUpDataOnClose(this.dialogRef.componentInstance.dialogReceivedData.fileNode.name);
  }
}
