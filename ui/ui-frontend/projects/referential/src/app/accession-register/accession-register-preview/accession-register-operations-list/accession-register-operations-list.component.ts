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
import { animate, AUTO_STYLE, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import {
  Direction,
  RegisterValueEventModel,
  RegisterValueEventType,
  TableFilterDirective,
  TableFilterComponent,
  TableFilterOptionComponent,
  OrderByButtonComponent,
} from 'vitamui-library';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-accession-register-operations-list',
  templateUrl: './accession-register-operations-list.component.html',
  styleUrls: ['./accession-register-operations-list.component.scss'],
  animations: [
    trigger('collapse', [
      state('false', style({ height: AUTO_STYLE, visibility: AUTO_STYLE })),
      state('true', style({ height: '0', visibility: 'hidden' })),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
  imports: [TableFilterDirective, TableFilterComponent, TableFilterOptionComponent, OrderByButtonComponent, DatePipe, TranslatePipe],
})
export class AccessionRegisterOperationsListComponent implements OnChanges {
  private translateService = inject(TranslateService);

  @Input() operationsIds: string[];
  @Input() operations: RegisterValueEventModel[];

  orderColumn: keyof RegisterValueEventModel = 'OpType';
  orderDirection = Direction.ASCENDANT;
  availableOperationsType: Array<{ name: RegisterValueEventType; translation: string }> = [];
  selectedFilters: Array<string>;
  operationsProcessed: RegisterValueEventModel[] = [];

  orderKeyOperationType: keyof RegisterValueEventModel = 'OpType';
  orderKeyOperationGots: keyof RegisterValueEventModel = 'Gots';
  orderKeyOperationUnits: keyof RegisterValueEventModel = 'Units';
  orderKeyOperationObjects: keyof RegisterValueEventModel = 'Objects';
  orderKeyOperationObjSize: keyof RegisterValueEventModel = 'ObjSize';
  orderKeyOperationCreationDate: keyof RegisterValueEventModel = 'CreationDate';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['operations']) {
      this.reloadOperations();
    }
  }

  private reloadOperations() {
    this.availableOperationsType = this.operations
      .map((o) => o.OpType)
      .filter((value, index, array) => index === array.indexOf(value))
      .map((operationType) => {
        return {
          name: operationType,
          translation: this.translateService.instant('ACCESSION_REGISTER.PREVIEW.OPERATIONS.TYPE.' + operationType),
        };
      });
    this.selectedFilters = this.availableOperationsType.map((operationType) => operationType.name);
    this.operationsProcessed = [...this.operations];
  }

  changeOrderDirection(direction: Direction) {
    this.orderDirection = direction;
  }

  changeOrderColumn(column: string) {
    this.orderColumn = column as keyof RegisterValueEventModel;
  }

  sortTable() {
    const sens: number = this.orderDirection === Direction.ASCENDANT ? -1 : 1;
    this.operationsProcessed.sort((a, b) => {
      return a[this.orderColumn] === b[this.orderColumn] ? 0 : a[this.orderColumn] > b[this.orderColumn] ? sens : -sens;
    });
  }

  changeFilter(selectedFilters: string[]) {
    this.operationsProcessed = this.operations.filter((a) => selectedFilters.includes(a.OpType));
    this.sortTable();
  }

  protected readonly RegisterValueEventType = RegisterValueEventType;
}
