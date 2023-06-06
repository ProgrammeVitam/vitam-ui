/**
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
import { unitTypeToVitamuiIcon } from '../../models';
import { VitamuiIcons } from '../../vitamui-icons.enum';
import { FilingHoldingSchemeNode } from './node.interface';

export function nodeToVitamuiIcon(node: FilingHoldingSchemeNode): VitamuiIcons {
  return unitTypeToVitamuiIcon(node.unitType, node.hasObject);
}

export const nodeHasChildren = (node: FilingHoldingSchemeNode): boolean => {
  return node.children && node.children.length > 0;
};

export const nodeHasMatch = (node: FilingHoldingSchemeNode): boolean => {
  return node.count && node.count > 0;
};

export const copyNodeWithoutChildren = (node: FilingHoldingSchemeNode): FilingHoldingSchemeNode => {
  return {
    id: node.id,
    title: node.title,
    type: node.type,
    label: node.label,
    children: null,
    vitamId: node.vitamId,
    checked: node.checked,
    count: node.count,

    hasObject: node?.hasObject,
    unitType: node?.unitType,

    hidden: node.hidden,
    isLoadingChildren: false,
    canLoadMoreChildren: true,
    canLoadMoreMatchingChildren: true,
  };
};

export function recursiveCheck(nodes: FilingHoldingSchemeNode[], show: boolean) {
  if (!nodes || nodes.length === 0) {
    return;
  }
  for (const node of nodes) {
    node.hidden = false;
    node.checked = show;
    node.count = null;
    this.recursiveCheck(node.children, show);
  }
}
