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
import { DisplayRule } from '../../object-viewer/models/display-rule.model';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';
import { TemplateService } from './template.service';

describe('TemplateService', () => {
  let service: TemplateService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [SchemaElementToDisplayRuleService, TemplateService] });
    service = TestBed.inject(TemplateService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  describe('ToUi', () => {
    it('should get back the data when null template', () => {
      const input = { name: 'john' };
      const output = service.toProjected(input, null);
      const expected = { name: 'john' };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should get empty data when empty template', () => {
      const input = { name: 'john' };
      const output = service.toProjected(input, []);
      const expected = {};

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should convert to ui data with simple projection', () => {
      const input = { username: 'azerty1234' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'login', component: 'textfield' } }];
      const output = service.toProjected(input, template);
      const expected = { login: 'azerty1234' };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should projection ignore template less fields', () => {
      const input = { username: 'azerty1234', templateLessField: 'templateLess' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'login', component: 'textfield' } }];
      const output = service.toProjected(input, template);
      const expected = { login: 'azerty1234' };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should convert to ui data with virtual projection', () => {
      const input = { username: 'azerty1234', firstname: 'john' };
      const template: DisplayRule[] = [
        { Path: null, ui: { Path: 'credentials', component: 'group' } },
        { Path: 'username', ui: { Path: 'credentials.firstname', component: 'textfield' } },
        { Path: 'firstname', ui: { Path: 'credentials.login', component: 'textfield' } },
      ];
      const output = service.toProjected(input, template);
      const expected = {
        credentials: {
          login: 'john',
          firstname: 'azerty1234',
        },
      };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should convert a nested node to ui data with virtual projection', () => {
      const input = {
        fruits: [
          { origin: 'france', name: 'abricot' },
          { origin: 'angleterre', name: 'noisette' },
          { origin: 'maroc', name: 'melon' },
          { origin: 'espagne', name: 'tomate' },
        ],
        societe: 'lidl',
      };
      const template: DisplayRule[] = [
        { Path: 'fruits', ui: { Path: 'panier', component: 'group' } },
        { Path: 'societe', ui: { Path: 'societe', component: 'group' } },
      ];
      const output = service.toProjected(input, template);
      const expected = {
        panier: [
          { origin: 'france', name: 'abricot' },
          { origin: 'angleterre', name: 'noisette' },
          { origin: 'maroc', name: 'melon' },
          { origin: 'espagne', name: 'tomate' },
        ],
        societe: 'lidl',
      };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });
  });

  describe('ToOriginal', () => {
    it('should get back the data when null template', () => {
      const output = service.toOriginal({ name: 'john' }, null);

      expect(output).toBeTruthy();
      expect(output).toEqual({ name: 'john' });
    });

    it('should get empty data when empty template', () => {
      const output = service.toOriginal({ name: 'john' }, []);

      expect(output).toBeTruthy();
      expect(output).toEqual({});
    });

    it('should convert to original data with simple projection', () => {
      const input = { foo: 'azerty1234' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'foo', component: 'textfield' } }];
      const output = service.toOriginal(input, template);

      expect(output).toBeTruthy();
      expect(output).toEqual({ username: 'azerty1234' });
    });

    it('should projection ignore template less fields', () => {
      const input = { foo: 'azerty1234', templateLessField: 'templateLess' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'foo', component: 'textfield' } }];
      const output = service.toOriginal(input, template);

      expect(output).toBeTruthy();
      expect(output).toEqual({ username: 'azerty1234' });
    });

    it('should convert to original data with virtual projection', () => {
      const input = {
        credentials: {
          login: 'john',
          foo: 'azerty1234',
        },
      };
      const template: DisplayRule[] = [
        { Path: null, ui: { Path: 'credentials', component: 'group' } },
        { Path: 'username', ui: { Path: 'credentials.foo', component: 'textfield' } },
        { Path: 'foo', ui: { Path: 'credentials.login', component: 'textfield' } },
      ];
      const expected = { username: 'azerty1234', foo: 'john' };
      const output = service.toOriginal(input, template);

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should convert a nested node to ui data with virtual projection', () => {
      const input = {
        panier: [
          { origin: 'france', name: 'abricot' },
          { origin: 'angleterre', name: 'noisette' },
          { origin: 'maroc', name: 'melon' },
          { origin: 'espagne', name: 'tomate' },
        ],
        societe: 'lidl',
      };
      const template: DisplayRule[] = [
        { Path: 'fruits', ui: { Path: 'panier', component: 'group' } },
        { Path: 'societe', ui: { Path: 'societe', component: 'group' } },
      ];
      const output = service.toOriginal(input, template);
      const expected = {
        fruits: [
          { origin: 'france', name: 'abricot' },
          { origin: 'angleterre', name: 'noisette' },
          { origin: 'maroc', name: 'melon' },
          { origin: 'espagne', name: 'tomate' },
        ],
        societe: 'lidl',
      };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should convert to undefined fields present in template but not in data', () => {
      const input = {
        Generalities: {
          Title: 'some title',
        },
      };
      const template: DisplayRule[] = [
        { Path: null, ui: { Path: 'Generalities', component: 'group' } },
        { Path: 'Title', ui: { Path: 'Generalities.Title', component: 'textfield' } },
        { Path: 'Title_', ui: { Path: 'Generalities.Title_', component: 'group' } },
      ];
      const output = service.toOriginal(input, template);
      const expected = { Title: 'some title', Title_: undefined as any };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });
  });

  it('should detect templates with infinite loops', () => {
    const data = { replicants: [] as any[] };
    const template: DisplayRule[] = [
      { Path: 'replicants', ui: { Path: 'replicants', component: 'group' } },
      { Path: 'replicants', ui: { Path: 'replicants.name', component: 'textfield' } },
    ];

    expect(() => service.toProjected(data, template)).toThrowError(
      "Rule 'replicants' contains circular references ['replicants','replicants.name']",
    );
  });
});
