/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FilingHoldingSchemeNode, ORPHANS_NODE_ID } from 'ui-frontend-common';
import { GetorixDepositSharedDataService } from '../../services/getorix-deposit-shared-data.service';

const INGEST = 'INGEST';
const ITEM = 'Item';
@Component({
  selector: 'getorix-tree-node',
  templateUrl: './getorix-tree-node.component.html',
  styleUrls: ['./getorix-tree-node.component.scss'],
})
export class GetorixTreeNodeComponent implements OnInit {
  @Input() node: FilingHoldingSchemeNode;
  @Input() icon: string;
  @Input() expanded: boolean;
  @Input() disabled: boolean;
  @Output() nodeToggle = new EventEmitter<void>();
  @Output() firstNodeToggle = new EventEmitter<void>();
  @Output() labelClickToSearchUnits = new EventEmitter<FilingHoldingSchemeNode>();

  constructor(private getorixDepositSharedDataService: GetorixDepositSharedDataService) {}

  ngOnInit() {
    if (!this.expanded) {
      this.showFirstNodeChildren();
    }
  }

  onLabelClick(nodeSelected: FilingHoldingSchemeNode) {
    if (nodeSelected?.unitType === INGEST && nodeSelected.descriptionLevel === ITEM) {
      return;
    } else {
      nodeSelected.checked = true;
      this.getorixDepositSharedDataService.emitSelectedNode(nodeSelected);
      if (nodeSelected.id === ORPHANS_NODE_ID) {
        this.labelClickToSearchUnits.emit(nodeSelected);
      }
      this.labelClickToSearchUnits.emit(nodeSelected);
    }
  }

  showFirstNodeChildren() {
    this.firstNodeToggle.emit();
  }
}
