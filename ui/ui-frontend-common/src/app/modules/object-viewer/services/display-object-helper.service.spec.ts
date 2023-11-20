import { TestBed } from '@angular/core/testing';
import { LoggerModule } from '../../logger';
import { DisplayObject, DisplayRule } from '../models';
import { DisplayObjectType } from '../types';
import { DataStructureService } from './data-structure.service';
import { DisplayObjectHelperService } from './display-object-helper.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { TypeService } from './type.service';

describe('DisplayObjectHelperService', () => {
  let service: DisplayObjectHelperService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DisplayObjectHelperService, DisplayRuleHelperService, TypeService, DataStructureService],
      imports: [LoggerModule.forRoot()],
    });
    service = TestBed.inject(DisplayObjectHelperService);
  });

  describe('toDisplayObject', () => {
    it('should map to display object', () => {
      const inputs: { data: any; template: DisplayRule[]; expected: DisplayObject }[] = [
        {
          data: 'bonjour',
          template: [],
          expected: {
            type: DisplayObjectType.PRIMITIVE,
            path: '',
            key: '',
            value: 'bonjour',
            component: 'textfield',
            children: [],
            favoriteKeys: [],
            open: true,
            displayRule: {
              path: '',
              ui: {
                path: '',
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
        },
        {
          data: ['A', 'B', 'C'],
          template: [],
          expected: {
            type: DisplayObjectType.LIST,
            path: '',
            key: '',
            value: ['A', 'B', 'C'],
            component: 'textfield',
            children: [
              {
                type: DisplayObjectType.PRIMITIVE,
                path: '[0]',
                key: '[0]',
                value: 'A',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: '[0]',
                  ui: {
                    path: '[0]',
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
                type: DisplayObjectType.PRIMITIVE,
                path: '[1]',
                key: '[1]',
                value: 'B',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: '[1]',
                  ui: {
                    path: '[1]',
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
                type: DisplayObjectType.PRIMITIVE,
                path: '[2]',
                key: '[2]',
                value: 'C',
                component: 'textfield',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: '[2]',
                  ui: {
                    path: '[2]',
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
        {
          data: { id: '1', name: 'core' },
          template: [],
          expected: {
            type: DisplayObjectType.GROUP,
            path: '',
            key: '',
            value: { id: '1', name: 'core' },
            component: 'group',
            children: [
              {
                type: DisplayObjectType.PRIMITIVE,
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
                type: DisplayObjectType.PRIMITIVE,
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
            type: DisplayObjectType.GROUP,
            path: '',
            key: '',
            value: { id: '1', name: 'core', tags: ['low', 'medium', 'high'] },
            component: 'group',
            children: [
              {
                type: DisplayObjectType.PRIMITIVE,
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
                type: DisplayObjectType.PRIMITIVE,
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
                type: DisplayObjectType.LIST,
                path: 'tags',
                key: 'tags',
                value: ['low', 'medium', 'high'],
                component: 'textfield',
                children: [
                  {
                    type: DisplayObjectType.PRIMITIVE,
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
                    type: DisplayObjectType.PRIMITIVE,
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
                    type: DisplayObjectType.PRIMITIVE,
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
            favoriteKeys: ['name'],
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
                favoriteKeys: ['name'],
                open: true,
                display: true,
              },
            },
          },
        },
      ];

      expect(service).toBeTruthy();
      inputs.forEach((input, i) => {
        const { data, template, expected }: { data: any; template: DisplayRule[]; expected: DisplayObject } = input;

        expect(data).toBeTruthy();
        expect(template).toBeTruthy();
        expect(expected).toBeTruthy();

        const displayObject = service.toDisplayObject(data, template);

        expect(displayObject).withContext(`failed on index ${i}`).toEqual(expected, `failed on index ${i}`);
      });
    });
  });
});
