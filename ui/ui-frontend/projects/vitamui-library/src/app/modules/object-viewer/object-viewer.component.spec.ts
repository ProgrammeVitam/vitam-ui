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
import { HttpBackend, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ChangeDetectionStrategy, CUSTOM_ELEMENTS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { customTemplate } from '../archive-unit/archive-unit-template';
import { BASE_URL } from '../injection-tokens';
import { LoggerModule } from '../logger/logger.module';
import { VitamuiMissingTranslationHandler } from '../missing-translation-handler';
import { PipesModule } from '../pipes/pipes.module';
import { GroupComponent } from './components/group/group.component';
import { ListComponent } from './components/list/list.component';
import { PrimitiveComponent } from './components/primitive/primitive.component';
import { DisplayObjectService } from './models';
import { ObjectViewerComponent } from './object-viewer.component';
import { DataStructureService } from './services/data-structure.service';
import { DateDisplayService } from './services/date-display.service';
import { DisplayObjectHelperService } from './services/display-object-helper.service';
import { DisplayRuleHelperService } from './services/display-rule-helper.service';
import { FavoriteEntryService } from './services/favorite-entry.service';
import { LayoutService } from './services/layout.service';
import { PathStrategyDisplayObjectService } from './services/path-strategy-display-object.service';
import { SchemaElementToDisplayRuleService } from './services/schema-element-to-display-rule.service';
import { TypeService } from './services/type.service';
import { Unit, UnitType } from '../models';
import { DescriptionLevel } from '../../../lib/models/description-level.enum';

class FakeTranslateLoader implements TranslateLoader {
  getTranslation(lang: string): Observable<any> {
    if (lang === 'fr') {
      return of({
        ARCHIVE_SEARCH: {
          RESULTS: 'résultats',
          ONE_SELECTED: 'séléctionné',
          MORE_THAN: '+ de',
        },
      });
    }

    return of({
      ARCHIVE_SEARCH: {
        RESULTS: 'results',
        ONE_SELECTED: 'selected',
        MORE_THAN: '+ than',
      },
    });
  }
}

describe('ObjectViewerComponent', () => {
  let component: ObjectViewerComponent;
  let fixture: ComponentFixture<ObjectViewerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ObjectViewerComponent, GroupComponent, ListComponent, PrimitiveComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        TranslateModule.forRoot({
          missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
          defaultLanguage: 'fr',
          loader: {
            provide: TranslateLoader,
            useClass: FakeTranslateLoader,
            deps: [HttpBackend],
          },
        }),
        PipesModule,
        LoggerModule.forRoot(),
      ],
      providers: [
        DataStructureService,
        TypeService,
        DisplayObjectHelperService,
        DisplayRuleHelperService,
        SchemaElementToDisplayRuleService,
        DateDisplayService,
        LayoutService,
        FavoriteEntryService,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: DisplayObjectService, useClass: PathStrategyDisplayObjectService },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    })
      .overrideComponent(ObjectViewerComponent, {
        set: { changeDetection: ChangeDetectionStrategy.Default },
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ObjectViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create with any', () => {
    component.data = data;
    component.template = customTemplate;

    component.ngOnChanges({
      data: new SimpleChange(null, data, true),
      template: new SimpleChange(null, customTemplate, true),
    });

    fixture.detectChanges();

    expect(component.data).toBeTruthy();
    expect(component.template).toBeTruthy();
  });
});

const data: Unit = {
  '#id': 'aeaqaaaaaeecehmeabneoamjofozk4iaaaea',
  Title: 'Gambetta par producteur1',
  DescriptionLevel: DescriptionLevel.RECORD_GRP,
  Description: 'Station Gambetta ligne 3 Paris',
  OriginatingAgencyArchiveUnitIdentifier: '',
  '#tenant': 1,
  '#unitups': [],
  '#min': 1,
  '#max': 1,
  '#allunitups': [],
  '#unitType': UnitType.INGEST,
  '#operations': ['aeeaaaaaagecehmeaa5rwamjofoy5haaaaaq'],
  '#opi': 'aeeaaaaaagecehmeaa5rwamjofoy5haaaaaq',
  '#originating_agency': 'producteur1',
  '#originating_agencies': ['producteur1'],
  '#management': {
    AppraisalRule: null,
    HoldRule: null,
    StorageRule: null,
    ReuseRule: null,
    ClassificationRule: null,
    DisseminationRule: null,
    AccessRule: null,
    UpdateOperation: null,
  },
  StartDate: new Date('2016-06-03T15:28:00'),
  EndDate: new Date('2016-06-03T15:28:00'),
  Xtag: [],
  Vtag: [],
  '#storage': {
    strategyId: 'default',
  },
  '#qualifiers': [],
  OriginatingSystemId: [],
  PhysicalAgency: [],
  PhysicalStatus: [],
  PhysicalType: [],
  Keyword: [],
  '#approximate_creation_date': '2023-07-20T03:35:07.967',
  '#approximate_update_date': '2023-07-20T03:35:07.967',
  originating_agencyName: 'Service producteur1',
};
