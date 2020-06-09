import {Component, Input, NO_ERRORS_SCHEMA} from '@angular/core';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTreeModule} from '@angular/material/tree';
import {EMPTY, of} from 'rxjs';

import {AuthService} from 'ui-frontend-common';
import {FilingPlanComponent} from './filing-plan.component';
import {FilingPlanMode, FilingPlanService} from './filing-plan.service';
import {Node} from '../../models/node.interface';
import {FileType} from "../../models/file-type.enum";

@Component({ selector: 'vitamui-library-node', template: '' })
class NodeStubComponent {
  @Input() tenantIdentifier: any;
  @Input() node: any;
  @Input() expanded: boolean;
  @Input() disabled: boolean;
  @Input() archiveParamPositions: any;
}

describe('FilingPlanComponent', () => {
  let component: FilingPlanComponent;
  let fixture: ComponentFixture<FilingPlanComponent>;
  let fillingPlanStub = { 
    tree$: of([]), 
    expandChange$: EMPTY, 
    loadTree:() => of([]) 
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatTreeModule,
        MatProgressSpinnerModule,
      ],
      declarations: [ FilingPlanComponent, NodeStubComponent ],
      providers: [
        { provide: FilingPlanService, useValue: fillingPlanStub },
        { provide: AuthService, useValue: { user: { profileGroup: { profiles: [] } } } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilingPlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Checkbox-Clic', () => {
    let nodes: Node[] = [];


    beforeEach(() => {
      const rootNode: Node = {
        id: 'rootId',
        label: "RootNode",
        type: FileType.FOLDER_HOLDING,
        children: [],
        ingestContractIdentifier: 'string',
        vitamId: 'rootId',
        parents: [],
        checked: false,
        disabledChild: false
      };

      const rootChildren: Node[] = [
        {
          id: 'rootChild-1',
          label: "RootChild 1",
          type: FileType.FOLDER_HOLDING,
          children: [],
          ingestContractIdentifier: 'string',
          vitamId: 'rootChild-1',
          parents: [rootNode],
          checked: false,
          disabledChild: false
        }, {
          id: 'rootChild-2',
          label: "RootChild 2",
          type: FileType.FOLDER_HOLDING,
          children: [],
          ingestContractIdentifier: 'string',
          vitamId: 'rootChild-2',
          parents: [rootNode],
          checked: false,
          disabledChild: false
        }
      ];
      rootNode.children = rootChildren;
      rootChildren[0].children = [{
        id: 'leaf-1',
        label: "Leaf 1",
        type: FileType.FOLDER_HOLDING,
        children: [],
        ingestContractIdentifier: 'string',
        vitamId: 'leaf-1',
        parents: [rootChildren[0]],
        checked: false,
        disabledChild: false
      }, {
        id: 'leaf-2',
        label: "Leaf 2",
        type: FileType.FOLDER_HOLDING,
        children: [],
        ingestContractIdentifier: 'string',
        vitamId: 'leaf-2',
        parents: [rootChildren[0]],
        checked: false,
        disabledChild: false
      }];

      nodes = [rootNode];
      component.nestedDataSource.data = nodes;
      component.nestedTreeControl.dataNodes = nodes;
    });

    describe('BOTH-Mode', () => {
      beforeEach(() => {
        component.mode = FilingPlanMode.BOTH;
      });

      it('should check all nodes when select root node', () => {
        // When: Check root node + emit update
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);

        // Then: Check all nodes checked
        expect(nodes[0].checked).toBeTruthy('Root node should be checked');
        expect(nodes[0].children[0].checked).toBeTruthy('Child 1 should be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeTruthy('Leaf 1 should be checked');
        expect(nodes[0].children[0].children[1].checked).toBeTruthy('Leaf 2 should be checked');

        // And: rootNode in selected / included nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
      });

      it('should exclude and deselect children when unselect root child 1', () => {
        // When: Check root node + emit update
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check all nodes state
        expect(nodes[0].checked).toBeTruthy('Root node should be checked');
        expect(nodes[0].children[0].checked).toBeFalsy('Child 1 should not be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeFalsy('Leaf 1 should not be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check Root indeterminate state
        expect(nodes[0].disabledChild).toBeTruthy('Root node should have disabledChild');

        // And: Check selectedNodes state
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
        expect(component.selectedNodes.excluded.length).toBe(1, 'only 1 node should be excluded');
        expect(component.selectedNodes.excluded[0]).toBe('rootChild-1', 'rootChild-1 should be excluded');
      });

      it('should re-include leaf is selected', () => {
        // When: Check root node + emit update
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0].children[0]);

        // Then: Check all nodes state
        expect(nodes[0].checked).toBeTruthy('Root node should be checked');
        expect(nodes[0].children[0].checked).toBeFalsy('Child 1 should not be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeTruthy('Leaf 1 should be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check Root indeterminate state
        expect(nodes[0].disabledChild).toBeTruthy('Root node should have disabledChild');

        // And: Check selectedNodes state
        expect(component.selectedNodes.included.length).toBe(2, 'only 2 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
        expect(component.selectedNodes.included[1]).toBe('leaf-1', 'leaf-1 should be included');
        expect(component.selectedNodes.excluded.length).toBe(1, 'only 1 node should be excluded');
        expect(component.selectedNodes.excluded[0]).toBe('rootChild-1', 'rootChild-1 should be excluded');
      });

      it('should remove leaf from included if parent is re-selected', () => {
        // When: Check root node + emit update
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0].children[0]);
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check all nodes state
        expect(nodes[0].checked).toBeTruthy('Root node should be checked');
        expect(nodes[0].children[0].checked).toBeTruthy('Child 1 should be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeTruthy('Leaf 1 should be checked');
        expect(nodes[0].children[0].children[1].checked).toBeTruthy('Leaf 2 should be checked');

        // Then: Check Root indeterminate state
        expect(nodes[0].disabledChild).toBeFalsy('Root node should not have disabledChild');

        // And: Check selectedNodes state
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
      });

      it('should exclude leaf is deselected', () => {
        // When: Check root node + emit update
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);
        nodes[0].children[0].children[1].checked = false;
        component.emitVitamId(nodes[0].children[0].children[1]);

        // Then: Check all nodes checked
        expect(nodes[0].checked).toBeTruthy('Root node should be checked');
        expect(nodes[0].children[0].checked).toBeTruthy('Child 1 should be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeTruthy('Leaf 1 should be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check Root indeterminate state
        expect(nodes[0].disabledChild).toBeTruthy('Root node should have disabledChild');
        expect(nodes[0].children[0].disabledChild).toBeTruthy('Child 1 should have disabledChild');

        // And: rootNode in selected / included nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
        expect(component.selectedNodes.excluded.length).toBe(1, 'only 1 node should be excluded');
        expect(component.selectedNodes.excluded[0]).toBe('leaf-2', 'leaf-2 should be excluded');
      });

      it('should remove leaf from excluded if parent is deselected', () => {
        // When: Check root node + emit update
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);
        nodes[0].children[0].children[1].checked = false;
        component.emitVitamId(nodes[0].children[0].children[1]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check all nodes checked
        expect(nodes[0].checked).toBeTruthy('Root node should be checked');
        expect(nodes[0].children[0].checked).toBeFalsy('Child 1 node should not be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeFalsy('Leaf 1 should not be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check Root indeterminate state
        expect(nodes[0].disabledChild).toBeTruthy('Root node should have disabledChild');

        // And: rootNode in selected / included nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
        expect(component.selectedNodes.excluded.length).toBe(1, 'only 1 node should be excluded');
        expect(component.selectedNodes.excluded[0]).toBe('rootChild-1', 'rootChild-1 should be excluded');
      });
    });

    describe('INCLUDE-Mode', () => {
      beforeEach(() => {
        component.mode = FilingPlanMode.INCLUDE_ONLY;
      });

      it('should check and disable all children nodes', () => {
        // When: Check root node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check selected and all children checked (and children disabled)
        expect(nodes[0].children[0].checked && !nodes[0].children[0].disabled).toBeTruthy('Child 1 should be Checked and not Disabled');
        expect(nodes[0].children[0].children[0].checked && nodes[0].children[0].children[0].disabled).toBeTruthy('Leaf 1 should be Checked and Disabled');
        expect(nodes[0].children[0].children[1].checked && nodes[0].children[0].children[1].disabled).toBeTruthy('Leaf 2 should be Checked and Disabled');

        // Then: Check sibling and parent nodes not checked nor disabled
        expect(!nodes[0].checked && !nodes[0].disabled).toBeTruthy('Root node should not be Checked nor Disabled');
        expect(!nodes[0].children[1].checked && !nodes[0].children[1].disabled).toBeTruthy('Root node should not be Checked nor Disabled');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootChild-1', 'rootChild-1 should be included');
      });

      it('should add another inclusion when selecting other node', () => {
        // When: Check root node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[1].checked = true;
        component.emitVitamId(nodes[0].children[1]);

        // Then: Check selected and all children checked (and children disabled)
        expect(nodes[0].children[0].checked && !nodes[0].children[0].disabled).toBeTruthy('Child 1 should be Checked and not Disabled');
        expect(nodes[0].children[0].children[0].checked && nodes[0].children[0].children[0].disabled).toBeTruthy('Leaf 1 should be Checked and Disabled');
        expect(nodes[0].children[0].children[1].checked && nodes[0].children[0].children[1].disabled).toBeTruthy('Leaf 2 should be Checked and Disabled');
        expect(nodes[0].children[1].checked && !nodes[0].children[1].disabled).toBeTruthy('Child 2 should be Checked and not Disabled');

        // Then: Check sibling and parent nodes not checked nor disabled
        expect(!nodes[0].checked && !nodes[0].disabled).toBeTruthy('Root node should not be Checked nor Disabled');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(2, 'only 2 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootChild-1', 'rootChild-1 should be included');
        expect(component.selectedNodes.included[1]).toBe('rootChild-2', 'rootChild-2 should be included');
      });

      it('should unselect node and children when uncheck node', () => {
        // When: Check root node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[1].checked = true;
        component.emitVitamId(nodes[0].children[1]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check selected and all children checked (and children disabled)
        expect(nodes[0].children[1].checked && !nodes[0].children[1].disabled).toBeTruthy('Child 2 should be Checked and not Disabled');

        // Then: Check sibling and parent nodes not checked nor disabled
        expect(!nodes[0].checked && !nodes[0].disabled).toBeTruthy('Root node should not be Checked nor Disabled');
        expect(!nodes[0].children[0].checked && !nodes[0].children[0].disabled).toBeTruthy('Child 1 should not be Checked nor Disabled');
        expect(!nodes[0].children[0].children[0].checked && !nodes[0].children[0].children[0].disabled).toBeTruthy('Leaf 1 should not be Checked nor Disabled');
        expect(!nodes[0].children[0].children[1].checked && !nodes[0].children[0].children[1].disabled).toBeTruthy('Leaf 2 should not be Checked nor Disabled');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootChild-2', 'rootChild-2 should be included');
      });

      it('should unselect and disable selected children when select root', () => {
        // When: Check some node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[1].checked = true;
        component.emitVitamId(nodes[0].children[1]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].checked = true;
        component.emitVitamId(nodes[0]);

        // Then: Check selected and all children checked (and children disabled)
        expect(nodes[0].checked && !nodes[0].disabled).toBeTruthy('Root node should be Checked and not Disabled');
        expect(nodes[0].children[0].checked && nodes[0].children[0].disabled).toBeTruthy('Child 1 should be Checked and Disabled');
        expect(nodes[0].children[1].checked && nodes[0].children[1].disabled).toBeTruthy('Child 2 should be Checked and Disabled');
        expect(nodes[0].children[0].children[0].checked && nodes[0].children[0].children[0].disabled).toBeTruthy('Leaf 1 should be Checked and Disabled');
        expect(nodes[0].children[0].children[1].checked && nodes[0].children[0].children[1].disabled).toBeTruthy('Leaf 2 should be Checked and Disabled');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootId', 'rootId should be included');
      });
    });

    describe('SOLO-Mode', () => {
      beforeEach(() => {
        component.mode = FilingPlanMode.SOLO;
      });

      it('should include selected node', () => {
        // When: Check root node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check only checked is checked
        expect(nodes[0].checked).toBeFalsy('Root node should not be checked');
        expect(nodes[0].children[0].checked).toBeTruthy('Child 1 should be checked');
        expect(nodes[0].children[1].checked).toBeFalsy('Child 2 should not be checked');
        expect(nodes[0].children[0].children[0].checked).toBeFalsy('Leaf 1 should not be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootChild-1', 'rootChild-1 should be included');
      });

      it('should remove unselected node from inclusion', () => {
        // When: Check root node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[0].checked = false;
        component.emitVitamId(nodes[0].children[0]);

        // Then: Check only new node is checked
        expect(nodes[0].checked).toBeFalsy('Root Node should not be checked');
        expect(nodes[0].children[0].checked).toBeFalsy('Child 1 should not be checked');
        expect(nodes[0].children[1].checked).toBeFalsy('Child 2 should not be checked');
        expect(nodes[0].children[0].children[0].checked).toBeFalsy('Leaf 1 should not be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(0, 'no node should be included');
      });

      it('should change inclusion node with the new selected node', () => {
        // When: Check root node + emit update
        nodes[0].children[0].checked = true;
        component.emitVitamId(nodes[0].children[0]);
        nodes[0].children[1].checked = true;
        component.emitVitamId(nodes[0].children[1]);

        // Then: Check only new node is checked
        expect(nodes[0].checked).toBeFalsy('Root Node should not be checked');
        expect(nodes[0].children[0].checked).toBeFalsy('Child 1 should not be checked');
        expect(nodes[0].children[1].checked).toBeTruthy('Child 2 should be checked');
        expect(nodes[0].children[0].children[0].checked).toBeFalsy('Leaf 1 should not be checked');
        expect(nodes[0].children[0].children[1].checked).toBeFalsy('Leaf 2 should not be checked');

        // Then: Check selected nodes
        expect(component.selectedNodes.included.length).toBe(1, 'only 1 node should be included');
        expect(component.selectedNodes.included[0]).toBe('rootChild-2', 'rootChild-2 should be included');
      });
    })

  })
});
