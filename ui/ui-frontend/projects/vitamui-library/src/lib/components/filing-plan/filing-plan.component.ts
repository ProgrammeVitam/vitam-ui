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
import { NestedTreeControl } from '@angular/cdk/tree';
import { ChangeDetectorRef, Component, forwardRef, inject, Injector, OnInit, input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import {
  MatNestedTreeNode,
  MatTree,
  MatTreeNestedDataSource,
  MatTreeNode,
  MatTreeNodeDef,
  MatTreeNodeOutlet,
} from '@angular/material/tree';
import { v4 as uuid } from 'uuid';

import { Node } from '../../models/node.interface';
import { FilingPlanMode, FilingPlanService } from './filing-plan.service';
import { Unit } from '../../../app/modules/models/units/unit.interface';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';
import { NodeComponent } from './node.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { FormErrorsComponent } from '../form-errors/form-errors.component';

export const NODE_SELECT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => FilingPlanComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-library-filing-plan',
  templateUrl: './filing-plan.component.html',
  styleUrls: ['./filing-plan.component.scss'],
  providers: [NODE_SELECT_VALUE_ACCESSOR],
  imports: [
    MatTree,
    MatTreeNodeDef,
    MatTreeNode,
    NodeComponent,
    MatNestedTreeNode,
    MatTreeNodeOutlet,
    MatProgressSpinner,
    FormErrorsComponent,
  ],
})
export class FilingPlanComponent extends AbstractFormInputDirective implements OnInit {
  filingPlanService = inject(FilingPlanService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly dataSource = input<Unit[]>(undefined);
  /** @deprecated should be removed - see VitamUIHttpInterceptor */
  readonly tenantIdentifier = input<number>(undefined);
  readonly mode = input<FilingPlanMode>(undefined);
  readonly componentId = input<string>(uuid());

  selectedNodes: { included: string[]; excluded: string[] } = {
    included: [],
    excluded: [],
  };

  nestedTreeControl: NestedTreeControl<Node>;
  nestedDataSource: MatTreeNestedDataSource<Node>;

  constructor() {
    const injector = inject(Injector);

    super(injector);
    this.nestedTreeControl = new NestedTreeControl<Node>((node) => node.children);
    this.nestedDataSource = new MatTreeNestedDataSource();
  }

  override ngOnInit(): void {
    super.ngOnInit();
    const dataSource = this.dataSource();
    if (dataSource) {
      const nodes = this.filingPlanService.loadTreeFromDataSource(dataSource, this.componentId());
      this.setNodes(nodes);
    } else {
      this.filingPlanService.loadTree(this.componentId()).subscribe((nodes) => {
        this.setNodes(nodes);
        // Tree data, selection and pending state are updated from an HTTP callback: refresh the view (zoneless)
        this.cdr.markForCheck();
      });
    }
  }

  setNodes(nodes: Node[]) {
    this.nestedDataSource.data = nodes;
    this.nestedTreeControl.dataNodes = nodes;
    // Ensure that the selectedNodes are included in the nodes (needed when a value is not in the datasource anymore)
    const nodeIds = this.getAllNodeIds(nodes);
    this.selectedNodes = {
      included: this.selectedNodes.included.filter((id) => nodeIds.includes(id)),
      excluded: this.selectedNodes.excluded.filter((id) => nodeIds.includes(id)),
    };
    this.initCheckedNodes(this.selectedNodes, nodes);
  }

  private getAllNodeIds(nodes: Node[]): string[] {
    return nodes.flatMap((node) => [node.vitamId, ...this.getAllNodeIds(node.children)]);
  }

  hasNestedChild = (_: number, node: any) => node.children && node.children.length;

  updateChildrenStatusAndSelectedNodes(children: Node[], check: boolean, enableAllChildren: boolean | null = false) {
    if (!children || children.length === 0) {
      return;
    }

    children.forEach((childNode) => {
      if (!childNode) {
        return;
      }

      childNode.checked = check;
      childNode.disabledChild = false;

      const mode = this.mode();
      if (mode === FilingPlanMode.INCLUDE_ONLY) {
        childNode.disabled = check;
      }

      this.selectedNodes.included = this.selectedNodes.included.filter((id) => childNode.vitamId !== id);

      if (mode === FilingPlanMode.BOTH) {
        if (enableAllChildren) {
          childNode.disabled = false;
        } else {
          childNode.disabled = !childNode.parents[0]?.checked;
        }
        this.selectedNodes.excluded = this.selectedNodes.excluded.filter((id) => childNode.vitamId !== id);
      }

      this.updateChildrenStatusAndSelectedNodes(childNode.children, check, enableAllChildren);
    });
  }

  updateParentsStatus(parents: Node[], nodeChecked: boolean, childDisabled: boolean) {
    if (!parents || parents.length === 0) {
      return;
    }

    parents.forEach((parentNode) => {
      if (!parentNode || !parentNode.checked) {
        return;
      }

      if (!nodeChecked || childDisabled) {
        parentNode.disabledChild = true;
      } else if (
        nodeChecked &&
        !childDisabled &&
        parentNode.disabledChild &&
        !parentNode.children.find((child) => !child.checked || childDisabled)
      ) {
        parentNode.disabledChild = false;
      }

      if (parentNode.parents) {
        this.updateParentsStatus(parentNode.parents, parentNode.checked, parentNode.disabledChild);
      }
    });
  }

  unselectOther(nodeId: string, nodes: Node[]) {
    if (!nodes && nodes.length === 0) {
      return;
    }

    nodes.forEach((node) => {
      if (!node) {
        return;
      }

      if (node.vitamId !== nodeId && node.checked) {
        node.checked = false;
        const index = this.selectedNodes.included.findIndex((id) => node.vitamId === id);
        if (index !== -1) {
          this.selectedNodes.included.splice(index, 1);
        }
      }

      this.unselectOther(nodeId, node.children);
    });
  }

  emitVitamId(node: Node) {
    const nodeChecked = node.checked;

    const mode = this.mode();
    if (mode === FilingPlanMode.BOTH) {
      this.updateParentsStatus(node.parents, nodeChecked, node.disabledChild);
      let enableAllChildren = false;
      if (!nodeChecked) {
        enableAllChildren = this.areAllParentsUnchecked(node.parents);
      }
      this.updateChildrenStatusAndSelectedNodes(node.children, nodeChecked, enableAllChildren);
    }

    if (mode === FilingPlanMode.INCLUDE_ONLY) {
      this.updateChildrenStatusAndSelectedNodes(node.children, node.checked);
    }

    if (mode === FilingPlanMode.SOLO && nodeChecked) {
      this.unselectOther(node.vitamId, this.nestedDataSource.data);
    }

    // Update selectedNodes with new update
    // remove old inclusion/exclusion because of a parent change status
    if (nodeChecked) {
      const oldExcludedIndex = this.selectedNodes.excluded.findIndex((id) => node.vitamId === id);
      if (mode === FilingPlanMode.BOTH && oldExcludedIndex !== -1) {
        this.selectedNodes.excluded.splice(oldExcludedIndex, 1);
      } else {
        this.selectedNodes.included.push(node.vitamId);
      }
    } else {
      const oldIncludedIndex = this.selectedNodes.included.findIndex((id) => node.vitamId === id);
      if (mode === FilingPlanMode.BOTH && oldIncludedIndex === -1) {
        this.selectedNodes.excluded.push(node.vitamId);
      } else {
        this.selectedNodes.included.splice(oldIncludedIndex, 1);
      }
    }

    this.onChange({
      included: [...this.selectedNodes.included],
      excluded: [...this.selectedNodes.excluded],
    });
    this.onTouched();
  }

  private areAllParentsUnchecked(parents: Node[]): boolean {
    if (parents[0]?.checked) {
      return false;
    }
    if (parents.length === 0 || !parents[0].parents || parents[0].parents.length === 0) {
      return true;
    }
    return this.areAllParentsUnchecked(parents[0].parents);
  }

  initCheckedNodes(obj: { included: string[]; excluded: string[] }, nodes: Node[], parentChecked: boolean = false): boolean {
    if (!obj || !nodes) {
      return false;
    }

    let shouldStop = false;

    for (const node of nodes) {
      if (!node || shouldStop) continue;

      const mode = this.mode();
      if (mode === FilingPlanMode.SOLO && obj.included && obj.included.includes(node.vitamId)) {
        node.checked = true;
        shouldStop = true;
        break;
      }

      if (mode === FilingPlanMode.INCLUDE_ONLY && !parentChecked && obj.included?.includes(node.vitamId)) {
        node.checked = true;
        this.updateChildrenStatusAndSelectedNodes(node.children, true);
        continue;
      }

      if (
        mode === FilingPlanMode.BOTH &&
        node.parents?.length > 0 &&
        (node.parents[0].disabled || (obj.excluded && obj.excluded.includes(node.parents[0].vitamId)))
      ) {
        node.disabled = true;
      }

      if ((mode === FilingPlanMode.BOTH && !parentChecked && obj.included?.includes(node.vitamId)) || parentChecked) {
        node.checked = true;
      }

      if (mode === FilingPlanMode.BOTH && parentChecked && obj.excluded?.includes(node.vitamId)) {
        node.checked = false;
        this.updateParentsStatus(node.parents, false, node.disabledChild);
      }

      shouldStop = this.initCheckedNodes(obj, node.children, node.checked);
    }

    return shouldStop;
  }

  override writeValue(obj: { included: string[]; excluded: string[] }): void {
    this.initCheckedNodes(obj, this.nestedDataSource.data);
    this.selectedNodes = obj;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabledState.set(isDisabled);
  }
}
