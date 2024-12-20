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
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { finalize, Subject, Subscription } from 'rxjs';
import { ItemFlatNode, ItemNode, ItemNodeUtils, SchemaElement, SchemaService, TableFilterModule } from 'vitamui-library';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatLegacyButtonModule as MatButtonModule } from '@angular/material/legacy-button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    TranslateModule,
    TableFilterModule,
    MatButtonToggleModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  selector: 'app-schema-list',
  templateUrl: './schema-list.component.html',
  styleUrls: ['./schema-list.component.scss'],
})
export class SchemaListComponent implements OnInit, OnDestroy {
  @Input()
  set searchText(searchText: string) {
    this.searchChange.next(searchText);
  }

  pending = false;
  private readonly searchChange = new Subject<string>();
  private readonly subscriptions = new Subscription();

  constructor(
    public schemaService: SchemaService,
    private translateService: TranslateService,
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
    this.pending = true;
    this.schemaService
      .getSchemaTreeByCategory()
      .pipe(finalize(() => (this.pending = false)))
      .subscribe({
        next: (data: ItemNode<SchemaElement>[]) => {
          this.dataSource.data = data;
        },
        error: (e) => console.error(e),
      });
    this.subscriptions.add(this.searchChange.subscribe((searchText) => this.filterBySearchText(searchText)));
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
  protected defaultSelectedFilters: Array<string> = this.availableFilters.map((e) => e.name);

  filterByOrigin(selectedFilters: string[]) {
    this.treeControl.dataNodes.forEach((node: ItemFlatNode<SchemaElement>) => {
      if (node.item.id === this.schemaService.VIRTUAL_ROOT_NODES) {
        node.display = true;
      } else {
        node.display = selectedFilters.includes(node.item.Origin);
      }
    });
  }

  private recursiveDisplayParents(nodes: ItemNode<SchemaElement>[]): boolean {
    for (var node of nodes) {
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

  filterBySearchText(searchText: string) {
    let reset = !searchText || searchText === '';
    searchText = searchText.toLowerCase();
    this.treeControl.dataNodes.forEach((node: ItemFlatNode<SchemaElement>) => {
      if (reset || node.item.id === this.schemaService.VIRTUAL_ROOT_NODES) {
        node.display = true;
      } else {
        node.display = this.itemContainsSearchText(node.item, searchText);
      }
    });
    this.recursiveDisplayParents(this.dataSource.data);
  }

  private itemContainsSearchText(item: SchemaElement, searchText: string) {
    return item.ShortName.toLowerCase().includes(searchText) || item.FieldName.toLowerCase().includes(searchText);
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
}
