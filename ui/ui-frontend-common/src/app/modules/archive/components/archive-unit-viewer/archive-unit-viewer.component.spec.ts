import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BASE_URL } from '../../../injection-tokens';
import { LoggerModule } from '../../../logger';
import { ObjectViewerModule } from '../../../object-viewer/object-viewer.module';
import { MockSchemaService } from '../../../object-viewer/services/mock-schema.service';
import { Collection, Schema, SchemaService } from '../../../schema';
import { SchemaApiService } from '../../../schema/schema-api.service';
import { ArchiveUnitViewerComponent } from './archive-unit-viewer.component';

describe('ArchiveUnitViewerComponent', () => {
  let component: ArchiveUnitViewerComponent;
  let fixture: ComponentFixture<ArchiveUnitViewerComponent>;
  const archiveUnit = {
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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveUnitViewerComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [HttpClientTestingModule, ObjectViewerModule, LoggerModule.forRoot(), TranslateModule.forRoot()],
      providers: [
        SchemaService,
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: SchemaService, useClass: MockSchemaService },
        {
          provide: SchemaApiService,
          use: () => ({
            getSchemas: (collections: Collection[]): Observable<Schema[]> => {
              return new MockSchemaService().getSchema(null).pipe(map((schema) => [schema]));
            },
            getSchema: (collection: Collection): Observable<Schema> => {
              return new MockSchemaService().getSchema(null);
            },
          }),
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitViewerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create with archive unit', () => {
    component.data = archiveUnit;

    component.ngOnChanges({
      data: new SimpleChange(null, archiveUnit, true),
    });

    fixture.detectChanges();

    expect(component.data).toBeTruthy();
    expect(component.template).toBeTruthy();
  });
});
