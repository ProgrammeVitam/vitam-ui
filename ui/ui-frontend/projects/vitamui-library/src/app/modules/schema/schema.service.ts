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
import { Injectable, inject } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { SchemaApiService } from '../api/schema-api.service';
import { Collection, Schema } from '../models';
import { map } from 'rxjs/operators';
import { SchemaElement } from '../models/schema/schema-element.model';
import { ItemNode } from '../components/autocomplete/utils/item-node.interface';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root',
})
export class SchemaService {
  private api = inject(SchemaApiService);
  private translateService = inject(TranslateService);

  selectedPath$ = new Subject<string>();

  public getSchemas(collections: Collection[]): Observable<Schema[]> {
    return this.api.getSchemas(collections);
  }

  public getSchema(collection: Collection): Observable<Schema> {
    return this.api.getSchema(collection);
  }

  public deleteSchema(paths: string[]): Observable<string> {
    return this.api.deleteSchema(paths);
  }

  public getArchiveUnitProfileSchema(archiveUnitProfileId: string) {
    return this.api.getArchiveUnitProfileSchema(archiveUnitProfileId);
  }

  private recursiveSort = (node: ItemNode<SchemaElement>) => {
    node.children.sort((n1, n2) =>
      n1.children.length && !n2.children.length
        ? 1
        : !n1.children.length && n2.children.length
          ? -1
          : n1.item.ShortName.localeCompare(n2.item.ShortName),
    );
    node.children.forEach((n) => this.recursiveSort(n));
  };

  private removeLeavesWithTypeObject = (node: ItemNode<SchemaElement>) => {
    node.children = node.children.filter((child) => !(child.item.Type === 'OBJECT' && !child.children.length));
    node.children.forEach((child) => this.removeLeavesWithTypeObject(child));
  };

  public getDescriptiveSchemaTree(): Observable<ItemNode<SchemaElement>[]> {
    return this.getSchema(Collection.ARCHIVE_UNIT).pipe(map(this.buildTree));
  }

  public getMetadataKeysByKeys(keys: string[], schema: ItemNode<SchemaElement>[]) {
    let res: ItemNode<SchemaElement>[] = [];
    for (const node of schema) {
      if (keys.includes(node.item.ApiField)) {
        res.push(node);
      }
      if (node.children) {
        const child = this.getMetadataKeysByKeys(keys, node.children);
        if (child) {
          res = [...res, ...child];
        }
      }
    }
    return res;
  }

  private buildTree: (schema: Schema) => ItemNode<SchemaElement>[] = (schema: Schema): ItemNode<SchemaElement>[] => {
    const rootNode = schema
      .filter((e) => (e.Category === 'DESCRIPTION' || e.Origin === 'EXTERNAL') && e.FieldName !== '_sp' && e.FieldName !== '_sps')
      .reduce(this.buildTreeReducer, { children: [] } as ItemNode<SchemaElement>);
    this.removeLeavesWithTypeObject(rootNode);
    this.recursiveSort(rootNode);
    return rootNode.children;
  };

  private buildTreeReducer = (acc: ItemNode<SchemaElement>, element: SchemaElement) => {
    const path = element.Path.split('.').slice(0, -1);
    const parentNode = path.reduce((currentItem, p) => currentItem.children.find((n) => n.item.FieldName === p), acc) || acc;
    parentNode.children.push({
      item: element,
      children: [],
    });
    return acc;
  };

  public getSchemaTree(): Observable<ItemNode<SchemaElement>[]> {
    return this.getSchema(Collection.ARCHIVE_UNIT).pipe(
      map((schema) => {
        const rootNode = schema
          .filter((e) => e.FieldName !== '_sp' && e.FieldName !== '_sps')
          .reduce(this.buildTreeReducer, { children: [] } as ItemNode<SchemaElement>);
        this.recursiveSort(rootNode);
        return rootNode.children;
      }),
    );
  }

  public readonly VIRTUAL_ROOT_NODES = 'VIRTUAL_ROOT_NODES';
  private groupByCategory: (nodes: ItemNode<SchemaElement>[]) => ItemNode<SchemaElement>[] = (
    nodes: ItemNode<SchemaElement>[],
  ): ItemNode<SchemaElement>[] => {
    const roots = {
      DESCRIPTION: {
        item: {
          id: this.VIRTUAL_ROOT_NODES,
          ShortName: this.translateService.instant('ONTOLOGY.NODES.DESCRIPTION'),
          Category: 'DESCRIPTION',
        } as SchemaElement,
        children: [],
      } as ItemNode<SchemaElement>,
      MANAGEMENT: {
        item: {
          id: this.VIRTUAL_ROOT_NODES,
          ShortName: this.translateService.instant('ONTOLOGY.NODES.MANAGEMENT'),
          Category: 'MANAGEMENT',
        } as SchemaElement,
        children: [],
      } as ItemNode<SchemaElement>,
      OTHER: {
        item: {
          id: this.VIRTUAL_ROOT_NODES,
          ShortName: this.translateService.instant('ONTOLOGY.NODES.OTHER'),
          Category: 'OTHER',
        } as SchemaElement,
        children: [],
      } as ItemNode<SchemaElement>,
    };
    nodes.forEach((node: ItemNode<SchemaElement>) => {
      roots[node.item.Category].children.push(node);
    });
    return Object.values(roots) as ItemNode<SchemaElement>[];
  };

  public getSchemaTreeByCategory(): Observable<ItemNode<SchemaElement>[]> {
    return this.getSchemaTree().pipe(map(this.groupByCategory));
  }
}
