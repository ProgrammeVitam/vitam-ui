import { TestBed } from '@angular/core/testing';
import { LoggerModule } from '../../logger/logger.module';
import { DisplayObject, DisplayRule } from '../models';
import { DataStructureService } from './data-structure.service';
import { DisplayObjectHelperService } from './display-object-helper.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { PathStrategyDisplayObjectService } from './path-strategy-display-object.service';
import { TypeService } from './type.service';

describe('PathStrategyDisplayObjectService', () => {
  let service: PathStrategyDisplayObjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot()],
      providers: [
        PathStrategyDisplayObjectService,
        TypeService,
        DataStructureService,
        DisplayObjectHelperService,
        DisplayRuleHelperService,
      ],
    });
    service = TestBed.inject(PathStrategyDisplayObjectService);
  });

  describe('Data driven mode', () => {
    it('should map to display object', () => {
      const inputs: { data: any; template: DisplayRule[]; expected: DisplayObject }[] = [
        {
          data: { id: '1', name: 'core', tags: ['low', 'medium', 'high'] },
          template: [
            {
              path: '',
              ui: {
                path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                favoriteKeys: ['name'],
              },
            },
            {
              path: '',
              ui: {
                path: 'Generalities',
                component: 'group',
                favoriteKeys: ['name'],
              },
            },
            {
              path: 'name',
              ui: {
                path: 'Generalities.name',
                component: 'group',
              },
            },
          ],
          expected: {
            type: 'group',
            path: '',
            key: '',
            value: { id: '1', name: 'core', tags: ['low', 'medium', 'high'] },
            component: 'group',
            children: [
              {
                type: 'primitive',
                path: 'id',
                key: 'id',
                value: '1',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'id',
                  ui: {
                    path: 'id',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'primitive',
                path: 'name',
                key: 'name',
                value: 'core',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'name',
                  ui: {
                    path: 'name',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'list',
                path: 'tags',
                key: 'tags',
                value: ['low', 'medium', 'high'],
                component: 'textfield',
                children: [
                  {
                    type: 'primitive',
                    path: 'tags[0]',
                    key: 'tags[0]',
                    value: 'low',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[0]',
                      ui: {
                        path: 'tags[0]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                  {
                    type: 'primitive',
                    path: 'tags[1]',
                    key: 'tags[1]',
                    value: 'medium',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[1]',
                      ui: {
                        path: 'tags[1]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                  {
                    type: 'primitive',
                    path: 'tags[2]',
                    key: 'tags[2]',
                    value: 'high',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[2]',
                      ui: {
                        path: 'tags[2]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                ],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'tags',
                  ui: {
                    path: 'tags',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
            ],
            favoriteKeys: [],
            open: true,
            displayRule: {
              path: '',
              ui: {
                path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                open: true,
                display: true,
              },
            },
          },
        },
      ];

      service.setMode('data-driven');

      expect(service).toBeTruthy();

      const { data, template, expected }: { data: any; template: DisplayRule[]; expected: DisplayObject } = inputs[0];

      expect(data).toBeTruthy();
      expect(template).toBeTruthy();
      expect(expected).toBeTruthy();

      service.setTemplate(template);
      service.setData(data);
      service.displayObject$.subscribe((displayObject) => {
        expect(displayObject).toEqual(expected);
      });
    });
  });

  describe('Template driven mode', () => {
    it('should map to display object', () => {
      const inputs: { data: any; template: DisplayRule[]; expected: DisplayObject }[] = [
        {
          data: { id: '1', name: 'core', tags: ['low', 'medium', 'high'] },
          template: [
            {
              path: '',
              ui: {
                path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
              },
            },
            {
              path: null,
              ui: {
                path: 'Generalities',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                favoriteKeys: ['name'],
              },
            },
            {
              path: 'name',
              ui: {
                path: 'Generalities.name',
                component: 'textfield',
                layout: {
                  columns: 1,
                  size: 'small',
                },
              },
            },
          ],
          expected: {
            type: 'group',
            path: '',
            key: '',
            value: { Generalities: { name: 'core' } },
            component: 'group',
            children: [
              {
                type: 'group',
                path: 'Generalities',
                key: 'Generalities',
                value: { name: 'core' },
                component: 'group',
                children: [
                  {
                    type: 'primitive',
                    path: 'Generalities.name',
                    key: 'name',
                    value: 'core',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'name',
                      ui: {
                        path: 'Generalities.name',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                ],
                favoriteKeys: ['name'],
                open: true,
                displayRule: {
                  path: null,
                  ui: {
                    path: 'Generalities',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    favoriteKeys: ['name'],
                    open: true,
                    display: true,
                  },
                },
              },
            ],
            favoriteKeys: [],
            open: true,
            displayRule: {
              path: '',
              ui: {
                path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                open: true,
                display: true,
              },
            },
          },
        },
      ];

      expect(service).toBeTruthy();

      service.setMode('template-driven');

      const { data, template, expected }: { data: any; template: DisplayRule[]; expected: DisplayObject } = inputs[0];

      expect(data).toBeTruthy();
      expect(template).toBeTruthy();
      expect(expected).toBeTruthy();

      service.setTemplate(template);
      service.setData(data);
      service.displayObject$.subscribe((displayObject) => {
        expect(displayObject).toEqual(expected);
      });
    });
  });

  describe('Mixed driven mode', () => {
    it('should map to display object', () => {
      const inputs: { data: any; template: DisplayRule[]; expected: DisplayObject }[] = [
        {
          data: { id: '1', name: 'core', tags: ['low', 'medium', 'high'] },
          template: [
            {
              path: '',
              ui: {
                path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
              },
            },
            {
              path: null,
              ui: {
                path: 'Generalities',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                favoriteKeys: ['name'],
              },
            },
            {
              path: 'name',
              ui: {
                path: 'Generalities.name',
                component: 'textfield',
                layout: {
                  columns: 1,
                  size: 'small',
                },
              },
            },
          ],
          expected: {
            type: 'group',
            path: '',
            key: '',
            value: { id: '1', name: 'core', tags: ['low', 'medium', 'high'], Generalities: { name: 'core' } },
            component: 'group',
            children: [
              {
                type: 'group',
                path: 'Generalities',
                key: 'Generalities',
                value: { name: 'core' },
                component: 'group',
                children: [
                  {
                    type: 'primitive',
                    path: 'Generalities.name',
                    key: 'name',
                    value: 'core',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'name',
                      ui: {
                        path: 'Generalities.name',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                ],
                favoriteKeys: ['name'],
                open: true,
                displayRule: {
                  path: null,
                  ui: {
                    path: 'Generalities',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                    favoriteKeys: ['name'],
                  },
                },
              },
              {
                type: 'primitive',
                path: 'id',
                key: 'id',
                value: '1',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'id',
                  ui: {
                    path: 'id',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'primitive',
                path: 'name',
                key: 'name',
                value: 'core',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'name',
                  ui: {
                    path: 'name',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'list',
                path: 'tags',
                key: 'tags',
                value: ['low', 'medium', 'high'],
                component: 'textfield',
                children: [
                  {
                    type: 'primitive',
                    path: 'tags[0]',
                    key: 'tags[0]',
                    value: 'low',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[0]',
                      ui: {
                        path: 'tags[0]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                  {
                    type: 'primitive',
                    path: 'tags[1]',
                    key: 'tags[1]',
                    value: 'medium',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[1]',
                      ui: {
                        path: 'tags[1]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                  {
                    type: 'primitive',
                    path: 'tags[2]',
                    key: 'tags[2]',
                    value: 'high',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[2]',
                      ui: {
                        path: 'tags[2]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                ],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'tags',
                  ui: {
                    path: 'tags',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
            ],
            favoriteKeys: [],
            open: true,
            displayRule: {
              path: '',
              ui: {
                path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                open: true,
                display: true,
              },
            },
          },
        },
      ];

      expect(service).toBeTruthy();

      service.setMode('mixed-driven');

      const { data, template, expected }: { data: any; template: DisplayRule[]; expected: DisplayObject } = inputs[0];

      expect(data).toBeTruthy();
      expect(template).toBeTruthy();
      expect(expected).toBeTruthy();

      service.setTemplate(template);
      service.setData(data);
      service.displayObject$.subscribe((displayObject) => {
        console.log({ displayObject, expected });
        expect(displayObject).toEqual(expected);
      });
    });
  });
});
