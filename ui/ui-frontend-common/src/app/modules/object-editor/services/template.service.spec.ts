import { TestBed } from '@angular/core/testing';
import { DisplayRule } from '../../object-viewer/models';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';
import { TemplateService } from './template.service';

describe('TemplateService', () => {
  let service: TemplateService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [SchemaElementToDisplayRuleService] });
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
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'password', component: 'textfield' } }];
      const output = service.toProjected(input, template);
      const expected = { password: 'azerty1234' };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should projection ignore template less fields', () => {
      const input = { username: 'azerty1234', templateLessField: 'templateLess' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'password', component: 'textfield' } }];
      const output = service.toProjected(input, template);
      const expected = { password: 'azerty1234' };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });

    it('should convert to ui data with virtual projection', () => {
      const input = { username: 'azerty1234', password: 'john' };
      const template: DisplayRule[] = [
        { Path: null, ui: { Path: 'credentials', component: 'group' } },
        { Path: 'username', ui: { Path: 'credentials.password', component: 'textfield' } },
        { Path: 'password', ui: { Path: 'credentials.login', component: 'textfield' } },
      ];
      const output = service.toProjected(input, template);
      const expected = {
        credentials: {
          login: 'john',
          password: 'azerty1234',
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
      const input = { password: 'azerty1234' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'password', component: 'textfield' } }];
      const output = service.toOriginal(input, template);

      expect(output).toBeTruthy();
      expect(output).toEqual({ username: 'azerty1234' });
    });

    it('should projection ignore template less fields', () => {
      const input = { password: 'azerty1234', templateLessField: 'templateLess' };
      const template: DisplayRule[] = [{ Path: 'username', ui: { Path: 'password', component: 'textfield' } }];
      const output = service.toOriginal(input, template);

      expect(output).toBeTruthy();
      expect(output).toEqual({ username: 'azerty1234' });
    });

    it('should convert to original data with virtual projection', () => {
      const input = {
        credentials: {
          login: 'john',
          password: 'azerty1234',
        },
      };
      const template: DisplayRule[] = [
        { Path: null, ui: { Path: 'credentials', component: 'group' } },
        { Path: 'username', ui: { Path: 'credentials.password', component: 'textfield' } },
        { Path: 'password', ui: { Path: 'credentials.login', component: 'textfield' } },
      ];
      const expected = { username: 'azerty1234', password: 'john' };
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
      const expected = { Title: 'some title', Title_: undefined };

      expect(output).toBeTruthy();
      expect(output).toEqual(expected);
    });
  });

  it('should detect templates with infinite loops', () => {
    const data = { replicants: [] };
    const template: DisplayRule[] = [
      { Path: 'replicants', ui: { Path: 'replicants', component: 'group' } },
      { Path: 'replicants', ui: { Path: 'replicants.name', component: 'textfield' } },
    ];

    expect(() => service.toProjected(data, template)).toThrowError(
      "Rule 'replicants' contains circular references ['replicants','replicants.name']",
    );
  });
});
