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
 *
 */

import { Id, UnitType } from '../index';
import { DescriptionLevel } from '../units/description-level.enum';

export interface FilingHoldingSchemeNode extends Id {
  title: string;
  /** @deprecated: use unitType & descriptionLevel instead */
  type?: string;
  unitType?: UnitType;
  descriptionLevel?: DescriptionLevel;
  label?: string;
  children: FilingHoldingSchemeNode[];
  vitamId: string;

  // DISPLAY
  disabledChild?: boolean; // TODO: try to remove - used in VitamuiTreeNodeComponent to set indeterminate
  disabled?: boolean; // used in VitamuiTreeNodeComponent to disable mat-icon-button & mat-checkbox
  checked: boolean; // used in VitamuiTreeNodeComponent to set ngModel of mat-checkbox
  count?: number;
  hidden?: boolean; // TODO: try to remove - may be unused
  isLoadingChildren?: boolean;
  toggled?: boolean;
  // help to detect the unit type and the icon to show
  hasObject?: boolean;
  // help to keep tracks on what has been loaded
  paginatedChildrenLoaded?: number;
  canLoadMoreChildren?: boolean;
  paginatedMatchingChildrenLoaded?: number;
  canLoadMoreMatchingChildren?: boolean;
}

export class MatchingNodesNumbers {
  nodesAdded: number;
  nodesAddedList: FilingHoldingSchemeNode[];
  nodesUpdated: number;
  nodesUpdatedList: FilingHoldingSchemeNode[];
  nodesFoundButUnchanged: number;

  constructor() {
    this.nodesAdded = 0;
    this.nodesAddedList = [];
    this.nodesUpdated = 0;
    this.nodesUpdatedList = [];
    this.nodesFoundButUnchanged = 0;
  }

  addNode(node: FilingHoldingSchemeNode) {
    this.nodesAdded += 1;
    this.nodesAddedList.push(node);
  }

  updatedNode(node: FilingHoldingSchemeNode) {
    this.nodesUpdated += 1;
    this.nodesUpdatedList.push(node);
  }

  incrementFoundButUnchanged() {
    this.nodesFoundButUnchanged += 1;
  }

  mergeWith(matchingNodesNumbers: MatchingNodesNumbers) {
    this.nodesAdded += matchingNodesNumbers.nodesAdded;
    this.nodesAddedList.push(...matchingNodesNumbers.nodesAddedList);
    this.nodesUpdated += matchingNodesNumbers.nodesUpdated;
    this.nodesFoundButUnchanged += matchingNodesNumbers.nodesFoundButUnchanged;
  }
}
