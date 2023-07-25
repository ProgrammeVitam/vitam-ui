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
import { ComponentMapperService } from './services/component-mapper.service';
import { DataStructureService } from './services/data-structure.service';
import { DateDisplayService } from './services/date-display.service';
import { DisplayObjectHelperService } from './services/display-object-helper.service';
import { DisplayRuleHelperService } from './services/display-rule-helper.service';
import { FavoriteEntryService } from './services/favorite-entry.service';
import { LayoutService } from './services/layout.service';
import { PathStrategyDisplayObjectService } from './services/path-strategy-display-object.service';
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
        ComponentMapperService,
        DateDisplayService,
        LayoutService,
        FavoriteEntryService,
        { provide: BASE_URL, useValue: '/fake-api' },
        // { provide: OntologyService, useClass: MockExtendedOntologyService },
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
    path: '_sps',
    ui: {
      path: '_sps',
      component: 'textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: '',
    ui: {
      path: 'Generalities',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'DescriptionLevel',
    ui: {
      path: 'Generalities.DescriptionLevel',
      component: 'select',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'Title',
    ui: {
      path: 'Generalities.Title',
      component: 'textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'Title_.*',
    ui: {
      path: 'Generalities.Title_.*',
      component: 'select+textfield',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'Description',
    ui: {
      path: 'Generalities.Description',
      component: null,
      layout: { columns: 2, size: 'large' },
    },
  },
  {
    path: 'Description_.*',
    ui: {
      path: 'Generalities.Description_.*',
      component: 'select+textarea',
      layout: { columns: 2, size: 'large' },
    },
  },
  {
    path: 'Tag',
    ui: {
      path: 'Generalities.Tag',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: '',
    ui: {
      path: 'Generalities.Dates',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'StartDate',
    ui: {
      path: 'Generalities.Dates.StartDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'EndDate',
    ui: {
      path: 'Generalities.Dates.EndDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'CreatedDate',
    ui: {
      path: 'Generalities.Dates.CreatedDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'TransactedDate',
    ui: {
      path: 'Generalities.Dates.TransactedDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'SentDate',
    ui: {
      path: 'Generalities.Dates.SentDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'ReceivedDate',
    ui: {
      path: 'Generalities.Dates.ReceivedDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'RegisteredDate',
    ui: {
      path: 'Generalities.Dates.RegisteredDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'AcquiredDate',
    ui: {
      path: 'Generalities.Dates.AcquiredDate',
      component: 'datepicker',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'DateLitteral',
    ui: {
      path: 'Generalities.Dates.DateLitteral',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: '',
    ui: {
      path: 'Generalities.Identifiers',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'SystemId',
    ui: {
      path: 'Generalities.Identifiers.SystemId',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'OriginatingSystemId',
    ui: {
      path: 'Generalities.Identifiers.OriginatingSystemId',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'OriginatingAgencyArchiveUnitIdentifier',
    ui: {
      path: 'Generalities.Identifiers.OriginatingAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'TransferringAgencyArchiveUnitIdentifier',
    ui: {
      path: 'Generalities.Identifiers.TransferringAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'ArchivalAgencyArchiveUnitIdentifier',
    ui: {
      path: 'Generalities.Identifiers.ArchivalAgencyArchiveUnitIdentifier',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'FilePlanPosition',
    ui: {
      path: 'Generalities.Identifiers.FilePlanPosition',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'PersistentIdentifier',
    ui: {
      path: 'Generalities.PersistentIdentifier',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'PersistentIdentifier.PersistentIdentifierType',
    ui: {
      path: 'Generalities.PersistentIdentifier.PersistentIdentifierType',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'PersistentIdentifier.PersistentIdentifierOrigin',
    ui: {
      path: 'Generalities.PersistentIdentifier.PersistentIdentifierOrigin',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'PersistentIdentifier.PersistentIdentifierReference',
    ui: {
      path: 'Generalities.PersistentIdentifier.PersistentIdentifierReference',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'PersistentIdentifier.PersistentIdentifierContent',
    ui: {
      path: 'Generalities.PersistentIdentifier.PersistentIdentifierContent',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: '',
    ui: {
      path: 'Generalities.Characteristics',
      component: 'group',
      layout: { columns: 2, size: 'medium' },
    },
  },
  {
    path: 'Type',
    ui: {
      path: 'Generalities.Characteristics.Type',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'DocumentType',
    ui: {
      path: 'Generalities.Characteristics.DocumentType',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'Language',
    ui: {
      path: 'Generalities.Characteristics.Language',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'DescriptionLanguage',
    ui: {
      path: 'Generalities.Characteristics.DescriptionLanguage',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'Status',
    ui: {
      path: 'Generalities.Characteristics.Status',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'Source',
    ui: {
      path: 'Generalities.Characteristics.Source',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'Version',
    ui: {
      path: 'Generalities.Characteristics.Version',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'OriginatingSystemIdReplyTo',
    ui: {
      path: 'Generalities.Characteristics.OriginatingSystemIdReplyTo',
      component: 'textfield',
      layout: { columns: 1, size: 'small' },
    },
  },
  {
    path: 'TextContent',
    ui: {
      path: 'Generalities.Characteristics.TextContent',
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
