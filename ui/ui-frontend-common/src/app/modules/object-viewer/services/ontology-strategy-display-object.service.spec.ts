import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BASE_URL } from '../../injection-tokens';
import { LoggerModule } from '../../logger/logger.module';
import { OntologyService } from '../../ontology';
import { DisplayObject, DisplayRule } from '../models';
import { ComponentMapperService } from './component-mapper.service';
import { DataStructureService } from './data-structure.service';
import { DisplayObjectHelperService } from './display-object-helper.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { MockExtendedOntologyService } from './mock-extended-ontology.service';
import { OntologyStrategyDisplayObjectService } from './ontology-strategy-display-object.service';
import { TypeService } from './type.service';

describe('OntologyStrategyDisplayObjectService', () => {
  let httpClient: HttpClient;
  let httpTestingController: HttpTestingController;
  let service: OntologyStrategyDisplayObjectService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerModule.forRoot()],
      providers: [
        OntologyStrategyDisplayObjectService,
        TypeService,
        DataStructureService,
        DisplayObjectHelperService,
        DisplayRuleHelperService,
        ComponentMapperService,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: OntologyService, useClass: MockExtendedOntologyService },
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpTestingController = TestBed.inject(HttpTestingController);
    service = TestBed.inject(OntologyStrategyDisplayObjectService);
  });

  describe('Observable initialization', () => {
    it('should initialize displayObject with null', () => {
      service.displayObject$.subscribe((displayObject: DisplayObject) => {
        expect(displayObject).toBeNull();
      });
    });
  });

  describe('Default mode', () => {
    it('should update data and compute display object when data is an ontology', (done) => {
      const data = { Title: 'La ville de Paris' };

      service.setData(data);

      service.displayObject$.subscribe((displayObject: DisplayObject) => {
        console.log('displayObject', displayObject);

        expect(displayObject).toBeTruthy();
        expect(displayObject.children).toBeTruthy();
        expect(displayObject.children.length).toEqual(1);
        if (displayObject.children.length) {
          expect(displayObject.children[0].path).toEqual('Title');
          expect(displayObject.children[0].value).toEqual('La ville de Paris');
        }

        done();
      });
    });

    it('should update data and compute display object when data is not an ontology', (done) => {
      const data = { notOntologicKey: 'La ville de Paris' };

      service.setData(data);

      service.displayObject$.subscribe((displayObject: DisplayObject) => {
        console.log('displayObject', displayObject);

        expect(displayObject).toBeTruthy();
        expect(displayObject.children).toBeTruthy();
        expect(displayObject.children.length).toEqual(1);
        if (displayObject.children.length) {
          expect(displayObject.children[0].path).toEqual('notOntologicKey');
          expect(displayObject.children[0].value).toEqual('La ville de Paris');
        }

        done();
      });
    });

    it('should display ontology nodes and not others', (done) => {
      const inputs: { data: any; template: DisplayRule[]; expected: DisplayObject }[] = [
        {
          data: { id: '1', Title: 'core', tags: ['low', 'medium', 'high'] },
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
                favoriteKeys: ['Title'],
              },
            },
          ],
          expected: {
            type: 'group',
            component: 'group',
            path: '',
            key: '',
            value: {
              Title: 'core',
            },
            children: [
              {
                type: 'primitive',
                component: 'textfield',
                path: 'id',
                key: 'id',
                value: '1',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'id',
                  ui: {
                    path: 'id',
                    component: 'textfield',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'primitive',
                component: 'textfield',
                path: 'Title',
                key: 'Title',
                value: 'core',
                children: [],
                favoriteKeys: [],
                open: true,
                displayRule: {
                  path: 'Title',
                  ui: {
                    path: 'Title',
                    component: 'textfield',
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'list',
                component: 'group',
                path: 'tags',
                key: 'tags',
                value: '1',
                children: [
                  {
                    type: 'primitive',
                    component: 'textfield',
                    path: 'tags[0]',
                    key: 'tags[0]',
                    value: 'low',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[0]',
                      ui: {
                        path: 'tags[0]',
                        component: 'textfield',
                        layout: {
                          columns: 2,
                          size: 'medium',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                  {
                    type: 'primitive',
                    component: 'textfield',
                    path: 'tags[1]',
                    key: 'tags[1]',
                    value: 'medium',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[0]',
                      ui: {
                        path: 'tags[0]',
                        component: 'textfield',
                        layout: {
                          columns: 2,
                          size: 'medium',
                        },
                        open: true,
                        display: true,
                      },
                    },
                  },
                  {
                    type: 'primitive',
                    component: 'textfield',
                    path: 'tags[2]',
                    key: 'tags[2]',
                    value: 'high',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'tags[0]',
                      ui: {
                        path: 'tags[0]',
                        component: 'textfield',
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
            favoriteKeys: ['Title'],
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
                favoriteKeys: ['Title'],
                open: true,
                display: true,
              },
            },
          },
        },
      ];

      expect(service).toBeTruthy();

      service.setMode('default');

      const { data, template, expected }: { data: any; template: DisplayRule[]; expected: DisplayObject } = inputs[0];

      expect(data).toBeTruthy();
      expect(template).toBeTruthy();
      expect(expected).toBeTruthy();

      service.setTemplate(template);
      service.setData(data);
      service.displayObject$.subscribe(async (displayObject) => {
        await expect(displayObject).toBeTruthy();
        await expect(displayObject.children).toBeTruthy();
        await expect(displayObject.children.length).toEqual(3);
        await expect(displayObject.children[0]).toBeTruthy();
        await expect(displayObject.children[0].path).toEqual('id');
        await expect(displayObject.children[0].displayRule.ui.display).toEqual(false);
        await expect(displayObject.children[1]).toBeTruthy();
        await expect(displayObject.children[1].path).toEqual('Title');
        await expect(displayObject.children[1].displayRule.ui.display).toEqual(true);
        await expect(displayObject.children[2]).toBeTruthy();
        await expect(displayObject.children[2].path).toEqual('tags');
        await expect(displayObject.children[2].displayRule.ui.display).toEqual(false);
        await expect(displayObject.children[2].children).toBeTruthy();
        await expect(displayObject.children[2].children.length).toEqual(3);
        await expect(displayObject.children[2].children[0].path).toEqual('tags[0]');
        await expect(displayObject.children[2].children[0].value).toEqual('low');
        await expect(displayObject.children[2].children[0].displayRule.ui.display).toEqual(false);
        await expect(displayObject.children[2].children[1].path).toEqual('tags[1]');
        await expect(displayObject.children[2].children[1].value).toEqual('medium');
        await expect(displayObject.children[2].children[1].displayRule.ui.display).toEqual(false);
        await expect(displayObject.children[2].children[2].path).toEqual('tags[2]');
        await expect(displayObject.children[2].children[2].value).toEqual('high');
        await expect(displayObject.children[2].children[2].displayRule.ui.display).toEqual(false);

        done();
      });
    });

    it('should only map data known as ontology or custom template to display object', (done) => {
      const inputs: { data: any; template: DisplayRule[]; expected: DisplayObject }[] = [
        {
          data: { id: '1', Title: 'core', Tag: ['low', 'medium', 'high'] },
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
                favoriteKeys: ['Title'],
              },
            },
            {
              path: null,
              ui: {
                path: 'Generalities',
                component: 'group',
                favoriteKeys: [],
              },
            },
            {
              path: 'Title',
              ui: {
                path: 'Generalities.Title',
                component: 'textfield',
              },
            },
          ],
          expected: {
            type: 'group',
            component: 'group',
            path: '',
            key: '',
            value: {
              Generalities: {
                Title: 'core',
              },
              Tag: ['low', 'medium', 'high'],
            },
            children: [
              {
                type: 'group',
                component: 'group',
                path: 'Generalities',
                key: 'Generalities',
                value: {
                  Title: 'core',
                },
                children: [
                  {
                    type: 'primitive',
                    component: 'textfield',
                    path: 'Generalities.Title',
                    key: 'Title',
                    value: 'core',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'Title',
                      ui: {
                        path: 'Generalities.Title',
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
                  path: null,
                  ui: {
                    path: 'Generalities',
                    component: 'group',
                    favoriteKeys: [],
                    layout: {
                      columns: 2,
                      size: 'medium',
                    },
                    open: true,
                    display: true,
                  },
                },
              },
              {
                type: 'list',
                component: 'textfield',
                path: 'Tag',
                key: 'Tag',
                value: ['low', 'medium', 'high'],
                children: [
                  {
                    type: 'primitive',
                    component: 'textfield',
                    path: 'Tag[0]',
                    key: 'Tag[0]',
                    value: 'low',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'Tag[0]',
                      ui: {
                        path: 'Tag[0]',
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
                    component: 'textfield',
                    path: 'Tag[1]',
                    key: 'Tag[1]',
                    value: 'medium',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'Tag[1]',
                      ui: {
                        path: 'Tag[1]',
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
                    component: 'textfield',
                    path: 'Tag[2]',
                    key: 'Tag[2]',
                    value: 'high',
                    children: [],
                    favoriteKeys: [],
                    open: true,
                    displayRule: {
                      path: 'Tag[2]',
                      ui: {
                        path: 'Tag[2]',
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
                  path: 'Tag',
                  ui: {
                    path: 'Tag',
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
            favoriteKeys: ['Title'],
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
                favoriteKeys: ['Title'],
                open: true,
                display: true,
              },
            },
          },
        },
      ];

      expect(service).toBeTruthy();

      service.setMode('default');

      const { data, template, expected }: { data: any; template: DisplayRule[]; expected: DisplayObject } = inputs[0];

      expect(data).toBeTruthy();
      expect(template).toBeTruthy();
      expect(expected).toBeTruthy();

      service.setTemplate(template);
      service.setData(data);
      service.displayObject$.subscribe(async (displayObject) => {
        console.log({ displayObject });
        await expect(displayObject).toBeTruthy();
        await expect(displayObject.favoriteKeys).toBeTruthy();
        await expect(displayObject.favoriteKeys.length).toEqual(1);
        await expect(displayObject.children).toBeTruthy();
        await expect(displayObject.children.length).toEqual(3);
        // await expect(displayObject.children[0]).toBeTruthy();
        // await expect(displayObject.children[0].path).toEqual('id');
        // await expect(displayObject.children[0].displayRule.ui.display).toEqual(false);
        // await expect(displayObject.children[1]).toBeTruthy();
        // await expect(displayObject.children[1].path).toEqual('Title');
        // await expect(displayObject.children[1].displayRule.ui.display).toEqual(true);
        // await expect(displayObject.children[2]).toBeTruthy();
        // await expect(displayObject.children[2].path).toEqual('tags');
        // await expect(displayObject.children[2].displayRule.ui.display).toEqual(false);
        // await expect(displayObject.children[2].children).toBeTruthy();
        // await expect(displayObject.children[2].children.length).toEqual(3);
        // await expect(displayObject.children[2].children[0].path).toEqual('tags[0]');
        // await expect(displayObject.children[2].children[0].value).toEqual('low');
        // await expect(displayObject.children[2].children[0].displayRule.ui.display).toEqual(false);
        // await expect(displayObject.children[2].children[1].path).toEqual('tags[1]');
        // await expect(displayObject.children[2].children[1].value).toEqual('medium');
        // await expect(displayObject.children[2].children[1].displayRule.ui.display).toEqual(false);
        // await expect(displayObject.children[2].children[2].path).toEqual('tags[2]');
        // await expect(displayObject.children[2].children[2].value).toEqual('high');
        // await expect(displayObject.children[2].children[2].displayRule.ui.display).toEqual(false);

        done();
      });
    });
  });
});
