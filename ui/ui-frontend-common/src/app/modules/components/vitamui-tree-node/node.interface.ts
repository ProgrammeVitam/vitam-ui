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

import { Id } from '../../models';

export interface FilingHoldingSchemeNode extends Id {
  title: string;
  /** @deprecated: use unitType & descriptionLevel instead */
  type?: string;
  unitType?: string;
  descriptionLevel?: string;
  label?: string;
  children: FilingHoldingSchemeNode[];
  vitamId: string;

  // DISPLAY
  disabled?: boolean; // used in VitamuiTreeNodeComponent to disable mat-icon-button & mat-checkbox
  checked: boolean; // used in VitamuiTreeNodeComponent to set ngModel of mat-checkbox
  count?: number;
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
  nodesFoundButUnchanged: number;

  constructor() {
    this.nodesAdded = 0;
    this.nodesAddedList = [];
    this.nodesUpdated = 0;
    this.nodesFoundButUnchanged = 0;
  }

  addNode(node: FilingHoldingSchemeNode) {
    this.nodesAdded += 1;
    this.nodesAddedList.push(node);
  }

  incrementUpdated() {
    this.nodesUpdated += 1;
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
