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
import { LoggerModule } from '../../logger/logger.module';
import { DisplayObject, DisplayRule } from '../models';
import { DisplayObjectType } from '../types';
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
              Path: '',
              ui: {
                Path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                favoriteKeys: ['name'],
              },
            },
            {
              Path: '',
              ui: {
                Path: 'Generalities',
                component: 'group',
                favoriteKeys: ['name'],
              },
            },
            {
              Path: 'name',
              ui: {
                Path: 'Generalities.name',
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
                  Path: 'id',
                  ui: {
                    Path: 'id',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                    label: 'id',
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
                  Path: 'name',
                  ui: {
                    Path: 'name',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                    label: 'name',
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
                      Path: 'tags[0]',
                      ui: {
                        Path: 'tags[0]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'tags[0]',
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
                      Path: 'tags[1]',
                      ui: {
                        Path: 'tags[1]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'tags[1]',
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
                      Path: 'tags[2]',
                      ui: {
                        Path: 'tags[2]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'tags[2]',
                      },
                    },
                  },
                ],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  Path: 'tags',
                  ui: {
                    Path: 'tags',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                    label: 'tags',
                  },
                },
              },
            ],
            favoriteKeys: [],
            open: true,
            displayRule: {
              Path: '',
              ui: {
                Path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                open: true,
                display: true,
                label: '',
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
              Path: '',
              ui: {
                Path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
              },
            },
            {
              Path: null,
              ui: {
                Path: 'Generalities',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                favoriteKeys: ['name'],
              },
            },
            {
              Path: 'name',
              ui: {
                Path: 'Generalities.name',
                component: 'textfield',
                layout: {
                  columns: 1,
                  size: 'small',
                },
              },
            },
          ],
          expected: {
            type: DisplayObjectType.GROUP,
            path: '',
            key: '',
            value: { Generalities: { name: 'core' } },
            component: 'group',
            children: [
              {
                type: DisplayObjectType.GROUP,
                path: 'Generalities',
                key: 'Generalities',
                value: { name: 'core' },
                component: 'group',
                children: [
                  {
                    type: DisplayObjectType.PRIMITIVE,
                    path: 'Generalities.name',
                    key: 'name',
                    value: 'core',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      Path: 'name',
                      ui: {
                        Path: 'Generalities.name',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'name',
                      },
                    },
                  },
                ],
                favoriteKeys: ['name'],
                open: true,
                displayRule: {
                  Path: null,
                  ui: {
                    Path: 'Generalities',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    favoriteKeys: ['name'],
                    open: true,
                    display: true,
                    label: 'Generalities',
                  },
                },
              },
            ],
            favoriteKeys: [],
            open: true,
            displayRule: {
              Path: '',
              ui: {
                Path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                open: true,
                display: true,
                label: '',
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
              Path: '',
              ui: {
                Path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
              },
            },
            {
              Path: null,
              ui: {
                Path: 'Generalities',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                favoriteKeys: ['name'],
              },
            },
            {
              Path: 'name',
              ui: {
                Path: 'Generalities.name',
                component: 'textfield',
                layout: {
                  columns: 1,
                  size: 'small',
                },
              },
            },
          ],
          expected: {
            type: DisplayObjectType.GROUP,
            path: '',
            key: '',
            value: { id: '1', name: 'core', tags: ['low', 'medium', 'high'], Generalities: { name: 'core' } },
            component: 'group',
            children: [
              {
                type: DisplayObjectType.GROUP,
                path: 'Generalities',
                key: 'Generalities',
                value: { name: 'core' },
                component: 'group',
                children: [
                  {
                    type: DisplayObjectType.PRIMITIVE,
                    path: 'Generalities.name',
                    key: 'name',
                    value: 'core',
                    component: 'textfield',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      Path: 'name',
                      ui: {
                        Path: 'Generalities.name',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'name',
                      },
                    },
                  },
                ],
                favoriteKeys: ['name'],
                open: true,
                displayRule: {
                  Path: null,
                  ui: {
                    Path: 'Generalities',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                    favoriteKeys: ['name'],
                    label: 'Generalities',
                  },
                },
              },
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
                  Path: 'id',
                  ui: {
                    Path: 'id',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                    label: 'id',
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
                  Path: 'name',
                  ui: {
                    Path: 'name',
                    component: 'textfield',
                    layout: {
                      columns: 1,
                      size: 'small',
                    },
                    open: true,
                    display: true,
                    label: 'name',
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
                      Path: 'tags[0]',
                      ui: {
                        Path: 'tags[0]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'tags[0]',
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
                      Path: 'tags[1]',
                      ui: {
                        Path: 'tags[1]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'tags[1]',
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
                      Path: 'tags[2]',
                      ui: {
                        Path: 'tags[2]',
                        component: 'textfield',
                        layout: {
                          columns: 1,
                          size: 'small',
                        },
                        open: true,
                        display: true,
                        label: 'tags[2]',
                      },
                    },
                  },
                ],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  Path: 'tags',
                  ui: {
                    Path: 'tags',
                    component: 'group',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                    label: 'tags',
                  },
                },
              },
            ],
            favoriteKeys: [],
            open: true,
            displayRule: {
              Path: '',
              ui: {
                Path: '',
                component: 'group',
                layout: {
                  columns: 2,
                  size: 'medium',
                },
                open: true,
                display: true,
                label: '',
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
        expect(displayObject).toEqual(expected);
      });
    });
  });
});
