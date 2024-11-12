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
              Path: '',
              ui: {
                Path: '',
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
                  Path: '[0]',
                  ui: {
                    Path: '[0]',
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
                  Path: '[1]',
                  ui: {
                    Path: '[1]',
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
                  Path: '[2]',
                  ui: {
                    Path: '[2]',
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
              },
            },
          },
        },
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
                  },
                },
              },
            ],
            favoriteKeys: ['name'],
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
