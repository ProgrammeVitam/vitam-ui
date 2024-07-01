import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import {
  ObjectPurgedPersistentOperationType,
  PurgedPersistentIdentifierDto,
  UnitPurgedPersistentOperationType,
} from '../../../core/api/persistent-identifier-response-dto.interface';
import { TranslateModule } from '@ngx-translate/core';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-purged-persistent-identifier-modal',
  templateUrl: './purged-persistent-identifier-modal.component.html',
  styleUrls: ['./purged-persistent-identifier-modal.component.scss'],
  standalone: true,
  imports: [DatePipe, TranslateModule],
})
export class PurgedPersistentIdentifierModalComponent implements OnInit {
  messageKey: string;

  constructor(
    private dialogRef: MatDialogRef<PurgedPersistentIdentifierDto>,
    @Inject(MAT_DIALOG_DATA) public data: { ark: string; purgedPersistentIdentifier: PurgedPersistentIdentifierDto },
  ) {}

  ngOnInit(): void {
    const type = this.data.purgedPersistentIdentifier.type;
    const operationType = this.data.purgedPersistentIdentifier.operationType;

    const possibleValues = {
      Object: [
        ObjectPurgedPersistentOperationType.TRANSFER_REPLY,
        ObjectPurgedPersistentOperationType.ELIMINATION_ACTION,
        ObjectPurgedPersistentOperationType.DELETE_GOT_VERSIONS,
      ],
      Unit: [UnitPurgedPersistentOperationType.TRANSFER_REPLY, UnitPurgedPersistentOperationType.ELIMINATION_ACTION],
    };

    if ((possibleValues[type] as any)?.includes(operationType)) {
      this.messageKey = `PERSISTENT_IDENTIFIER_SEARCH.MODAL.${type.toUpperCase()}_${operationType}_MESSAGE`;
    } else {
      this.messageKey = `PERSISTENT_IDENTIFIER_SEARCH.MODAL.${type.toUpperCase()}_UNKNOWN_MESSAGE`;
    }
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
