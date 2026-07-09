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
import { Component, DestroyRef, inject, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import {
  ApplicationId,
  ConfirmDialogComponent,
  ConfirmDialogData,
  Direction,
  Griffin,
  GriffinsService,
  SnackBarService,
  StartupService,
  TenantSelectionService,
  VitamUICommonModule,
} from 'vitamui-library';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { factorOf, sortByKey } from '../../sorting';
import { Subject } from 'rxjs';
import { debounceTime, filter } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-griffin-list',
  templateUrl: './griffin-list.component.html',
  styleUrls: ['./griffin-list.component.scss'],
  imports: [CommonModule, TranslatePipe, VitamUICommonModule, MatProgressSpinnerModule],
})
export class GriffinListComponent implements OnInit {
  private readonly griffinsService = inject(GriffinsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly matDialog = inject(MatDialog);
  private readonly snackBarService = inject(SnackBarService);
  private readonly startupService = inject(StartupService);
  private readonly tenantSelectionService = inject(TenantSelectionService);
  private readonly translateService = inject(TranslateService);

  private readonly vitamAdminTenant = +this.startupService.getConfigStringValue('VITAM_ADMIN_TENANT');

  private readonly allGriffins = signal<Griffin[]>([]);
  readonly griffins = signal<Griffin[]>([]);
  readonly loading = signal(false);
  readonly selectedGriffin = signal<Griffin>(null);

  readonly nameKey: keyof Griffin = 'Name';
  readonly identifierKey: keyof Griffin = 'Identifier';
  readonly creationDateKey: keyof Griffin = 'CreationDate';

  orderByKey: keyof Griffin = 'Identifier';
  direction = Direction.ASCENDANT;

  private _searchText: string;
  private readonly searchChange = new Subject<string>();

  @Input()
  set searchText(value: string) {
    this._searchText = value ?? '';
    this.searchChange.next(this._searchText);
  }

  constructor() {
    this.searchChange.pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS), takeUntilDestroyed()).subscribe(() => this.applyFilterAndSort());
  }

  ngOnInit() {
    this.loadGriffins();
  }

  sort() {
    this.applyFilterAndSort();
  }

  canDelete(): boolean {
    return this.tenantSelectionService.getSelectedTenant()?.identifier === this.vitamAdminTenant;
  }

  deleteGriffinDialog(griffin: Griffin) {
    this.matDialog
      .open<ConfirmDialogComponent, ConfirmDialogData>(ConfirmDialogComponent, {
        panelClass: 'small',
        data: {
          title: 'PRESERVATION.GRIFFIN.DELETE_DIALOG.TITLE',
          // `message` is rendered as-is by ConfirmDialogComponent, without the translate pipe.
          message: this.translateService.instant('PRESERVATION.GRIFFIN.DELETE_DIALOG.MESSAGE'),
          confirmLabel: 'COMMON.CONFIRM',
          cancelLabel: 'COMMON.CANCEL',
        },
      })
      .afterClosed()
      .pipe(
        filter((confirmed) => !!confirmed),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.deleteGriffin(griffin));
  }

  private deleteGriffin(griffin: Griffin) {
    this.griffinsService
      .delete(griffin)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.snackBarService.open({
            message: 'PRESERVATION.GRIFFIN.SNACKBAR.DELETE_REQUEST_ACCEPTED',
            buttons: [{ appId: ApplicationId.LOGBOOK_OPERATION_APP, label: 'SNACKBAR.OPEN_LOGBOOK' }],
          });

          if (this.isSelected(griffin)) {
            this.selectedGriffin.set(null);
          }

          this.loadGriffins();
        },
        error: () => {
          this.snackBarService.open({
            message: 'PRESERVATION.GRIFFIN.SNACKBAR.DELETE_FAILED',
            buttons: [{ appId: ApplicationId.LOGBOOK_OPERATION_APP, label: 'SNACKBAR.OPEN_LOGBOOK' }],
          });
        },
      });
  }

  private loadGriffins() {
    this.loading.set(true);
    this.griffinsService
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (griffins) => {
          this.allGriffins.set(griffins);
          this.applyFilterAndSort();
        },
        complete: () => this.loading.set(false),
        error: () => this.loading.set(false),
      });
  }

  private applyFilterAndSort() {
    const text = this._searchText.toLowerCase();
    const filtered = text
      ? this.allGriffins().filter((g) => g.Identifier.toLowerCase().includes(text) || g.Name.toLowerCase().includes(text))
      : [...this.allGriffins()];

    this.griffins.set(filtered.sort(sortByKey(this.orderByKey, factorOf(this.direction))));
  }

  isSelected(griffin: Griffin) {
    return this.selectedGriffin() ? this.selectedGriffin()['#id'] === griffin['#id'] : false;
  }
}
