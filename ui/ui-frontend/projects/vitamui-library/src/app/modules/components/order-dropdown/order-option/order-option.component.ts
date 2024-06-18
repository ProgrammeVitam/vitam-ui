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
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Direction } from '../../../vitamui-table';

@Component({
  selector: 'vitamui-common-order-option',
  templateUrl: './order-option.component.html',
  styleUrls: ['./order-option.component.scss'],
})
export class OrderOptionComponent {
  @Input() orderByKey: string;
  @Input() orderBy: string;
  @Input() direction: Direction;

  @Output() readonly orderChange = new EventEmitter<string>();
  @Output() readonly orderByChange = new EventEmitter<string>();
  @Output() readonly directionChange = new EventEmitter<Direction>();

  toggleOrder() {
    if (this.orderBy === this.orderByKey) {
      this.direction = this.direction !== Direction.DESCENDANT ? Direction.DESCENDANT : Direction.ASCENDANT;
    } else {
      this.orderBy = this.orderByKey;
      this.orderByChange.emit(this.orderBy);
      this.direction = Direction.DESCENDANT;
    }
    this.directionChange.emit(this.direction);
    this.orderChange.emit();
  }
}
