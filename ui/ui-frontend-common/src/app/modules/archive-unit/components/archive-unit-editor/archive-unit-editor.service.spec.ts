import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { LoggerModule } from '../../../logger';
import { EditObjectService } from '../../../object-editor/services/edit-object.service';
import { SchemaService as SchemaUtils } from '../../../object-editor/services/schema.service';
import { TemplateService } from '../../../object-editor/services/template.service';
import { DisplayObjectHelperService } from '../../../object-viewer/services/display-object-helper.service';
import { DisplayRuleHelperService } from '../../../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../../../object-viewer/services/schema-element-to-display-rule.service';
import { SchemaService } from '../../../schema';
import { ArchiveUnitEditorService } from './archive-unit-editor.service';
import arrayWithExactContents = jasmine.arrayWithExactContents;

describe('ArchiveUnitEditorService', () => {
  let service: ArchiveUnitEditorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot()],
      providers: [
        ArchiveUnitEditorService,
        { provide: SchemaService, useValue: { getSchema: () => of() } },
        { provide: DisplayObjectHelperService, useValue: {} },
        { provide: DisplayRuleHelperService, useValue: {} },
        { provide: SchemaElementToDisplayRuleService, useValue: {} },
        { provide: TemplateService, useValue: {} },
        { provide: EditObjectService, useValue: {} },
        { provide: SchemaUtils, useValue: {} },
      ],
    });
    service = TestBed.inject(ArchiveUnitEditorService);
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should generate a JsonPatch', () => {
    const originalValue = {
      '#id': '42',
      Title: 'Title',
      Toto: {
        Titi: 'foo',
      },
      Foo: 'foo',
    };
    const value = {
      '#id': '42',
      Title: 'Title modified',
      Toto: {
        Titi: 'foo',
        Tata: {
          Tutu: 'foobar',
        },
      },
      Bar: {
        AAA: 'aaa',
        BBB: 'bbb',
      },
    };
    spyOn(service, 'getOriginalValue').and.returnValue(originalValue);
    spyOn(service, 'getValue').and.returnValue(value);

    const jsonPatch = service.toJsonPatch();
    expect(jsonPatch).toEqual(
      arrayWithExactContents([
        { op: 'replace', path: 'Title', value: value.Title },
        { op: 'replace', path: 'Toto', value: value.Toto },
        { op: 'add', path: 'Bar', value: value.Bar },
        { op: 'remove', path: 'Foo', value: originalValue.Foo },
      ]),
    );
  });
});
