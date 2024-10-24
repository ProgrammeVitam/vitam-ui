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
 *
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SchemaApiService } from '../api/schema-api.service';
import { Collection, Schema } from '../models';
import { map } from 'rxjs/operators';
import { ItemNode } from '../components/autocomplete';
import { SchemaElement } from '../models/schema/schema-element.model';

@Injectable({
  providedIn: 'root',
})
export class SchemaService {
  constructor(private api: SchemaApiService) {}

  public getSchemas(collections: Collection[]): Observable<Schema[]> {
    return this.api.getSchemas(collections);
  }

  public getSchema(collection: Collection): Observable<Schema> {
    return this.api.getSchema(collection);
  }

  public getArchiveUnitProfileSchema(archiveUnitProfileId: string) {
    return this.api.getArchiveUnitProfileSchema(archiveUnitProfileId);
  }

  public getDescriptiveSchemaTree(): Observable<ItemNode<SchemaElement>[]> {
    const recursiveSort = (node: ItemNode<SchemaElement>) => {
      node.children.sort((n1, n2) =>
        n1.children.length && !n2.children.length
          ? 1
          : !n1.children.length && n2.children.length
            ? -1
            : n1.item.ShortName.localeCompare(n2.item.ShortName),
      );
      node.children.forEach((n) => recursiveSort(n));
    };

    const removeLeavesWithTypeObject = (node: ItemNode<SchemaElement>) => {
      node.children = node.children.filter((child) => !(child.item.Type === 'OBJECT' && !child.children.length));
      node.children.forEach((child) => removeLeavesWithTypeObject(child));
    };

    return this.getSchema(Collection.ARCHIVE_UNIT).pipe(
      map((schema) => {
        const rootNode = schema
          .filter((e) => (e.Category === 'DESCRIPTION' || e.Origin === 'EXTERNAL') && e.FieldName !== '_sp' && e.FieldName !== '_sps')
          .reduce(
            (acc, element) => {
              const path = element.Path.split('.').slice(0, -1);
              const parentNode = path.reduce((currentItem, p) => currentItem.children.find((n) => n.item.FieldName === p), acc) || acc;
              parentNode.children.push({
                item: element,
                children: [],
              });
              return acc;
            },
            { children: [] } as ItemNode<SchemaElement>,
          );

        removeLeavesWithTypeObject(rootNode);

        recursiveSort(rootNode);
        return rootNode.children;
      }),
    );
  }
}
