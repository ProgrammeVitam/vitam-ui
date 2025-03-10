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
import { Component, ElementRef, Input, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { finalize, Subscription } from 'rxjs';
import {
  CommonTooltipModule,
  ItemFlatNode,
  ItemNode,
  ItemNodeUtils,
  normalizeString,
  SchemaElement,
  SchemaService,
  TableFilterModule,
  TenantSelectionService,
} from 'vitamui-library';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatRow, MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { SchemaDeleteDialogComponent, SchemaDeleteDialogComponentData } from './schema-delete-dialog/schema-delete-dialog.component';

@Component({
  imports: [
    CommonModule,
    MatTableModule,
    TranslatePipe,
    TableFilterModule,
    MatButtonToggleModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    CommonTooltipModule,
  ],
  selector: 'app-schema-list',
  templateUrl: './schema-list.component.html',
  styleUrls: ['./schema-list.component.scss'],
})
export class SchemaListComponent implements OnInit, OnDestroy {
  private _searchText: string;
  @Input()
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.filter();
  }

  @ViewChildren(MatRow, { read: ElementRef }) rows: QueryList<ElementRef>;
  pending = false;
  private readonly subscriptions = new Subscription();

  constructor(
    public schemaService: SchemaService,
    private translateService: TranslateService,
    public dialog: MatDialog,
    private tenantSelectionService: TenantSelectionService,
  ) {
    this.treeFlattener = new MatTreeFlattener(
      this.transformer,
      ItemNodeUtils.getLevel,
      ItemNodeUtils.isExpandable,
      ItemNodeUtils.getChildren,
    );
    this.treeControl = new FlatTreeControl<ItemFlatNode<SchemaElement>>(ItemNodeUtils.getLevel, ItemNodeUtils.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  }

  ngOnInit() {
    this.updateSchema();
  }

  private updateSchema() {
    this.pending = true;
    this.schemaService
      .getSchemaTreeByCategory()
      .pipe(finalize(() => (this.pending = false)))
      .subscribe({
        next: (data: ItemNode<SchemaElement>[]) => {
          const previousVirtualNodesByCategory = this.treeControl.dataNodes
            ?.filter((node) => node.item.id === this.schemaService.VIRTUAL_ROOT_NODES)
            .reduce((acc, node) => acc.set(node.item.Category, node), new Map<SchemaElement['Category'], ItemFlatNode<SchemaElement>>());
          const previousNodesByPath = this.treeControl.dataNodes
            ?.filter((node) => node.item.Path)
            .reduce((acc, node) => acc.set(node.item.Path, node), new Map<SchemaElement['Path'], ItemFlatNode<SchemaElement>>());

          this.dataSource.data = data;
          this.treeControl.dataNodes.forEach((node) => {
            const isVirtualNode = node.item.id === this.schemaService.VIRTUAL_ROOT_NODES;
            const previousNode = isVirtualNode
              ? previousVirtualNodesByCategory?.get(node.item.Category)
              : previousNodesByPath?.get(node.item.Path);
            if (this.treeControl.isExpanded(previousNode)) this.treeControl.expand(node);
          });
          this.filter();
        },
        error: (e) => console.error(e),
      });
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  protected availableFilters: Array<{ name: 'INTERNAL' | 'EXTERNAL'; translation: string }> = [
    {
      name: 'INTERNAL',
      translation: this.translateService.instant('ONTOLOGY.FILTER.INTERNAL'),
    },
    {
      name: 'EXTERNAL',
      translation: this.translateService.instant('ONTOLOGY.FILTER.EXTERNAL'),
    },
  ];
  protected selectedFilters: Array<string> = this.availableFilters.map((e) => e.name);

  filter() {
    this.treeControl.dataNodes?.forEach((node: ItemFlatNode<SchemaElement>) => {
      if (node.item.id === this.schemaService.VIRTUAL_ROOT_NODES) {
        node.display = true;
      } else {
        node.display =
          this.selectedFilters.includes(node.item.Origin) &&
          (!this._searchText || this.itemContainsSearchText(node.item, this._searchText));
      }
    });
    this.recursiveDisplayParents(this.dataSource.data);
  }

  private recursiveDisplayParents(nodes: ItemNode<SchemaElement>[]): boolean {
    for (let node of nodes) {
      if (node.item.id === this.schemaService.VIRTUAL_ROOT_NODES) {
        this.recursiveDisplayParents(node.children);
        continue;
      }
      const flatNode: ItemFlatNode<SchemaElement> = this.nestedNodeMap.get(node.item);
      if (flatNode.display) {
        return true;
      }
      if (node.children && this.recursiveDisplayParents(node.children)) {
        flatNode.display = true;
        return true;
      }
    }
    return false;
  }

  private itemContainsSearchText(item: SchemaElement, searchText: string) {
    const normalizedSearchText = normalizeString(searchText);
    return normalizeString(item.ShortName).includes(normalizedSearchText) || normalizeString(item.FieldName).includes(normalizedSearchText);
  }

  treeControl: FlatTreeControl<ItemFlatNode<SchemaElement>>;
  dataSource: MatTreeFlatDataSource<ItemNode<SchemaElement>, ItemFlatNode<SchemaElement>>;
  private treeFlattener: MatTreeFlattener<ItemNode<SchemaElement>, ItemFlatNode<SchemaElement>>;
  /** Map from nested node to flattened node. This helps us to keep the same object for selection */
  private nestedNodeMap = new Map<SchemaElement, ItemFlatNode<SchemaElement>>();
  private idIncrement = 0;
  private transformer = (node: ItemNode<SchemaElement>, level: number) => {
    const existingNode = this.nestedNodeMap.get(node.item);
    const flatNode = existingNode && existingNode.item === node.item ? existingNode : new ItemFlatNode<SchemaElement>();
    flatNode.id = `node-${this.idIncrement++}`;
    flatNode.item = node.item;
    flatNode.level = level;
    flatNode.expandable = !!node.children?.length;
    flatNode.display = true;
    this.nestedNodeMap.set(node.item, flatNode);
    return flatNode;
  };

  canDelete(schemaElement: SchemaElement) {
    return schemaElement.Origin === 'EXTERNAL' && schemaElement.Tenant === this.tenantSelectionService.getSelectedTenant().identifier;
  }

  delete(itemNode: ItemFlatNode<SchemaElement>) {
    const paths = [itemNode, ...this.treeControl.getDescendants(itemNode)].map((node) => node.item.Path);
    this.dialog
      .open<SchemaDeleteDialogComponent, SchemaDeleteDialogComponentData>(SchemaDeleteDialogComponent, { data: paths })
      .afterClosed()
      .subscribe((shouldUpdate) => {
        if (shouldUpdate) this.updateSchema();
      });
  }

  previewDelete(item: SchemaElement) {
    this.rows
      .map((row) => row.nativeElement as HTMLElement)
      .filter((row) => row.classList.contains(`path-${item.Path}`) || row.className.includes(`path-${item.Path}.`))
      .forEach((row) => row.classList.add('selected'));
  }

  hidePreviewDelete() {
    this.rows.forEach((row) => (row.nativeElement as HTMLElement).classList.remove('selected'));
  }
}
