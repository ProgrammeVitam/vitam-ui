import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ArkSearchErrorResponseData, ArkStatus } from './ark-search-error-response-data.interface';

@Component({
  selector: 'app-error-response-modal',
  templateUrl: './error-response-modal.component.html',
  styleUrls: ['./error-response-modal.component.scss']
})
export class ErrorResponseModalComponent implements OnInit {
  readonly ArkStatus = ArkStatus;
  id: string;

  constructor(
    private dialogRef: MatDialogRef<ArkSearchErrorResponseData>,
    @Inject(MAT_DIALOG_DATA) public data: ArkSearchErrorResponseData
  ) { }

  ngOnInit(): void {
    console.log(JSON.stringify(this.data))
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
