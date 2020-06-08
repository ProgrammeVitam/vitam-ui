import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { IngestComponent } from './ingest.component';
import { MatSidenavModule, MatMenuModule } from '@angular/material';
import { InjectorModule, LoggerModule } from 'ui-frontend-common';
import { ActivatedRoute } from '@angular/router';
import { EMPTY, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { IngestService } from './ingest.service';

describe('IngestComponent', () => {
  let component: IngestComponent;
  let fixture: ComponentFixture<IngestComponent>;
  const ingestServiceMock = {
    ingest: () => of('test ingest')
  };
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatSidenavModule,
        InjectorModule,
        VitamUICommonTestModule,
        LoggerModule.forRoot()
      ],
      declarations: [
        IngestComponent,
      ],
      providers: [
        { provide: IngestService, useValue: ingestServiceMock },
        { provide: ActivatedRoute, useValue: { data: EMPTY } },
        { provide: environment, useValue: environment }
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
