import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { EMPTY } from 'rxjs';
import { BASE_URL } from '../../../injection-tokens';
import { LoggerModule } from '../../../logger';
import { DisplayObject, DisplayRule } from '../../../object-viewer/models';
import { DataStructureService } from '../../../object-viewer/services/data-structure.service';
import { DisplayObjectHelperService } from '../../../object-viewer/services/display-object-helper.service';
import { DisplayRuleHelperService } from '../../../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../../../object-viewer/services/schema-element-to-display-rule.service';
import { TypeService } from '../../../object-viewer/services/type.service';
import { SchemaService } from '../../../schema';
import { MockSchemaService } from '../../../schema/mock-schema.service';
import { ArchiveUnitViewerService } from './archive-unit-viewer.service';

describe('ArchiveUnitViewerService', () => {
  let service: ArchiveUnitViewerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, ReactiveFormsModule, LoggerModule.forRoot()],
      providers: [
        ArchiveUnitViewerService,
        TypeService,
        DataStructureService,
        DisplayObjectHelperService,
        DisplayRuleHelperService,
        SchemaElementToDisplayRuleService,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: SchemaService, useClass: MockSchemaService },
        { provide: TranslateService, useValue: { instant: () => EMPTY } },
      ],
    });
    service = TestBed.inject(ArchiveUnitViewerService);
  });

  describe('Observable initialization', () => {
    it('should initialize displayObject with null', () => {
      service.displayObject$.subscribe((displayObject: DisplayObject) => {
        expect(displayObject).toBeNull();
      });
    });
  });

  describe('Default mode', () => {
    it('should update data and compute display object when data is a schema element', waitForAsync(() => {
      const data = { Title: 'La ville de Paris' };

      service.setData(data);

      service.displayObject$.subscribe((displayObject: DisplayObject) => {
        expect(displayObject).toBeTruthy();
        expect(displayObject.children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'Title',
              value: 'La ville de Paris',
              displayRule: jasmine.objectContaining({
                Path: 'Title',
                ui: jasmine.objectContaining({
                  component: 'textfield',
                }),
              }),
            }),
          ]),
        );
      });
    }));

    it('should update data and compute display object when data is in custom template', waitForAsync(() => {
      const data = { notOntologicKey: 'La ville de Paris' };
      const customTemplate: DisplayRule[] = [
        {
          Path: 'notOntologicKey',
          ui: {
            Path: 'notOntologicKey',
            component: 'textfield',
          },
        },
      ];

      service.setData(data);
      service.setTemplate(customTemplate);

      service.displayObject$.subscribe((displayObject: DisplayObject) => {
        expect(displayObject).toBeTruthy();
        expect(displayObject.children).toEqual(
          jasmine.arrayContaining([
            jasmine.objectContaining({
              path: 'notOntologicKey',
              value: 'La ville de Paris',
              displayRule: jasmine.objectContaining({
                Path: 'notOntologicKey',
                ui: jasmine.objectContaining({
                  component: 'textfield',
                }),
              }),
            }),
          ]),
        );
      });
    }));
  });
});
