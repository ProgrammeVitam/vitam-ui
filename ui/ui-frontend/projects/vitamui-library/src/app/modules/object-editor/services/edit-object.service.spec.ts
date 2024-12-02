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
import { inject, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { EMPTY, from, mergeMap, toArray } from 'rxjs';
import { LoggerModule } from '../../logger';
import { Collection, ProfiledSchemaElement, Schema } from '../../models';
import { DisplayRule } from '../../object-viewer/models';
import { DisplayRuleHelperService } from '../../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';
import { TypeService } from '../../object-viewer/services/type.service';
import { MockSchemaService } from '../../schema/mock-schema.service';
import { EditObject } from '../models/edit-object.model';
import { EditObjectService } from './edit-object.service';
import { SchemaService } from './schema.service';
import { TemplateService } from './template.service';
import { Control } from '../../models/schema/control.model';
import { Cardinality, EffectiveCardinality } from '../../object-viewer/types';
import { filter, map } from 'rxjs/operators';

describe('EditObjectService', () => {
  let service: EditObjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, LoggerModule.forRoot()],
      providers: [
        TypeService,
        MockSchemaService,
        SchemaElementToDisplayRuleService,
        DisplayRuleHelperService,
        { provide: TranslateService, useValue: { instant: () => EMPTY } },
      ],
    });
    service = TestBed.inject(EditObjectService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  describe('EditObject', () => {
    it('should create simple editObject', waitForAsync(
      inject([MockSchemaService], (schemaService: MockSchemaService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const data = { Title: 'Hello' };
          const template: DisplayRule[] = [{ Path: 'Title', ui: { Path: 'Title', component: 'textfield' } }];
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const editObject = service.editObject(path, data, template, subschema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(50);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'Addressee',
                'Agent',
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                'Transmitter',
                'Writer',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                displayRule: jasmine.objectContaining({
                  Path: 'Title',
                }),
              }),
            ]),
          );
        });
      }),
    ));

    it('should create nested editObject', waitForAsync(
      inject([MockSchemaService], (schemaService: MockSchemaService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const data = {
            Title: 'Hello',
            Addressee: [
              {
                BirthDate: '01/01/2000',
              },
            ],
          };
          const template: DisplayRule[] = [
            { Path: 'Title', ui: { Path: 'Title', component: 'textfield' } },
            { Path: 'Addressee', ui: { Path: 'Addressee', component: 'group' } },
          ];
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const editObject = service.editObject(path, data, template, subschema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(50);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'Addressee',
                'Agent',
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                'Transmitter',
                'Writer',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                path: 'Addressee',
                displayRule: jasmine.objectContaining({
                  Path: 'Addressee',
                  ui: jasmine.objectContaining({
                    component: 'group',
                  }),
                }),
              }),
            ]),
          );

          expect(editObject.children.find((node) => node.path === 'Addressee').children.length).toBeGreaterThan(0);
        });
      }),
    ));

    it('should create projected nested object', waitForAsync(
      inject([MockSchemaService, TemplateService, SchemaService], (schemaService: MockSchemaService, templetaService: TemplateService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const originalData = {
            Title: 'Hello',
            Addressee: [{ BirthDate: '01/01/2000' }],
          };
          const template: DisplayRule[] = [
            { Path: 'Title', ui: { Path: 'Title', component: 'textfield' } },
            { Path: null, ui: { Path: 'Actors', component: 'group' } },
            { Path: 'Addressee', ui: { Path: 'Actors.Addressee', component: 'group' } },
            { Path: 'Agent', ui: { Path: 'Actors.Agent', component: 'group' } },
            { Path: 'Transmitter', ui: { Path: 'Actors.Transmitter', component: 'group' } },
            { Path: 'Writer', ui: { Path: 'Actors.Writer', component: 'group' } },
          ];
          const projectedData = templetaService.toProjected(originalData, template);
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const templatedSchema = service.createTemplateSchema(template, subschema);
          const editObject = service.editObject(path, projectedData, template, templatedSchema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(47);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                path: 'Actors',
                default: {
                  Addressee: [{ BirthDate: '01/01/2000' }],
                  Agent: [],
                  Transmitter: [],
                  Writer: [],
                },
                children: jasmine.arrayContaining([
                  jasmine.objectContaining({ path: 'Actors.Addressee' }),
                  jasmine.objectContaining({ path: 'Actors.Agent' }),
                  jasmine.objectContaining({ path: 'Actors.Transmitter' }),
                  jasmine.objectContaining({ path: 'Actors.Writer' }),
                ]),
              }),
            ]),
          );
        });
      }),
    ));

    it('should create projected nested object and add new items', waitForAsync(
      inject([MockSchemaService, TemplateService, SchemaService], (schemaService: MockSchemaService, templetaService: TemplateService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const originalData = {
            Title: 'Hello',
            Addressee: [{ BirthDate: '01/01/2000' }],
          };
          const template: DisplayRule[] = [
            { Path: 'Title', ui: { Path: 'Title', component: 'textfield' } },
            { Path: null, ui: { Path: 'Actors', component: 'group' } },
            { Path: 'Addressee', ui: { Path: 'Actors.Addressee', component: 'group' } },
            { Path: 'Agent', ui: { Path: 'Actors.Agent', component: 'group' } },
            { Path: 'Transmitter', ui: { Path: 'Actors.Transmitter', component: 'group' } },
            { Path: 'Writer', ui: { Path: 'Actors.Writer', component: 'group' } },
          ];
          const projectedData = templetaService.toProjected(originalData, template);
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const templatedSchema = service.createTemplateSchema(template, subschema);
          const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(47);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                path: 'Actors',
                default: {
                  Addressee: [{ BirthDate: '01/01/2000' }],
                  Agent: [],
                  Transmitter: [],
                  Writer: [],
                },
                children: jasmine.arrayContaining([
                  jasmine.objectContaining({ path: 'Actors.Addressee' }),
                  jasmine.objectContaining({ path: 'Actors.Agent' }),
                  jasmine.objectContaining({ path: 'Actors.Transmitter' }),
                  jasmine.objectContaining({ path: 'Actors.Writer' }),
                ]),
              }),
            ]),
          );

          const addresseesEditObject = editObject.children.find((eo) => eo.key === 'Actors').children.find((eo) => eo.key === 'Addressee');

          expect(addresseesEditObject).toBeTruthy();

          addresseesEditObject.actions.add.handler();

          expect(addresseesEditObject.children.length).toEqual(2);
        });
      }),
    ));

    it('should create projected nested object and remove an item', waitForAsync(
      inject([MockSchemaService, TemplateService, SchemaService], (schemaService: MockSchemaService, templetaService: TemplateService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const originalData = {
            Title: 'Hello',
            Addressee: [{ BirthDate: '01/01/2000' }],
          };
          const template: DisplayRule[] = [
            { Path: 'Title', ui: { Path: 'Title', component: 'textfield' } },
            { Path: null, ui: { Path: 'Actors', component: 'group' } },
            { Path: 'Addressee', ui: { Path: 'Actors.Addressee', component: 'group' } },
            { Path: 'Agent', ui: { Path: 'Actors.Agent', component: 'group' } },
            { Path: 'Transmitter', ui: { Path: 'Actors.Transmitter', component: 'group' } },
            { Path: 'Writer', ui: { Path: 'Actors.Writer', component: 'group' } },
          ];
          const projectedData = templetaService.toProjected(originalData, template);
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const templatedSchema = service.createTemplateSchema(template, subschema);
          const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(47);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                path: 'Actors',
                default: {
                  Addressee: [{ BirthDate: '01/01/2000' }],
                  Agent: [],
                  Transmitter: [],
                  Writer: [],
                },
                children: jasmine.arrayContaining([
                  jasmine.objectContaining({ path: 'Actors.Addressee' }),
                  jasmine.objectContaining({ path: 'Actors.Agent' }),
                  jasmine.objectContaining({ path: 'Actors.Transmitter' }),
                  jasmine.objectContaining({ path: 'Actors.Writer' }),
                ]),
              }),
            ]),
          );

          const addresseesEditObject = editObject.children.find((eo) => eo.key === 'Actors').children.find((eo) => eo.key === 'Addressee');

          expect(addresseesEditObject).toBeTruthy();

          addresseesEditObject.children[0].actions.remove.handler();

          expect(addresseesEditObject.children.length).toEqual(0);
        });
      }),
    ));

    it('should create projected nested object and do complex operations on arrays', waitForAsync(
      inject([MockSchemaService, TemplateService, SchemaService], (schemaService: MockSchemaService, templetaService: TemplateService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const originalData = {
            Title: 'Hello',
            Addressee: [{ BirthDate: '01/01/2000' }],
          };
          const template: DisplayRule[] = [
            { Path: 'Title', ui: { Path: 'Title', component: 'textfield' } },
            { Path: null, ui: { Path: 'Actors', component: 'group' } },
            { Path: 'Addressee', ui: { Path: 'Actors.Addressee', component: 'group' } },
            { Path: 'Agent', ui: { Path: 'Actors.Agent', component: 'group' } },
            { Path: 'Transmitter', ui: { Path: 'Actors.Transmitter', component: 'group' } },
            { Path: 'Writer', ui: { Path: 'Actors.Writer', component: 'group' } },
          ];
          const projectedData = templetaService.toProjected(originalData, template);
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const templatedSchema = service.createTemplateSchema(template, subschema);
          const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(47);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                path: 'Actors',
                default: {
                  Addressee: [{ BirthDate: '01/01/2000' }],
                  Agent: [],
                  Transmitter: [],
                  Writer: [],
                },
                children: jasmine.arrayContaining([
                  jasmine.objectContaining({ path: 'Actors.Addressee' }),
                  jasmine.objectContaining({ path: 'Actors.Agent' }),
                  jasmine.objectContaining({ path: 'Actors.Transmitter' }),
                  jasmine.objectContaining({ path: 'Actors.Writer' }),
                ]),
              }),
            ]),
          );

          const addresseesEditObject = editObject.children.find((eo) => eo.key === 'Actors').children.find((eo) => eo.key === 'Addressee');

          expect(addresseesEditObject).toBeTruthy();

          addresseesEditObject.actions.add.handler({ FirstName: 'John' });
          addresseesEditObject.actions.add.handler({ FirstName: 'Daniel' });
          addresseesEditObject.actions.add.handler({ FirstName: 'Alexandre' });

          expect(addresseesEditObject.children.length).toEqual(4);

          addresseesEditObject.children[2].actions.remove.handler();

          expect(addresseesEditObject.children.length).toEqual(3);
          expect(addresseesEditObject.control.value).not.toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                FirstName: 'Daniel',
              }),
            ]),
          );
        });
      }),
    ));

    it('should create with empty data', waitForAsync(
      inject([MockSchemaService, TemplateService, SchemaService], (schemaService: MockSchemaService, templetaService: TemplateService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const originalData = {};
          const template: DisplayRule[] = [];
          const projectedData = templetaService.toProjected(originalData, template);
          const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
          const templatedSchema = service.createTemplateSchema(template, subschema);
          const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

          expect(editObject).toBeTruthy();
          expect(editObject.children.length).toEqual(50);
          expect(editObject.children).toEqual(
            jasmine.arrayContaining(
              [
                'Addressee',
                'Agent',
                'AuthorizedAgent',
                'Coverage',
                'DescriptionLevel',
                'Event',
                'Gps',
                'Recipient',
                'Sender',
                'Signature',
                'SigningInformation',
                'Title',
                'Transmitter',
                'Writer',
                '#originating_agency',
                '#originating_agencies',
              ].map((path) => jasmine.objectContaining({ path })),
            ),
          );
          expect(editObject.children).toEqual(
            jasmine.arrayContaining([
              jasmine.objectContaining({
                path: 'Addressee',
                kind: 'object-array',
              }),
            ]),
          );
          expect(editObject.children.length).toEqual(50);
        });
      }),
    ));

    it('should create with nested arrays', waitForAsync(
      inject([TemplateService, SchemaService], () => {
        const schema: Schema = [
          {
            Path: 'Invoice',
            Cardinality: 'MANY',
            FieldName: 'Invoice',
            ShortName: 'Facture',
            Description: 'Informations de facturation',
            Type: 'OBJECT',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice',
            DataType: 'OBJECT',
            ApiField: 'Invoice',
            SedaVersions: [],
          },
          {
            Path: 'Invoice.Provider',
            Cardinality: 'MANY',
            FieldName: 'Provider',
            ShortName: 'Provider',
            Description: 'Émetteur de la facture',
            Type: 'OBJECT',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice.Provider',
            DataType: 'OBJECT',
            ApiField: 'Provider',
            SedaVersions: [],
          },
          {
            Path: 'Invoice.Provider.MyKeyword',
            StringSize: 'MEDIUM',
            Cardinality: 'ONE_REQUIRED',
            FieldName: 'MyKeyword',
            ShortName: 'My keyword',
            Description: 'Extension au SEDA. Elément de type mot clé',
            Type: 'KEYWORD',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice.Provider.MyKeyword',
            DataType: 'STRING',
            ApiField: 'MyKeyword',
            SedaVersions: [],
          },
          {
            Path: 'Invoice.Provider.MyText',
            StringSize: 'MEDIUM',
            Cardinality: 'ONE',
            FieldName: 'MyText',
            ShortName: 'My text',
            Description: 'Extension au SEDA. Elément de type texte',
            Type: 'TEXT',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice.Provider.MyText',
            DataType: 'STRING',
            ApiField: 'MyText',
            SedaVersions: [],
          },
          {
            Path: 'Invoice.Provider.MyDate',
            Cardinality: 'MANY',
            FieldName: 'MyDate',
            ShortName: 'My date',
            Description: 'Extension au SEDA. Elément de type date',
            Type: 'DATE',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice.Provider.MyDate',
            DataType: 'DATETIME',
            ApiField: 'MyDate',
            SedaVersions: [],
          },
          {
            Path: 'Invoice.Provider.MyDouble',
            Cardinality: 'ONE',
            FieldName: 'MyDouble',
            ShortName: 'My double',
            Description: 'Extension au SEDA. Elément de type décimal',
            Type: 'DOUBLE',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice.Provider.MyDouble',
            DataType: 'DOUBLE',
            ApiField: 'MyDouble',
            SedaVersions: [],
          },
          {
            Path: 'Invoice.Provider.MyEnum',
            Cardinality: 'ONE',
            FieldName: 'MyEnum',
            ShortName: 'My enum',
            Description: 'Extension au SEDA. Elément de type énumératif',
            Type: 'ENUM',
            Origin: 'EXTERNAL',
            Collection: Collection.ARCHIVE_UNIT,
            Category: 'OTHER',
            ApiPath: 'Invoice.Provider.MyEnum',
            DataType: 'STRING',
            ApiField: 'MyEnum',
            SedaVersions: [],
          },
        ];
        const path = '';
        const originalData = {
          Invoice: [
            {
              Provider: [
                {
                  MyText: 'Hello',
                },
              ],
            },
          ],
        };
        const template: DisplayRule[] = [];
        const templatedSchema = service.createTemplateSchema(template, schema);
        const editObject: EditObject = service.editObject(path, originalData, template, templatedSchema);

        expect(editObject).toBeTruthy();
        expect(editObject.children.length).toEqual(1);
        expect(editObject.children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'Invoice',
              kind: 'object-array',
            }),
          ]),
        );
        expect(editObject.children[0].children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'Invoice[0]',
              kind: 'object',
            }),
          ]),
        );
        expect(editObject.children[0].children[0].children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'Invoice[0].Provider',
              kind: 'object-array',
            }),
          ]),
        );
        expect(editObject.children[0].children[0].children[0].children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'Invoice[0].Provider[0]',
              kind: 'object',
            }),
          ]),
        );
        expect(editObject.children[0].children[0].children[0].children[0].children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'Invoice[0].Provider[0].MyText',
              kind: 'primitive',
              value: 'Hello',
            }),
          ]),
        );
      }),
    ));
  });

  describe('Kind', () => {
    it('should object be object kind', () => {
      expect(service.kind({})).toEqual('object');
    });

    // TODO: Determine which kind is empty array
    xit('should empty array be object-array kind', () => {
      expect(service.kind([])).toEqual('object-array');
    });

    it('should string array be primitive-array kind', () => {
      expect(service.kind(['a', 'b', 'c'])).toEqual('primitive-array');
    });

    it('should object array be object-array kind', () => {
      expect(service.kind([{}, {}, {}])).toEqual('object-array');
    });

    it('should null be object kind', () => {
      expect(service.kind(null)).toEqual('object');
    });
  });

  describe('TemplateSchema', () => {
    it('should create template schema without template and schema', () => {
      const template: DisplayRule[] = [];
      const schema: Schema = [];
      const output = service.createTemplateSchema(template, schema);
      const expected: Schema = [];

      expect(output).toEqual(expected);
    });

    it('should create template schema without template and with schema', () => {
      const template: DisplayRule[] = [];
      const schema: Schema = [
        {
          Path: '',
          FieldName: '',
          ApiField: '',
          Type: 'OBJECT',
          DataType: 'OBJECT',
          Origin: 'VIRTUAL',
          StringSize: 'MEDIUM',
          Cardinality: 'ONE',
          SedaVersions: [],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: '',
          Category: 'DESCRIPTION',
        },
      ];
      const ouput = service.createTemplateSchema(template, schema);
      const expected: Schema = [
        {
          Path: '',
          FieldName: '',
          ApiField: '',
          Type: 'OBJECT',
          DataType: 'OBJECT',
          Origin: 'VIRTUAL',
          StringSize: 'MEDIUM',
          Cardinality: 'ONE',
          SedaVersions: [],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: '',
          Category: 'DESCRIPTION',
        },
      ];

      expect(ouput.length).toEqual(1);
      expect(ouput).toEqual(expected);
    });

    it('should create template schema with template and without schema', () => {
      const template: DisplayRule[] = [
        {
          Path: '',
          ui: {
            Path: '',
            component: 'group',
          },
        },
      ];
      const schema: Schema = [];
      const ouput = service.createTemplateSchema(template, schema);
      const expected: Schema = [];

      expect(ouput).toEqual(expected);
    });
  });

  describe('Controls and cardinality modifiers', () => {
    describe('Required', () => {
      it('should primitive be required when its schema has one required cardinality modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Description';
          const kind = 'primitive';
          const cardinality: Cardinality = 'ONE';
          const effectiveCardinality = 'ONE_REQUIRED';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({
                      path,
                      kind,
                      cardinality: effectiveCardinality,
                      required: true,
                    }),
                  ]),
                );
              },
            });
        }),
      ));

      it('should primitive-array be required when its schema has one required cardinality modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Tag';
          const kind = 'primitive-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'ONE_REQUIRED';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({
                      path,
                      kind,
                      cardinality: effectiveCardinality,
                      required: true,
                    }),
                  ]),
                );
              },
            });
        }),
      ));

      it('should primitive-array be required when its schema has many required cardinality modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Tag';
          const kind = 'primitive-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'MANY_REQUIRED';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({
                      path,
                      kind,
                      cardinality: effectiveCardinality,
                      required: true,
                    }),
                  ]),
                );
              },
            });
        }),
      ));

      it('should object-array be required when its schema has one required cardinality modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Writer';
          const kind = 'object-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'ONE_REQUIRED';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({
                      path,
                      kind,
                      cardinality: effectiveCardinality,
                      required: true,
                    }),
                  ]),
                );
              },
            });
        }),
      ));

      it('should object-array be required when its schema has many required cardinality modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Writer';
          const kind = 'object-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'MANY_REQUIRED';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({
                      path,
                      kind,
                      cardinality: effectiveCardinality,
                      required: true,
                    }),
                  ]),
                );
              },
            });
        }),
      ));
    });

    describe('Pattern', () => {
      it('should primitive have pattern when its schema has set regex control modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Title';
          const kind = 'primitive';
          const cardinality: Cardinality = 'ONE';
          const effectiveCardinality = 'ONE';
          const control: Control = {
            Type: 'REGEX',
            Value: '^DCP14_[A-Z]{4}$',
          };

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, control, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({ path, kind, pattern: control.Value, cardinality: effectiveCardinality }),
                  ]),
                );
              },
            });
        }),
      ));

      it('should primitive-array have pattern when its schema has set regex control modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Tag';
          const kind = 'primitive-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'MANY';
          const control: Control = {
            Type: 'REGEX',
            Value: '^DCP14_[A-Z]{4}$',
          };

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, control, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({ path, kind, pattern: control.Value, cardinality: effectiveCardinality }),
                  ]),
                );
              },
            });
        }),
      ));
    });

    describe('Options', () => {
      it('should primitive have options when its schema has set select control modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Title';
          const kind = 'primitive';
          const cardinality: Cardinality = 'ONE';
          const effectiveCardinality = 'ONE';
          const control: Control = {
            Type: 'SELECT',
            Values: ['A', 'B'],
          };

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, control, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({ path, kind, options: control.Values, cardinality: effectiveCardinality }),
                  ]),
                );
              },
            });
        }),
      ));

      it('should primitive-array have options when its schema has set select control modifier', waitForAsync(
        inject([MockSchemaService], (schemaService: MockSchemaService) => {
          const path = 'Tag';
          const kind = 'primitive-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'MANY';
          const control: Control = {
            Type: 'SELECT',
            Values: ['A', 'B'],
          };

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, control, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => service.editObject('', {}, [], schema)),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([
                    jasmine.objectContaining({ path, kind, options: control.Values, cardinality: effectiveCardinality }),
                  ]),
                );
              },
            });
        }),
      ));
    });

    describe('Cardinality zero', () => {
      it('should primitive have inconsistent value when its schema has zero cardinality modifier', waitForAsync(
        inject([MockSchemaService, TemplateService], (schemaService: MockSchemaService, templateService: TemplateService) => {
          const path = 'Description';
          const kind = 'primitive';
          const cardinality: Cardinality = 'ONE';
          const effectiveCardinality = 'ZERO';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => {
                const data = {
                  Description: 'test',
                };
                const template = templateService.template(schema);

                return service.editObject('', data, template, schema);
              }),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([jasmine.objectContaining({ path, kind, cardinality: effectiveCardinality })]),
                );
                editObject.children.filter((eo) => eo.path === path).forEach((eo) => expect(eo.control.value).toBeNull());
              },
            });
        }),
      ));

      it('should primitive-array have inconsistent value when its schema has zero cardinality modifier', waitForAsync(
        inject([MockSchemaService, TemplateService], (schemaService: MockSchemaService, templateService: TemplateService) => {
          const path = 'Tag';
          const kind = 'primitive-array';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'ZERO';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => {
                const data = {
                  Tag: ['High', 'Medium', 'Low'],
                };
                const template = templateService.template(schema);

                return service.editObject('', data, template, schema);
              }),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual(
                  jasmine.arrayContaining([jasmine.objectContaining({ path, kind, cardinality: effectiveCardinality })]),
                );
                editObject.children.filter((eo) => eo.path === path).forEach((eo) => expect(eo.control.value).toEqual([]));
              },
            });
        }),
      ));

      it('should object-array have inconsistent value when its schema has zero cardinality modifier', waitForAsync(
        inject([MockSchemaService, TemplateService], (schemaService: MockSchemaService, templateService: TemplateService) => {
          const path = 'Writer';
          const cardinality: Cardinality = 'MANY';
          const effectiveCardinality = 'ZERO';

          schemaService
            .getSchema(Collection.ARCHIVE_UNIT)
            .pipe(
              map((schema) => applyConstraints(schema, path, null, effectiveCardinality)),
              mergeMap((schema) =>
                from(schema).pipe(
                  filter((element) => element.Path === path),
                  filter((element) => element.Cardinality === cardinality),
                  toArray(),
                  map((schema) => {
                    if (schema.length > 0) return schema;

                    throw new Error('No element in schema after filtering');
                  }),
                ),
              ),
              map((schema) => {
                const data = {
                  Writer: [{ FirstName: 'John' }],
                };
                const template = templateService.template(schema);

                return service.editObject('', data, template, schema);
              }),
            )
            .subscribe({
              next: (editObject) => {
                expect(editObject).toBeTruthy();
                expect(editObject.children).toEqual([]);
              },
            });
        }),
      ));
    });
  });

  it('should template schema be same as schema when template does not move data', waitForAsync(
    inject([MockSchemaService], (schemaService: MockSchemaService) => {
      schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
        const template: DisplayRule[] = [{ Path: 'Title', ui: { Path: 'Title', component: 'textfield' } }];
        const subschema = schema
          .filter((element) => element.Category === 'DESCRIPTION')
          .sort((a, b) => {
            if (a.ApiPath < b.ApiPath) return -1;
            if (a.ApiPath > b.ApiPath) return 1;

            return 0;
          });
        const templatedSchema = service.createTemplateSchema(template, subschema).sort((a, b) => {
          if (a.ApiPath < b.ApiPath) return -1;
          if (a.ApiPath > b.ApiPath) return 1;

          return 0;
        });

        expect(templatedSchema).toBeTruthy();
        expect(templatedSchema.length).toEqual(subschema.length);
        expect(templatedSchema).toEqual(subschema);
      });
    }),
  ));

  it('should template schema add only one node when moves a primitive from schema into a group a root level', waitForAsync(
    inject([MockSchemaService], (schemaService: MockSchemaService) => {
      schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
        const template: DisplayRule[] = [
          { Path: null, ui: { Path: 'Generalities', component: 'group' } },
          { Path: 'Title', ui: { Path: 'Generalities.Title', component: 'textfield' } },
        ];
        const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
        const templatedSchema = service.createTemplateSchema(template, subschema);

        expect(templatedSchema).toBeTruthy();
        expect(templatedSchema.length).toEqual(subschema.length + 1);
        expect(templatedSchema).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({ Path: 'Generalities', ApiPath: 'Generalities' }),
            jasmine.objectContaining({ Path: 'Title', ApiPath: 'Generalities.Title' }),
          ]),
        );
      });
    }),
  ));

  it('should template schema add only concerned nodes when moves an object array from schema into a group a root level', waitForAsync(
    inject([MockSchemaService], (schemaService: MockSchemaService) => {
      schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
        const template: DisplayRule[] = [
          { Path: null, ui: { Path: 'Generalities', component: 'group' } },
          { Path: 'Addressee', ui: { Path: 'Generalities.Addressee', component: 'group' } },
        ];
        const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
        const templatedSchema = service.createTemplateSchema(template, subschema);
        const nodeCount = subschema.reduce((acc, cur) => (cur.ApiPath.startsWith('Addressee') ? acc + 1 : acc), 0);

        expect(nodeCount).toEqual(30);
        expect(templatedSchema).toBeTruthy();
        expect(templatedSchema.length).toEqual(subschema.length + 1);

        const allPaths = templatedSchema.map((se) => se.Path);
        const uniquePaths = Array.from(new Set(allPaths));
        expect(templatedSchema.length).toEqual(uniquePaths.length);

        expect(templatedSchema).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({ Path: 'Generalities', ApiPath: 'Generalities' }),
            jasmine.objectContaining({ Path: 'Addressee', ApiPath: 'Generalities.Addressee' }),
            jasmine.objectContaining({ Path: 'Addressee.Activity', ApiPath: 'Generalities.Addressee.Activity' }),
          ]),
        );
      });
    }),
  ));
});

const applyConstraints = (schema: Schema, path: string, control: Control, cardinality?: EffectiveCardinality) => {
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
