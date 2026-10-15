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
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Collection } from '../models/schema/collection.enum';
import { ProfiledSchemaElement, SchemaElement } from '../models/schema/schema-element.model';
import { Schema } from '../models/schema/schema.interface';
import { Control } from '../models/schema/control.model';
import { EffectiveCardinality } from '../object-viewer/types';
import { map } from 'rxjs/operators';
import { ItemNode } from '../components/autocomplete/utils/item-node.interface';
import mockSchema from './mock-schema.data.json';

@Injectable()
export class MockSchemaService {
  private readonly schema = mockSchema as ProfiledSchemaElement[];

  getSchema(_collection: Collection): Observable<Schema> {
    return of(this.schema);
  }

  getSchemas(_collections: Collection[]): Observable<Schema[]> {
    return of([this.schema]);
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
          .filter((e) => e.Category === 'DESCRIPTION' || e.Origin === 'EXTERNAL')
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

  public getArchiveUnitProfileSchema(archiveUnitProfileId: string): Observable<Schema> {
    if (!archiveUnitProfileId) return of([]);

    let archiveUnitProfileSchema: Schema = JSON.parse(JSON.stringify(this.schema));

    archiveUnitProfileSchema = this.requiredConstraints(archiveUnitProfileSchema);
    archiveUnitProfileSchema = this.monoValuedConstraints(archiveUnitProfileSchema);
    archiveUnitProfileSchema = this.enumsConstraints(archiveUnitProfileSchema);
    archiveUnitProfileSchema = this.regexConstraints(archiveUnitProfileSchema);
    archiveUnitProfileSchema = this.cardinalityZeroConstrains(archiveUnitProfileSchema);

    return of(archiveUnitProfileSchema);
  }

  private readonly applyConstraints = (schema: Schema, path: string, control: Control, cardinality?: EffectiveCardinality) => {
    const index = schema.findIndex((e) => e.Path === path);

    if (index < 0) return schema;

    const element = schema[index];
    const next: ProfiledSchemaElement = {
      ...element,
      Control: control,
      EffectiveCardinality: cardinality || element.Cardinality,
    };

    return [...schema.slice(0, index), next, ...schema.slice(index + 1)];
  };

  // C1 - https://assistance.programmevitam.fr/plugins/tracker/?aid=13004
  private readonly requiredConstraints = (schema: Schema): Schema => {
    schema = this.applyConstraints(schema, 'Addressee', null, 'ONE_REQUIRED');
    schema = this.applyConstraints(schema, 'Addressee.BirthPlace', null, 'ONE_REQUIRED');
    schema = this.applyConstraints(schema, 'Addressee.FirstName', null, 'ONE_REQUIRED');

    schema = this.applyConstraints(schema, 'Agent', null, 'MANY_REQUIRED');
    return schema;
  };

  // C2 - https://assistance.programmevitam.fr/plugins/tracker/?aid=13004
  private monoValuedConstraints(schema: Schema): Schema {
    schema = this.applyConstraints(schema, 'Description', null, 'ONE_REQUIRED');
    schema = this.applyConstraints(schema, 'Writer', null, 'ONE');
    schema = this.applyConstraints(schema, 'Writer.Identifier', null, 'ONE');

    return schema;
  }

  // C3 - https://assistance.programmevitam.fr/plugins/tracker/?aid=13004
  private enumsConstraints(schema: Schema): Schema {
    schema = this.applyConstraints(schema, 'DescriptionLevel', {
      Type: 'SELECT',
      Values: ['Fonds', 'Subfonds', 'Class', 'Collection', 'Series', 'Subseries', 'RecordGrp', 'SubGrp', 'File', 'Item'],
    });

    schema = this.applyConstraints(
      schema,
      'Tag',
      {
        Type: 'SELECT',
        Values: ['Titi', 'Toto'],
        Comment: 'Titi ou Toto',
      },
      'ONE_REQUIRED',
    );

    schema = this.applyConstraints(schema, 'SigningInformation.SigningRole', {
      Type: 'SELECT',
      Values: ['SignedDocument', 'Timestamp', 'Signature', 'AdditionalProof'],
    });

    return schema;
  }

  // C4 - https://assistance.programmevitam.fr/plugins/tracker/?aid=13004
  private regexConstraints(schema: Schema): Schema {
    schema = this.applyConstraints(schema, 'Title', {
      Type: 'REGEX',
      Value: '^Fichier.*',
      Comment: "Doit commencer par 'Fichier'",
    });
    schema = this.applyConstraints(schema, 'Writer.Function', {
      Type: 'REGEX',
      Value: '^FN_.*',
      Comment: "Doit commencer par 'FN_'",
    });

    return schema;
  }

  // C7 - https://assistance.programmevitam.fr/plugins/tracker/?aid=13004
  private cardinalityZeroConstrains(schema: Schema): Schema {
    schema = this.applyConstraints(schema, 'Sender', null, 'ZERO');
    schema = this.applyConstraints(schema, 'Writer.BirthPlace', null, 'ZERO');
    schema = this.applyConstraints(schema, 'Writer.Nationality', null, 'ZERO');
    schema = this.applyConstraints(schema, 'Writer.FirstName', null, 'ZERO');
    schema = this.applyConstraints(schema, 'Writer.BirthName', null, 'ZERO');

    return schema;
  }
}
