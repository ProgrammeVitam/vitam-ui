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
import { of } from 'rxjs';
import { LoggerModule } from '../../../logger/logger.module';
import { EditObjectService } from '../../../object-editor/services/edit-object.service';
import { SchemaService as SchemaUtils } from '../../../object-editor/services/schema.service';
import { TemplateService } from '../../../object-editor/services/template.service';
import { DisplayObjectHelperService } from '../../../object-viewer/services/display-object-helper.service';
import { DisplayRuleHelperService } from '../../../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../../../object-viewer/services/schema-element-to-display-rule.service';
import { SchemaService } from '../../../schema/schema.service';
import { ArchiveUnitEditorService } from './archive-unit-editor.service';

const arrayWithExactContents = <T>(arr: T[]) => expect.arrayContaining(arr as any);

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
    vi.spyOn(service, 'getOriginalValue').mockReturnValue(originalValue);
    vi.spyOn(service, 'getValue').mockReturnValue(value);

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
