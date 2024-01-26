/* eslint-disable @typescript-eslint/no-use-before-define, @angular-eslint/component-selector */
import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, forwardRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { v4 as uuid } from 'uuid';

import { Node } from '../../models/node.interface';
import { FilingPlanMode, FilingPlanService } from './filing-plan.service';

export const NODE_SELECT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => FilingPlanComponent),
  multi: true
};

@Component({
  selector: 'vitamui-library-filing-plan',
  templateUrl: './filing-plan.component.html',
  styleUrls: ['./filing-plan.component.scss'],
  providers: [NODE_SELECT_VALUE_ACCESSOR]
})
export class FilingPlanComponent implements ControlValueAccessor, OnChanges {

  @Input() tenantIdentifier: number;
  /** @deprecated should be removed */
  @Input() accessContract: string;
  @Input() mode: FilingPlanMode;
  @Input() componentId: string = uuid();

  selectedNodes: { included: string[]; excluded: string[] } = {
    included: [],
    excluded: []
  };

  disabled: boolean;

  nestedTreeControl: NestedTreeControl<Node>;
  nestedDataSource: MatTreeNestedDataSource<Node>;

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  onChange = (_x: { included: string[]; excluded: string[] }) => {
  };

  onTouched = () => {
  };

  constructor(public filingPlanService: FilingPlanService) {
    this.nestedTreeControl = new NestedTreeControl<Node>((node) => node.children);
    this.nestedDataSource = new MatTreeNestedDataSource();
  }

  initFiningTree() {
    this.filingPlanService
      .loadTree(this.tenantIdentifier, this.componentId)
      .subscribe(nodes => {
        this.nestedDataSource.data = nodes;
        this.nestedTreeControl.dataNodes = nodes;
        this.initCheckedNodes(this.selectedNodes, nodes);
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.tenantIdentifier) {
      this.initFiningTree();
    }
  }

  hasNestedChild = (_: number, node: any) => node.children && node.children.length;

  updateChildrenStatusAndSelectedNodes(children: Node[], check: boolean) {
    if (!children || children.length === 0) {
      return;
    }

    children.forEach(childNode => {
      if (!childNode) {
        return;
      }

      childNode.checked = check;
      if (this.mode === FilingPlanMode.INCLUDE_ONLY) {
        childNode.disabled = check;
      }

      this.selectedNodes.included = this.selectedNodes.included.filter(id => childNode.id !== id);

      if (this.mode === FilingPlanMode.BOTH) {
        this.selectedNodes.excluded = this.selectedNodes.excluded.filter(id => childNode.id !== id);
      }

      this.updateChildrenStatusAndSelectedNodes(childNode.children, check);
    });
  }

  updateParentsStatus(parents: Node[], nodeChecked: boolean, childDisabled: boolean) {
    if (!parents || parents.length === 0) {
      return;
    }

    parents.forEach(parentNode => {
      if (!parentNode || !parentNode.checked) {
        return;
      }

      if (!nodeChecked || childDisabled) {
        parentNode.disabledChild = true;
      } else if (nodeChecked
        && !childDisabled
        && parentNode.disabledChild
        && !parentNode.children.find(child => !child.checked || childDisabled)) {
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

    nodes.forEach(node => {
      if (!node) {
        return;
      }

      if (node.vitamId !== nodeId && node.checked) {
        node.checked = false;
        const index = this.selectedNodes.included.findIndex(id => node.vitamId === id);
        if (index !== -1) {
          this.selectedNodes.included.splice(index, 1);
        }
      }

      this.unselectOther(nodeId, node.children);
    });
  }

  emitVitamId(node: Node) {
    const nodeChecked = node.checked;

    if (this.mode === FilingPlanMode.BOTH) {
      this.updateParentsStatus(node.parents, nodeChecked, node.disabledChild);
    }

    if (this.mode === FilingPlanMode.INCLUDE_ONLY || this.mode === FilingPlanMode.BOTH) {
      this.updateChildrenStatusAndSelectedNodes(node.children, node.checked);
    }

    if (this.mode === FilingPlanMode.SOLO && nodeChecked) {
      this.unselectOther(node.vitamId, this.nestedDataSource.data);
    }

    // Update selectedNodes with new update
    // remove old inclusion/exclusion because of a parent change status
    if (nodeChecked) {
      const oldExcludedIndex = this.selectedNodes.excluded.findIndex(id => node.vitamId === id);
      if (this.mode === FilingPlanMode.BOTH && oldExcludedIndex !== -1) {
        this.selectedNodes.excluded.splice(oldExcludedIndex, 1);
      } else {
        this.selectedNodes.included.push(node.vitamId);
      }
    } else {
      const oldIncludedIndex = this.selectedNodes.included.findIndex(id => node.vitamId === id);
      if (this.mode === FilingPlanMode.BOTH && oldIncludedIndex === -1) {
        this.selectedNodes.excluded.push(node.vitamId);
      } else {
        this.selectedNodes.included.splice(oldIncludedIndex, 1);
      }
    }

    // FIXME is this really needed ?
    this.onChange(this.selectedNodes);
  }

  initCheckedNodes(obj: { included: string[]; excluded: string[] }, nodes: Node[], parentChecked: boolean = false) {

    if (!obj || !nodes) {
      return;
    }

    let shouldStop = false;

    nodes.forEach(node => {

      if (!node || shouldStop) {
        return;
      }

      if (this.mode === FilingPlanMode.SOLO && obj.included && obj.included.includes(node.vitamId)) {
        node.checked = true;
        shouldStop = true;
        return;
      }

      if (this.mode === FilingPlanMode.INCLUDE_ONLY && !parentChecked && obj.included && obj.included.includes(node.vitamId)) {
        node.checked = true;
        this.updateChildrenStatusAndSelectedNodes(node.children, true);
        return;
      }

      if (this.mode === FilingPlanMode.BOTH && (!parentChecked && obj.included && obj.included.includes(node.vitamId)) || parentChecked) {
        node.checked = true;
      }

      if (this.mode === FilingPlanMode.BOTH && parentChecked && obj.excluded && obj.excluded.includes(node.vitamId)) {
        node.checked = false;
        this.updateParentsStatus(node.parents, false, node.disabledChild);
      }

      shouldStop = this.initCheckedNodes(obj, node.children, node.checked);
    });

    return shouldStop;
  }

  writeValue(obj: { included: string[]; excluded: string[] }): void {
    this.initCheckedNodes(obj, this.nestedDataSource.data);
    this.selectedNodes = obj;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

}
