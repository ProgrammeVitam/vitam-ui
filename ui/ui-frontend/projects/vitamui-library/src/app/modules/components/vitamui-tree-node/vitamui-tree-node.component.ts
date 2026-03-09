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
import { AfterContentChecked, ChangeDetectorRef, Component, EventEmitter, Input, Output } from '@angular/core';
import { FilingHoldingSchemeNode } from '../../models';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CommonTooltipModule } from '../common-tooltip/common-tooltip.module';
import { UnitType } from '../../models';
import { CommonModule } from '@angular/common';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'vitamui-tree-node',
  templateUrl: './vitamui-tree-node.component.html',
  styleUrls: ['./vitamui-tree-node.component.scss'],
  imports: [CommonModule, FormsModule, MatButtonModule, MatCheckboxModule, CommonTooltipModule],
  standalone: true,
})
export class VitamuiTreeNodeComponent implements AfterContentChecked {
  @Input() node: FilingHoldingSchemeNode;
  @Input() icon: string;
  @Input() expanded: boolean;
  @Input() disabled: boolean;
  @Input() hasCheckBox = true;
  @Input() labelIsLinkedToCheckbox = false;

  @Output() nodeToggle = new EventEmitter<void>();
  @Output() checkboxClick = new EventEmitter<void>();
  @Output() labelClick = new EventEmitter<void>();

  constructor(private cdr: ChangeDetectorRef) {}

  ngAfterContentChecked(): void {
    this.cdr.detectChanges();
  }

  onCheckboxClick() {
    this.checkboxClick.emit();
  }

  onLabelClick(event: MouseEvent) {
    if (this.isVirtualNode() || this.node.disableLabelClickCallback) return;
    this.labelClick.emit();
    if (!this.labelIsLinkedToCheckbox) {
      event.stopPropagation();
      event.preventDefault();
    } else {
      this.node.checked = !this.node.checked;
    }
  }

  isVirtualNode() {
    return this.node.unitType === UnitType.VIRTUAL;
  }
}
