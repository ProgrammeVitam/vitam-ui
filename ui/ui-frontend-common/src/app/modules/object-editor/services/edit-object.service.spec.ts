import { TestBed, inject, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Collection, Schema } from '../../models';
import { DisplayRule } from '../../object-viewer/models';
import { DisplayRuleHelperService } from '../../object-viewer/services/display-rule-helper.service';
import { MockSchemaService } from '../../object-viewer/services/mock-schema.service';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';
import { TypeService } from '../../object-viewer/services/type.service';
import { EditObject } from '../models/edit-object.model';
import { EditObjectService } from './edit-object.service';
import { SchemaService } from './schema.service';
import { TemplateService } from './template.service';

describe('EditObjectService', () => {
  let service: EditObjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      providers: [TypeService, MockSchemaService, SchemaElementToDisplayRuleService, DisplayRuleHelperService],
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
          expect(editObject.children.length).toEqual(16);
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
          expect(editObject.children.length).toEqual(16);
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
      inject(
        [MockSchemaService, TemplateService, SchemaService],
        (schemaService: MockSchemaService, templetaService: TemplateService, ss: SchemaService) => {
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
            const templatedSchema = service.createaTemplateSchema(template, subschema);
            const editObject = service.editObject(path, projectedData, template, templatedSchema);

            expect(editObject).toBeTruthy();
            expect(editObject.children.length).toEqual(13);
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
        },
      ),
    ));

    it('should create projected nested object and add new items', waitForAsync(
      inject(
        [MockSchemaService, TemplateService, SchemaService],
        (schemaService: MockSchemaService, templetaService: TemplateService, ss: SchemaService) => {
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
            const templatedSchema = service.createaTemplateSchema(template, subschema);
            const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

            expect(editObject).toBeTruthy();
            expect(editObject.children.length).toEqual(13);
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

            const addresseesEditObject = editObject.children
              .find((eo) => eo.key === 'Actors')
              .children.find((eo) => eo.key === 'Addressee');

            expect(addresseesEditObject).toBeTruthy();

            addresseesEditObject.actions.add();

            expect(addresseesEditObject.children.length).toEqual(2);
          });
        },
      ),
    ));

    it('should create projected nested object and remove an item', waitForAsync(
      inject(
        [MockSchemaService, TemplateService, SchemaService],
        (schemaService: MockSchemaService, templetaService: TemplateService, ss: SchemaService) => {
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
            const templatedSchema = service.createaTemplateSchema(template, subschema);
            const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

            expect(editObject).toBeTruthy();
            expect(editObject.children.length).toEqual(13);
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

            const addresseesEditObject = editObject.children
              .find((eo) => eo.key === 'Actors')
              .children.find((eo) => eo.key === 'Addressee');

            expect(addresseesEditObject).toBeTruthy();

            addresseesEditObject.actions.removeAt(0);

            expect(addresseesEditObject.children.length).toEqual(0);
          });
        },
      ),
    ));

    it('should create projected nested object and do complexe operations on arrays', waitForAsync(
      inject(
        [MockSchemaService, TemplateService, SchemaService],
        (schemaService: MockSchemaService, templetaService: TemplateService, ss: SchemaService) => {
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
            const templatedSchema = service.createaTemplateSchema(template, subschema);
            const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

            expect(editObject).toBeTruthy();
            expect(editObject.children.length).toEqual(13);
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

            const addresseesEditObject = editObject.children
              .find((eo) => eo.key === 'Actors')
              .children.find((eo) => eo.key === 'Addressee');

            expect(addresseesEditObject).toBeTruthy();

            addresseesEditObject.actions.add({ FirstName: 'John' });
            addresseesEditObject.actions.add({ FirstName: 'Daniel' });
            addresseesEditObject.actions.add({ FirstName: 'Alexandre' });

            expect(addresseesEditObject.children.length).toEqual(4);

            addresseesEditObject.actions.removeAt(2);

            expect(addresseesEditObject.children.length).toEqual(3);
            expect(addresseesEditObject.control.value).not.toEqual(
              jasmine.arrayContaining([
                jasmine.objectContaining({
                  FirstName: 'Daniel',
                }),
              ]),
            );
          });
        },
      ),
    ));

    it('should create with empty data', waitForAsync(
      inject(
        [MockSchemaService, TemplateService, SchemaService],
        (schemaService: MockSchemaService, templetaService: TemplateService, ss: SchemaService) => {
          schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
            const path = '';
            const originalData = {};
            const template: DisplayRule[] = [];
            const projectedData = templetaService.toProjected(originalData, template);
            const subschema = schema.filter((element) => element.Category === 'DESCRIPTION');
            const templatedSchema = service.createaTemplateSchema(template, subschema);
            const editObject: EditObject = service.editObject(path, projectedData, template, templatedSchema);

            expect(editObject).toBeTruthy();
            expect(editObject.children.length).toEqual(16);
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
                  children: [],
                }),
              ]),
            );
          });
        },
      ),
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
      const template = [];
      const schema = [];
      const output = service.createaTemplateSchema(template, schema);
      const expected = [];

      expect(output).toEqual(expected);
    });

    it('should create template schema without template and with schema', () => {
      const template = [];
      const schema: Schema = [
        {
          Path: '',
          FieldName: '',
          ApiField: '',
          Type: 'OBJECT',
          DataType: 'OBJECT',
          Origin: 'VIRTUAL',
          Indexed: false,
          StringSize: 'MEDIUM',
          Cardinality: 'ONE',
          SedaVersions: [],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: '',
          Category: 'DESCRIPTION',
        },
      ];
      const ouput = service.createaTemplateSchema(template, schema);
      const expected: Schema = [
        {
          Path: '',
          FieldName: '',
          ApiField: '',
          Type: 'OBJECT',
          DataType: 'OBJECT',
          Origin: 'VIRTUAL',
          Indexed: false,
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
      const ouput = service.createaTemplateSchema(template, schema);
      const expected: Schema = [];

      expect(ouput).toEqual(expected);
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
        const templatedSchema = service.createaTemplateSchema(template, subschema).sort((a, b) => {
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
        const templatedSchema = service.createaTemplateSchema(template, subschema);

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
        const templatedSchema = service.createaTemplateSchema(template, subschema);
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
