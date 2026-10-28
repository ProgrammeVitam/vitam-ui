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
import { TestBed } from '@angular/core/testing';
import { DatePatternConstants } from '../../dates.constants';
import { Collection } from '../../models/schema/collection.enum';
import { ProfiledSchemaElement, SchemaElement } from '../../models/schema/schema-element.model';
import { SchemaElementToDisplayRuleService } from './schema-element-to-display-rule.service';

const element = (overrides: Partial<ProfiledSchemaElement> = {}): ProfiledSchemaElement => ({
  FieldName: 'Title',
  ApiField: undefined,
  Path: 'Title',
  ApiPath: 'Title',
  Type: 'TEXT',
  DataType: 'STRING',
  Origin: 'INTERNAL',
  ShortName: 'Titre',
  StringSize: 'SHORT',
  Cardinality: 'ONE',
  SedaVersions: [],
  Collection: Collection.ARCHIVE_UNIT,
  Category: 'DESCRIPTION',
  ...overrides,
});

describe('SchemaElementToDisplayRuleService', () => {
  let service: SchemaElementToDisplayRuleService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [SchemaElementToDisplayRuleService] });
    service = TestBed.inject(SchemaElementToDisplayRuleService);
  });

  describe('mapSchemaElementToComponent', () => {
    it('should return the default component when no schema element is given', () => {
      expect(service.mapSchemaElementToComponent(null)).toEqual('textfield-short-mono');
      expect(service.mapSchemaElementToComponent(undefined)).toEqual('textfield-short-mono');
    });

    it.each([
      ['Writer', 'balise-n1'],
      ['Writer.Identifier', 'balise-n2'],
      ['Writer.Identifier.Value', 'balise-n3'],
      ['Writer.Identifier.Value.Detail', 'balise-n4'],
      ['Writer.Identifier.Value.Detail.Deeper', 'textfield-short-mono'],
    ])('should map an OBJECT at path %s to %s according to its depth', (path, expected) => {
      expect(service.mapSchemaElementToComponent(element({ DataType: 'OBJECT', Path: path }))).toEqual(expected);
    });

    it.each([
      ['ONE', 'SHORT', 'textfield-short-mono'],
      ['ONE_REQUIRED', 'SHORT', 'textfield-short-mono'],
      ['ONE', 'MEDIUM', 'textfield-medium-mono'],
      ['ONE', 'LARGE', 'textfield-large-mono'],
      ['MANY', 'SHORT', 'textfield-short-multi'],
      ['MANY_REQUIRED', 'SHORT', 'textfield-short-multi'],
      ['MANY', 'MEDIUM', 'textfield-medium-multi'],
      ['MANY', 'LARGE', 'textfield-large-multi'],
    ] as const)('should map a regular STRING with cardinality %s and size %s to %s', (cardinality, size, expected) => {
      expect(service.mapSchemaElementToComponent(element({ Cardinality: cardinality, StringSize: size }))).toEqual(expected);
    });

    it('should fall back to the default component for a regular STRING without size', () => {
      expect(service.mapSchemaElementToComponent(element({ Cardinality: 'MANY', StringSize: undefined }))).toEqual('textfield-short-mono');
    });

    it.each([
      ['ONE', 'SHORT', 'attribut-short-mono'],
      ['MANY', 'SHORT', 'attribut-short-multi'],
      ['ONE', 'MEDIUM', 'attribut-mono'],
      ['ONE', 'LARGE', 'attribut-mono'],
      ['MANY', 'MEDIUM', 'attribut-multi'],
      ['MANY', undefined, 'attribut-multi'],
    ] as const)('should map a special "_." STRING with cardinality %s and size %s to %s', (cardinality, size, expected) => {
      expect(service.mapSchemaElementToComponent(element({ Path: 'Title_.fr', Cardinality: cardinality, StringSize: size }))).toEqual(
        expected,
      );
    });

    it.each([
      DatePatternConstants.YEAR,
      DatePatternConstants.YEAR_MONTH,
      DatePatternConstants.YEAR_MONTH_DAY,
      DatePatternConstants.FULL_DATE,
    ])('should map a STRING controlled by the date regex %s to a date picker', (pattern) => {
      expect(service.mapSchemaElementToComponent(element({ Control: { Type: 'REGEX', Value: pattern } }))).toEqual('datepicker-date');
    });

    it('should map a STRING controlled by any other regex to the default component, whatever its size', () => {
      expect(
        service.mapSchemaElementToComponent(
          element({ StringSize: 'LARGE', Cardinality: 'MANY', Control: { Type: 'REGEX', Value: '^[A-Z]+$' } }),
        ),
      ).toEqual('textfield-short-mono');
    });

    it('should map DATETIME elements to a datetime picker', () => {
      expect(service.mapSchemaElementToComponent(element({ DataType: 'DATETIME' }))).toEqual('datepicker-datetime');
    });

    it.each(['LONG', 'DOUBLE', 'BOOLEAN', undefined] as const)('should map %s elements to the default component', (dataType) => {
      expect(service.mapSchemaElementToComponent(element({ DataType: dataType, StringSize: 'LARGE' }))).toEqual('textfield-short-mono');
    });
  });

  describe('mapSchemaElementToDisplayRule', () => {
    it('should build a display rule from the schema element', () => {
      const rule = service.mapSchemaElementToDisplayRule(
        element({ Path: 'Description', ApiPath: 'Description', ShortName: 'Description', StringSize: 'LARGE', Cardinality: 'MANY' }),
      );

      expect(rule).toEqual({
        Path: 'Description',
        ui: {
          Path: 'Description',
          component: 'textarea',
          label: 'Description',
          display: true,
          layout: { size: 'large', columns: 2 },
        },
      });
    });

    it.each([
      ['SHORT', 'small', 1],
      ['MEDIUM', 'medium', 2],
      ['LARGE', 'large', 2],
      [undefined, 'medium', 2],
    ] as const)('should compute the layout for string size %s as %s on %s column(s)', (size, layoutSize, columns) => {
      const rule = service.mapSchemaElementToDisplayRule(element({ StringSize: size }));

      expect(rule.ui.layout).toEqual({ size: layoutSize, columns });
    });

    it('should always use a small layout for DATETIME elements', () => {
      const rule = service.mapSchemaElementToDisplayRule(element({ DataType: 'DATETIME', StringSize: 'LARGE' }));

      expect(rule.ui.component).toEqual('datetime');
      expect(rule.ui.layout).toEqual({ size: 'small', columns: 1 });
    });

    it('should use the schema Path for the rule and the ApiPath for the ui', () => {
      const rule = service.mapSchemaElementToDisplayRule(element({ Path: 'Title_.fr', ApiPath: 'Title_.fr.value' }));

      expect(rule.Path).toEqual('Title_.fr');
      expect(rule.ui.Path).toEqual('Title_.fr.value');
      expect(rule.ui.component).toEqual('select+textfield');
    });

    it.each([
      ['DESCRIPTION', 'INTERNAL', true],
      ['MANAGEMENT', 'EXTERNAL', true],
      ['MANAGEMENT', 'INTERNAL', false],
      ['OTHER', 'VIRTUAL', false],
    ] as const)('should display an element of category %s and origin %s: %s', (category, origin, display) => {
      expect(service.mapSchemaElementToDisplayRule(element({ Category: category, Origin: origin })).ui.display).toBe(display);
    });

    it('should not share the base rule between calls', () => {
      const first = service.mapSchemaElementToDisplayRule(element({ Path: 'A', ApiPath: 'A' }));
      const second = service.mapSchemaElementToDisplayRule(element({ Path: 'B', ApiPath: 'B' }));

      expect(first.Path).toEqual('A');
      expect(first.ui.Path).toEqual('A');
      expect(second.Path).toEqual('B');
    });
  });

  describe('mapSchemaToDisplayRules', () => {
    it('should map every element of the schema in order', () => {
      const rules = service.mapSchemaToDisplayRules([
        element({ Path: 'Writer', DataType: 'OBJECT', StringSize: undefined }),
        element({ Path: 'Writer.FullName', ApiPath: 'Writer.FullName' }),
      ]);

      expect(rules.map((rule) => [rule.Path, rule.ui.component])).toEqual([
        ['Writer', 'group'],
        ['Writer.FullName', 'textfield'],
      ]);
    });

    it('should return an empty array for an empty schema', () => {
      expect(service.mapSchemaToDisplayRules([])).toEqual([]);
    });
  });

  describe('getSchemaElementFrontendModelPath', () => {
    it('should return the path unchanged when there is no ApiField', () => {
      expect(service.getSchemaElementFrontendModelPath(element({ Path: 'Writer.FullName', ApiField: undefined }))).toEqual(
        'Writer.FullName',
      );
    });

    it('should replace the last path fragment by the ApiField', () => {
      expect(service.getSchemaElementFrontendModelPath(element({ Path: 'Management.AppraisalRule.Rules', ApiField: '#rules' }))).toEqual(
        'Management.AppraisalRule.#rules',
      );
    });

    it('should replace the whole path for a root element with an ApiField', () => {
      expect(service.getSchemaElementFrontendModelPath(element({ Path: 'Id', ApiField: '#id' } as Partial<SchemaElement>))).toEqual('#id');
    });
  });
});
