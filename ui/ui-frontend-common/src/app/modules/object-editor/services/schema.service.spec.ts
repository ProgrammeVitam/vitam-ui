import { TestBed, inject, waitForAsync } from '@angular/core/testing';
import { Collection, Schema } from '../../models';
import { MockSchemaService } from '../../object-viewer/services/mock-schema.service';
import { PathService } from './path.service';
import { SchemaOptions, SchemaService } from './schema.service';

describe('SchemaService', () => {
  let service: SchemaService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [MockSchemaService] });
    service = TestBed.inject(SchemaService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  describe('Subschema', () => {
    it('should get archive unit schema subschema', waitForAsync(
      inject([MockSchemaService], (schemaService: MockSchemaService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const subschema = service.subschema(schema, { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' });

          expect(subschema.every((element) => element.SedaVersions.includes('2.1'))).toBeTruthy();
          expect(subschema.every((element) => element.Collection === Collection.ARCHIVE_UNIT)).toBeTruthy();
        });
      }),
    ));
  });

  describe('Data', () => {
    it('should create simple data according schema', waitForAsync(
      inject([MockSchemaService], (schemaService: MockSchemaService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const data = service.data(path, schema, { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' });

          expect(data).toEqual(
            jasmine.objectContaining({
              Title: null,
            }),
          );
        });
      }),
    ));

    it('should create data with all root level keys according schema', waitForAsync(
      inject([MockSchemaService, PathService], (schemaService: MockSchemaService, pathService: PathService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = '';
          const options: SchemaOptions = { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' };
          const subschema = service.subschema(schema, options);
          const data = service.data(path, subschema, options);
          const keys = Object.keys(data);
          const children = pathService.children(
            path,
            subschema.map((element) => element[options.pathKey]),
          );

          expect(children.length).toEqual(keys.length);
          expect(children).toEqual(keys);
        });
      }),
    ));

    it('should create archive unit addressee block according schema', waitForAsync(
      inject([MockSchemaService, PathService], (schemaService: MockSchemaService, pathService: PathService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = 'Addressee';
          const options: SchemaOptions = { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' };
          const subschema = service.subschema(schema, options);
          const data = service.data(path, subschema, options);
          const keys = Object.keys(data);
          const children = pathService.children(
            path,
            subschema.map((element) => element[options.pathKey]),
          );

          expect(children.length).toEqual(keys.length);
          expect(children.map((child) => child.split('.').pop())).toEqual(pathService.paths(data));
          expect(data).toEqual(
            jasmine.objectContaining({
              BirthDate: null,
            }),
          );
        });
      }),
    ));

    it('should create archive unit addressee birth place block according schema', waitForAsync(
      inject([MockSchemaService, PathService], (schemaService: MockSchemaService, pathService: PathService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = 'Addressee.BirthPlace';
          const options: SchemaOptions = { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' };
          const subschema = service.subschema(schema, options);
          const data = service.data(path, subschema, options);
          const keys = Object.keys(data);
          const children = pathService.children(
            path,
            subschema.map((element) => element[options.pathKey]),
          );

          expect(children.length).toEqual(keys.length);
          expect(children.map((child) => child.split('.').pop())).toEqual(pathService.paths(data));
          expect(data).toEqual(
            jasmine.objectContaining({
              Address: null,
              City: null,
              Country: null,
              Geogname: null,
            }),
          );
        });
      }),
    ));

    it('should work when a primitive node is provided', waitForAsync(
      inject([MockSchemaService, PathService], (schemaService: MockSchemaService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = 'AcquiredDate';
          const options: SchemaOptions = { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' };
          const subschema = service.subschema(schema, options);
          const data = service.data(path, subschema, options);

          expect(data).toBeFalsy();
          expect(data).toBeNull();
        });
      }),
    ));

    it('should work when a primitive array node is provided', waitForAsync(
      inject([MockSchemaService], (schemaService: MockSchemaService) => {
        schemaService.getSchema(Collection.ARCHIVE_UNIT).subscribe((schema) => {
          const path = 'Tag';
          const options: SchemaOptions = { collection: Collection.ARCHIVE_UNIT, versions: ['2.1'], pathKey: 'ApiPath' };
          const subschema = service.subschema(schema, options);
          const data = service.data(path, subschema, options);

          expect(data).toBeTruthy();
          expect(data).toEqual([]);
        });
      }),
    ));
  });

  describe('Validate', () => {
    it('should detect duplicates in schema', () => {
      const schema: Schema = [
        {
          Path: 'Title',
          FieldName: 'Title',
          ApiField: 'Title',
          Type: 'TEXT',
          Origin: 'INTERNAL',
          Indexed: true,
          Cardinality: 'ONE',
          SedaVersions: ['2.1'],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: 'Title',
          Category: 'DESCRIPTION',
        },
        {
          Path: 'Title',
          FieldName: 'Title',
          ApiField: 'Title',
          Type: 'TEXT',
          Origin: 'INTERNAL',
          Indexed: true,
          Cardinality: 'ONE',
          SedaVersions: ['2.1'],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: 'Title',
          Category: 'DESCRIPTION',
        },
      ];

      expect(() => service.validate(schema)).toThrowError(/seems have duplicates elements/gm);
    });

    it('should detect leaf with children in schema', () => {
      const schema: Schema = [
        {
          Path: 'Title',
          FieldName: 'Title',
          ApiField: 'Title',
          Type: 'TEXT',
          Origin: 'INTERNAL',
          Indexed: true,
          Cardinality: 'ONE',
          SedaVersions: ['2.1'],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: 'Title',
          Category: 'DESCRIPTION',
        },
        {
          Path: 'Title.keyword',
          FieldName: 'Title.keyword',
          ApiField: 'Title.keyword',
          Type: 'TEXT',
          Origin: 'INTERNAL',
          Indexed: true,
          Cardinality: 'ONE',
          SedaVersions: ['2.1'],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: 'Title',
          Category: 'DESCRIPTION',
        },
      ];

      expect(() => service.validate(schema)).toThrowError(/a leaf and seems have children/gm);
    });

    it('should detect groups without children in schema', () => {
      const schema: Schema = [
        {
          Path: 'Signature.Masterdata',
          FieldName: 'Masterdata',
          ApiField: 'Masterdata',
          Type: 'OBJECT',
          Origin: 'INTERNAL',
          Indexed: true,
          Cardinality: 'ONE',
          SedaVersions: ['2.1'],
          Collection: Collection.ARCHIVE_UNIT,
          ApiPath: 'Signature.Masterdata',
          Category: 'DESCRIPTION',
        },
      ];

      expect(() => service.validate(schema)).toThrowError(/a group and seems not have children/gm);
    });
  });
});
