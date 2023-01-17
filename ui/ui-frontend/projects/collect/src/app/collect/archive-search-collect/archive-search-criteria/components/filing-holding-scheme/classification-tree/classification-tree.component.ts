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
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { FilingHoldingSchemeNode, nodeHasChildren, nodeHasMatch, VitamuiIcons, VitamuiUnitTypes } from 'ui-frontend-common';
import { Pair } from '../../../models/utils';

@Component({
  selector: 'app-classification-tree',
  templateUrl: './classification-tree.component.html',
  styleUrls: ['./classification-tree.component.scss'],
})
export class ClassificationTreeComponent {
  @Input() accessContract: string;
  @Input() loadingHolding: boolean;
  @Input() nestedDataSourceFull: MatTreeNestedDataSource<FilingHoldingSchemeNode>;
  @Input() nestedTreeControlFull: NestedTreeControl<FilingHoldingSchemeNode>;
  @Input() hasMatchesInSearch: boolean;
  @Input() showEveryNodes: boolean;
  @Input() loadingNodeUnit: boolean;

  @Output() addToSearchCriteria: EventEmitter<FilingHoldingSchemeNode> = new EventEmitter();
  @Output() showNodeDetail: EventEmitter<Pair> = new EventEmitter();
  @Output() switchView: EventEmitter<void> = new EventEmitter();
  @Output() closePanel: EventEmitter<void> = new EventEmitter();

  addToSearchCriteriaList(node: FilingHoldingSchemeNode) {
    this.addToSearchCriteria.emit(node);
  }

  onShowNodeDetails(archiveUnitId: string) {
    this.showNodeDetail.emit(new Pair(archiveUnitId, false));
  }

  onViewSwitched() {
    this.switchView.emit();
  }

  nodeHasChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return nodeHasChildren(node);
  }

  nodeHasNoChidren(_: number, node: FilingHoldingSchemeNode): boolean {
    return !nodeHasChildren(node);
  }

  nodeHasMatchsOrShowAll(node: FilingHoldingSchemeNode): boolean {
    return this.showEveryNodes || nodeHasMatch(node);
  }

  onClosePanel() {
    this.closePanel.emit();
  }
  getNodeUnitType(filingholdingscheme: FilingHoldingSchemeNode) {
    if (filingholdingscheme && filingholdingscheme.unitType) {
      return filingholdingscheme.unitType;
    }
  }

  getNodeUnitIcone(filingholdingscheme: FilingHoldingSchemeNode) {
    return this.getNodeUnitType(filingholdingscheme) === VitamuiUnitTypes.HOLDING_UNIT
      ? VitamuiIcons.VITAMUI_HOLDING_UNIT_ICON_
      : this.getNodeUnitType(filingholdingscheme) === VitamuiUnitTypes.FILING_UNIT
      ? VitamuiIcons.VITAMUI_FILING_UNIT_ICON_
      : this.getNodeUnitType(filingholdingscheme) === VitamuiUnitTypes.INGEST && !filingholdingscheme?.hasObject
      ? VitamuiIcons.VITAMUI_INGEST_WITHOUT_OBJECT_ICON_
      : VitamuiIcons.VITAMUI_INGEST_WITH_OBJECT_ICON_;
  }
}
