import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import {
  PurgedPersistentIdentifierDto, PurgedPersistentOperationType
} from '../../../core/api/persistent-identifier-response-dto.interface';

@Component({
  selector: 'app-purged-persistent-identifier-modal',
  templateUrl: './purged-persistent-identifier-modal.component.html',
  styleUrls: ['./purged-persistent-identifier-modal.component.scss']
})
export class PurgedPersistentIdentifierModalComponent implements OnInit {
  readonly PurgedPersistentOperationType = PurgedPersistentOperationType;
  id: string;

  constructor(
    private dialogRef: MatDialogRef<PurgedPersistentIdentifierDto>,
    @Inject(MAT_DIALOG_DATA) public data: PurgedPersistentIdentifierDto
  ) { }

  ngOnInit(): void {
    console.log(JSON.stringify(this.data))
  }

  closeDialog() {
    this.dialogRef.close();
  }

}
