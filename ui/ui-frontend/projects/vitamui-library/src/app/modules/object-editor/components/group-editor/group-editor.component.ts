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
import { AfterViewInit, Component, Input, OnChanges, OnDestroy, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { Subscription, of } from 'rxjs';
import { filter, switchMap } from 'rxjs/operators';
import { FavoriteEntryService } from '../../../object-viewer/services/favorite-entry.service';
import { LayoutService } from '../../../object-viewer/services/layout.service';
import { TypeService } from '../../../object-viewer/services/type.service';
import { DisplayObjectType } from '../../../object-viewer/types';
import { Action, EditObject } from '../../models/edit-object.model';

@Component({
  selector: 'vitamui-common-group-editor',
  templateUrl: './group-editor.component.html',
  styleUrls: ['./group-editor.component.scss'],
})
export class GroupEditorComponent implements OnChanges, AfterViewInit, OnDestroy {
  @Input() editObject: EditObject;

  @ViewChild('removeDialog') removeDialog: TemplateRef<GroupEditorComponent>;

  entries: [key: string, value: any][] = [];
  favoriteEntry: [key: string, value: any];
  favoritePath: string;
  rows: EditObject[][] = [[]];
  actionList: Action[] = [];

  readonly DisplayObjectType = DisplayObjectType;
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog', width: '800px', autoFocus: false };
  private subscription: Subscription;

  constructor(
    private layoutService: LayoutService,
    private favoriteEntryService: FavoriteEntryService,
    private typeService: TypeService,
    private matDialog: MatDialog,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    const { editObject } = changes;

    if (editObject) {
      if (this.editObject?.childrenChange) {
        if (this.subscription) this.subscription.unsubscribe();
        this.subscription = this.editObject.childrenChange.subscribe(() => {
          this.computeLayout();
          this.computeActions();
          this.isEmptyObject(editObject.currentValue);
        });
      }

      this.computeLayout();
    }
  }

  ngAfterViewInit(): void {
    this.computeActions();
  }

  ngOnDestroy(): void {
    if (this.subscription) this.subscription.unsubscribe();
  }

  computeLayout() {
    this.favoriteEntry = this.favoriteEntryService.favoriteEntry(this.editObject);
    this.favoritePath = this.favoriteEntryService.favoritePath(this.editObject);
    this.rows = this.layoutService.compute(this.editObject) as EditObject[][];
  }

  computeActions() {
    if (!this.editObject?.actions) return;

    const current: Action = this.editObject.actions.remove;
    if (current) {
      const next: Action = this.withValidation(current, () => this.typeService.isConsistent(this.editObject.control.value));
      this.replaceAction(current, next);
    }

    this.actionList = Object.values(this.editObject.actions);
  }

  toggle(): void {
    this.editObject.open = !this.editObject.open;
  }

  stopPropagation(event: Event) {
    event.stopPropagation();
  }

  isEmptyObject(obj: any): boolean {
    return Object.values(obj).every(
      (value) =>
        value === null || (Array.isArray(value) && value.length === 0) || (typeof value === 'object' && Object.keys(value).length === 0),
    );
  }

  private withValidation(action: Action, predicate: () => boolean): Action {
    const { label, handler } = action;

    return {
      name: `${action.name}WithValidation`,
      label,
      handler: () => {
        if (!predicate()) return handler();

        const subscription: Subscription = this.matDialog
          .open(this.removeDialog, this.dialogConfig)
          .afterClosed()
          .pipe(
            filter((value) => value),
            switchMap(() => of(handler())),
          )
          .subscribe(() => subscription.unsubscribe());
      },
    };
  }

  private replaceAction(current: Action, next: Action): void {
    delete this.editObject.actions[current.name];
    this.editObject.actions[next.name] = next;
  }
}
