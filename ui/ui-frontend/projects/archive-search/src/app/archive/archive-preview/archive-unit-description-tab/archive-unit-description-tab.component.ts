/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, EventEmitter, Input, OnDestroy, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar, MatLegacySnackBarConfig as MatSnackBarConfig } from '@angular/material/legacy-snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, pipe, Subscription, UnaryFunction } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import { ArchiveUnit, ArchiveUnitEditorComponent, JsonPatch, Logger, SpinnerOverlayService, StartupService } from 'vitamui-library';
import { EditObject } from 'vitamui-library/app/modules/object-editor/models/edit-object.model';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar/vitamui-snack-bar.component';
import { ArchiveUnitService } from './archive-unit.service';

@Component({
  selector: 'app-archive-unit-description-tab',
  templateUrl: './archive-unit-description-tab.component.html',
  styleUrls: ['./archive-unit-description-tab.component.scss'],
})
export class ArchiveUnitDescriptionTabComponent implements OnDestroy {
  @Input() archiveUnit: ArchiveUnit;
  @Input() editMode = false;
  @Output() editModeChange = new EventEmitter<boolean>();

  @ViewChild('updateDialog') updateDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ArchiveUnitDescriptionTabComponent>;

  archiveUnitEditor: ArchiveUnitEditorComponent;
  editObject: EditObject;

  private readonly subscriptions = new Subscription();
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog', width: '800px', autoFocus: false };
  private readonly snackBarConfig: MatSnackBarConfig = {
    panelClass: 'vitamui-snack-bar',
    data: {
      type: 'WorkflowSuccessSnackBar',
    },
    duration: 100000,
  };

  private notifyFormInvalidityOrContinue: UnaryFunction<Observable<unknown>, Observable<boolean>> = pipe(
    map(() => {
      // Skip validation check when haven't archive unit profile.
      if (!this.archiveUnit.ArchiveUnitProfile) return true;
      // Skip error collect when haven't invalid fields.
      if (this.archiveUnitEditor.editObject$.value.control.valid) return true;

      const invalidLeafErrorsMap = this.collectInvalidNode(this.archiveUnitEditor.editObject$.value)
        .filter((node) => node.children?.length === 0)
        .map((node) => ({
          path: node.path,
          errors: node.control.errors,
        }));

      const isValid = invalidLeafErrorsMap.length === 0;
      if (!isValid) {
        this.logger.warn(this, 'Current form data contains errors', invalidLeafErrorsMap);

        const invalidLeavesMessage = invalidLeafErrorsMap.map((node) => node.path).join(', ');
        const message = this.translateService.instant('ARCHIVE_UNIT.INVALID_FORM', { invalidLeavesMessage });
        this.snackBar.open(message, 'close', this.snackBarConfig);
      }

      return isValid;
    }),
    filter((isValid) => isValid),
  );

  private updateArchiveUnit: UnaryFunction<Observable<unknown>, Observable<{ operationId: String }>> = pipe(
    map(() => this.archiveUnitEditor.getJsonPatch()),
    tap(() => this.spinnerOverlayService.open()),
    switchMap((jsonPatchDto) => this.archiveUnitService.asyncPartialUpdateArchiveUnitByCommands(jsonPatchDto)),
  );

  constructor(
    private logger: Logger,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private startupService: StartupService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private archiveUnitService: ArchiveUnitService,
    private spinnerOverlayService: SpinnerOverlayService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  @ViewChild(ArchiveUnitEditorComponent) set editor(editor: ArchiveUnitEditorComponent) {
    if (!editor) return;

    this.archiveUnitEditor = editor;

    const subscription = this.archiveUnitEditor?.editObject$.subscribe((editObject) => {
      this.editObject = editObject;
    });

    if (subscription) this.subscriptions.add(subscription);
  }

  isModified(): boolean {
    return this.editMode && this.editObject?.control?.dirty;
  }

  collectInvalidNode(editObject: EditObject): EditObject[] {
    let collectedNodes: EditObject[] = [];

    if (editObject.control.invalid) {
      collectedNodes.push(editObject);
    }

    // Utilise `reduce` pour accumuler les nœuds invalides des enfants
    collectedNodes = collectedNodes.concat(editObject.children.reduce((acc, child) => acc.concat(this.collectInvalidNode(child)), []));

    return collectedNodes;
  }

  onCancel() {
    if (!this.isModified()) return this.backToDisplayMode();
    this.subscriptions.add(
      this.dialog
        .open(this.cancelDialog, this.dialogConfig)
        .afterClosed()
        .pipe(
          filter((shouldUpdate: boolean) => Boolean(shouldUpdate || this.backToDisplayMode())),
          this.notifyFormInvalidityOrContinue,
          this.updateArchiveUnit,
        )
        .subscribe({
          next: ({ operationId }) => this.handleUpdateSuccess({ operationId }),
          error: (err) => {
            this.logger.error(this, err);
            this.backToDisplayMode();
          },
        }),
    );
  }

  onSave(): void {
    this.subscriptions.add(
      this.dialog
        .open(this.updateDialog, this.dialogConfig)
        .afterClosed()
        .pipe(
          filter((result: boolean) => result),
          this.notifyFormInvalidityOrContinue,
          this.updateArchiveUnit,
        )
        .subscribe({
          next: ({ operationId }) => this.handleUpdateSuccess({ operationId }),
          error: (err) => {
            this.logger.error(this, err);
            this.spinnerOverlayService.close();
          },
        }),
    );
  }

  private handleUpdateSuccess({ operationId }: { operationId: String }): void {
    const tenantId = this.route.snapshot.params.tenantIdentifier;

    if (!operationId) return this.logger.error(this, 'Operation id is mandatory to build logbook operation link');
    if (!tenantId) return this.logger.error(this, 'Tenant id is mandatory to build logbook operation link');

    const serviceUrl = `${this.startupService.getReferentialUrl()}/logbook-operation/tenant/${tenantId}?guid=${operationId}`;
    const translationKey = 'ARCHIVE_UNIT.DIALOGS.SAVE.MESSAGES.IN_PROGRESS';
    const message = this.translateService.instant(translationKey);

    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      ...this.snackBarConfig,
      data: {
        ...this.snackBarConfig.data,
        message,
        serviceUrl,
      },
    });

    this.patchUnit(this.archiveUnit, this.archiveUnitEditor.getJsonPatch().jsonPatch);

    this.backToDisplayMode();
  }

  private patchUnit(archiveUnit: ArchiveUnit, jsonPatch: JsonPatch) {
    jsonPatch.forEach((patch) => {
      const key = patch.path;
      switch (patch.op) {
        case 'add':
        case 'replace':
          archiveUnit[key] = patch.value;
          break;
        case 'remove':
          delete archiveUnit[key];
          break;
      }
    });
  }

  private backToDisplayMode(): void {
    this.spinnerOverlayService.close();
    this.editMode = false;
    this.editModeChange.emit(this.editMode);
  }
}
