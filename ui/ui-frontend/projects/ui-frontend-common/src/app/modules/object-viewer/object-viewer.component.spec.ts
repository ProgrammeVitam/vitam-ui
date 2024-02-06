import { HttpBackend } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ChangeDetectionStrategy, CUSTOM_ELEMENTS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';
import { BASE_URL } from '../injection-tokens';
import { LoggerModule } from '../logger/logger.module';
import { VitamuiMissingTranslationHandler } from '../missing-translation-handler';
import { PipesModule } from '../pipes/pipes.module';
import { GroupComponent } from './components/group/group.component';
import { ListComponent } from './components/list/list.component';
import { PrimitiveComponent } from './components/primitive/primitive.component';
import { DisplayObjectService, DisplayRule } from './models';
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
    component.template = template;

    component.ngOnChanges({
      data: new SimpleChange(null, data, true),
      template: new SimpleChange(null, template, true),
    });

    fixture.detectChanges();

    expect(component.data).toBeTruthy();
    expect(component.template).toBeTruthy();
  });
});

const template: DisplayRule[] = [
  {
    Path: '_sps',
    ui: {
      Path: '_sps',
      component: 'textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: '',
    ui: {
      Path: 'Generalities',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'DescriptionLevel',
    ui: {
      Path: 'Generalities.DescriptionLevel',
      component: 'select',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'Title',
    ui: {
      Path: 'Generalities.Title',
      component: 'textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'Title_.*',
    ui: {
      Path: 'Generalities.Title_.*',
      component: 'select+textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'Description',
    ui: {
      Path: 'Generalities.Description',
      component: null,
      layout: { columns: 2, size: 'large' },
    },
  },
  {
    Path: 'Description_.*',
    ui: {
      Path: 'Generalities.Description_.*',
      component: 'select+textarea',
      layout: { columns: 2, size: 'large' },
    },
  },
  {
    Path: 'Tag',
    ui: {
      Path: 'Generalities.Tag',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: '',
    ui: {
      Path: 'Generalities.Dates',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'StartDate',
    ui: {
      Path: 'Generalities.Dates.StartDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'EndDate',
    ui: {
      Path: 'Generalities.Dates.EndDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'CreatedDate',
    ui: {
      Path: 'Generalities.Dates.CreatedDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'TransactedDate',
    ui: {
      Path: 'Generalities.Dates.TransactedDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'SentDate',
    ui: {
      Path: 'Generalities.Dates.SentDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'ReceivedDate',
    ui: {
      Path: 'Generalities.Dates.ReceivedDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'RegisteredDate',
    ui: {
      Path: 'Generalities.Dates.RegisteredDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'AcquiredDate',
    ui: {
      Path: 'Generalities.Dates.AcquiredDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'DateLitteral',
    ui: {
      Path: 'Generalities.Dates.DateLitteral',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: '',
    ui: {
      Path: 'Generalities.Identifiers',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'SystemId',
    ui: {
      Path: 'Generalities.Identifiers.SystemId',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'OriginatingSystemId',
    ui: {
      Path: 'Generalities.Identifiers.OriginatingSystemId',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'OriginatingAgencyArchiveUnitIdentifier',
    ui: {
      Path: 'Generalities.Identifiers.OriginatingAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'TransferringAgencyArchiveUnitIdentifier',
    ui: {
      Path: 'Generalities.Identifiers.TransferringAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'ArchivalAgencyArchiveUnitIdentifier',
    ui: {
      Path: 'Generalities.Identifiers.ArchivalAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'FilePlanPosition',
    ui: {
      Path: 'Generalities.Identifiers.FilePlanPosition',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'PersistentIdentifier',
    ui: {
      Path: 'Generalities.PersistentIdentifier',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierType',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierType',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierOrigin',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierOrigin',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierReference',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierReference',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'PersistentIdentifier.PersistentIdentifierContent',
    ui: {
      Path: 'Generalities.PersistentIdentifier.PersistentIdentifierContent',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: '',
    ui: {
      Path: 'Generalities.Characteristics',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    Path: 'Type',
    ui: {
      Path: 'Generalities.Characteristics.Type',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'DocumentType',
    ui: {
      Path: 'Generalities.Characteristics.DocumentType',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'Language',
    ui: {
      Path: 'Generalities.Characteristics.Language',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'DescriptionLanguage',
    ui: {
      Path: 'Generalities.Characteristics.DescriptionLanguage',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'Status',
    ui: {
      Path: 'Generalities.Characteristics.Status',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'Source',
    ui: {
      Path: 'Generalities.Characteristics.Source',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'Version',
    ui: {
      Path: 'Generalities.Characteristics.Version',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'OriginatingSystemIdReplyTo',
    ui: {
      Path: 'Generalities.Characteristics.OriginatingSystemIdReplyTo',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    Path: 'TextContent',
    ui: {
      Path: 'Generalities.Characteristics.TextContent',
      component: 'textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
];
const data = {
  '#id': 'aeaqaaaaaeecehmeabneoamjofozk4iaaaea',
  Title: 'Gambetta par producteur1',
  DescriptionLevel: 'RecordGrp',
  Description: 'Station Gambetta ligne 3 Paris',
  OriginatingAgencyArchiveUnitIdentifier: [],
  '#tenant': 1,
  '#unitups': [],
  '#min': 1,
  '#max': 1,
  '#allunitups': [],
  '#unitType': 'INGEST',
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
  StartDate: '2016-06-03T15:28:00',
  EndDate: '2016-06-03T15:28:00',
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
