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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { BASE_URL } from '../../../injection-tokens';
import { LoggerModule } from '../../../logger/logger.module';
import { DisplayObject } from '../../../object-viewer/models/display-object.model';
import { DisplayRule } from '../../../object-viewer/models/display-rule.model';
import { DataStructureService } from '../../../object-viewer/services/data-structure.service';
import { DisplayObjectHelperService } from '../../../object-viewer/services/display-object-helper.service';
import { DisplayRuleHelperService } from '../../../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../../../object-viewer/services/schema-element-to-display-rule.service';
import { TypeService } from '../../../object-viewer/services/type.service';
import { SchemaService } from '../../../schema/schema.service';
import { MockSchemaService } from '../../../schema/mock-schema.service';
import { ArchiveUnitViewerService } from './archive-unit-viewer.service';
import { ObjectEditorModule } from '../../../object-editor/object-editor.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

describe('ArchiveUnitViewerService', () => {
  let service: ArchiveUnitViewerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, LoggerModule.forRoot(), ObjectEditorModule],
      providers: [
        ArchiveUnitViewerService,
        TypeService,
        DataStructureService,
        DisplayObjectHelperService,
        DisplayRuleHelperService,
        SchemaElementToDisplayRuleService,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: SchemaService, useClass: MockSchemaService },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
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
          expect.arrayContaining([
            expect.objectContaining({
              path: 'Title',
              value: 'La ville de Paris',
              displayRule: expect.objectContaining({
                Path: 'Title',
                ui: expect.objectContaining({
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
          expect.arrayContaining([
            expect.objectContaining({
              path: 'notOntologicKey',
              value: 'La ville de Paris',
              displayRule: expect.objectContaining({
                Path: 'notOntologicKey',
                ui: expect.objectContaining({
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
