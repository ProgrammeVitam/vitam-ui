import { HttpBackend } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
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
import { DescriptionLevel } from '../models/units/description-level.enum';

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
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
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
      ],
      imports: [
        HttpClientTestingModule,
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
        ObjectViewerComponent,
        GroupComponent,
        ListComponent,
        PrimitiveComponent,
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
