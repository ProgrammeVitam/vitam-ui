import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import {
  PurgedPersistentIdentifierDto,
  PurgedPersistentOperationType,
} from '../../../core/api/persistent-identifier-response-dto.interface';

@Component({
  selector: 'app-purged-persistent-identifier-modal',
  templateUrl: './purged-persistent-identifier-modal.component.html',
  styleUrls: ['./purged-persistent-identifier-modal.component.scss'],
})
export class PurgedPersistentIdentifierModalComponent implements OnInit {
  messageKey: string;

  constructor(
    private dialogRef: MatDialogRef<PurgedPersistentIdentifierDto>,
    @Inject(MAT_DIALOG_DATA) public data: { ark: string; purgedPersistentIdentifier: PurgedPersistentIdentifierDto },
  ) {}

  ngOnInit(): void {
    switch (this.data.purgedPersistentIdentifier.operationType) {
      case PurgedPersistentOperationType.TRANSFER_REPLY:
        this.messageKey = `PERSISTENT_IDENTIFIER_SEARCH.MODAL.TRANSFERRED_MESSAGE`;
        break;
      case PurgedPersistentOperationType.DELETE_GOT_VERSIONS:
      case PurgedPersistentOperationType.ELIMINATION_ACTION:
        this.messageKey = `PERSISTENT_IDENTIFIER_SEARCH.MODAL.DELETED_MESSAGE`;
        break;
    }
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
