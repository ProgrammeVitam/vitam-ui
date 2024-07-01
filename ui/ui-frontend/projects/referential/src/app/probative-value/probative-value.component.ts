/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, OnDestroy, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ActivatedRoute, Router } from '@angular/router';
import moment from 'moment';
import { Subscription } from 'rxjs';
import {
  Event,
  GlobalEventService,
  SearchBarComponent,
  SidenavPage,
  VitamuiTitleBreadcrumbComponent,
  VitamuiCommonBannerComponent,
} from 'vitamui-library';
import { ProbativeValueCreateComponent } from './probative-value-create/probative-value-create.component';
import { ProbativeValueListComponent } from './probative-value-list/probative-value-list.component';
import { TranslateModule } from '@ngx-translate/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyInputModule } from '@angular/material/legacy-input';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';
import { ProbativeValuePreviewComponent } from './probative-value-preview/probative-value-preview.component';
import { NgIf } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';

@Component({
  selector: 'app-probative-value',
  templateUrl: './probative-value.component.html',
  styleUrls: ['./probative-value.component.scss'],
  standalone: true,
  imports: [
    MatSidenavModule,
    NgIf,
    ProbativeValuePreviewComponent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiCommonBannerComponent,
    FormsModule,
    ReactiveFormsModule,
    MatLegacyFormFieldModule,
    MatLegacyInputModule,
    MatDatepickerModule,
    ProbativeValueListComponent,
    TranslateModule,
  ],
})
export class ProbativeValueComponent extends SidenavPage<Event> implements OnDestroy {
  search: string;
  dateRangeFilterForm: FormGroup;
  filters: any = {};

  accessContractSub: Subscription;
  errorMessageSub: Subscription;
  foundAccessContract = false;
  accessContract: string;

  @ViewChild(SearchBarComponent, { static: true }) searchBar: SearchBarComponent;
  @ViewChild(ProbativeValueListComponent, { static: true }) probativeValueListComponent: ProbativeValueListComponent;

  constructor(
    public dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private formBuilder: FormBuilder,
  ) {
    super(route, globalEventService);

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
    });

    this.dateRangeFilterForm.controls.startDate.valueChanges.subscribe((value) => {
      this.filters = { ...this.filters, startDate: value };
    });

    this.dateRangeFilterForm.controls.endDate.valueChanges.subscribe((value: Date) => {
      this.filters = { ...this.filters, endDate: value ? moment(value).endOf('day') : null };
    });
  }

  openCreateProbativeValueDialog() {
    const dialogRef = this.dialog.open(ProbativeValueCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      autoFocus: false,
    });
    dialogRef.afterClosed().subscribe((result: any) => {
      if (result !== undefined && result.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.probativeValueListComponent) {
      return;
    }
    this.probativeValueListComponent.searchProbativeValueOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  clearDate(dateToClear: 'startDate' | 'endDate', $event: any, input: HTMLInputElement): void {
    if (!!this.dateRangeFilterForm.get(dateToClear).value) {
      this.dateRangeFilterForm.get(dateToClear).reset();
    }

    input.value = null;
    $event.stopPropagation();
  }

  resetFilters() {
    this.dateRangeFilterForm.reset();
    this.searchBar.reset();
  }

  showProbativeValue(item: Event) {
    this.openPanel(item);
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }
}
