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
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatMenuModule } from '@angular/material/menu';

import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { BASE_URL, LogbookService } from 'vitamui-library';
import { LogbookOperation } from '../../models/logbook-event.interface';
import { IngestService } from '../ingest.service';
import { IngestPreviewComponent } from './ingest-preview.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

@Pipe({
  name: 'truncate',
  standalone: false,
})
class MockTruncatePipe implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

describe('IngestPreviewComponent test:', () => {
  let component: IngestPreviewComponent;
  let fixture: ComponentFixture<IngestPreviewComponent>;
  const logbookOperation: LogbookOperation = { id: 'aeeaaaaaaoem5lyiaa3lialtbt3j6haaaaaq', agIdExt: {}, events: [{}] };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IngestPreviewComponent, MockTruncatePipe],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [MatMenuModule],
      providers: [
        { provide: LogbookService, useValue: {} },
        {
          provide: IngestService,
          useValue: {
            getIngestOperation: (_id: string) => of(logbookOperation),
            logbookOperationsReloaded: of([logbookOperation]),
          },
        },
        { provide: BASE_URL, useValue: '/fake-api' },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IngestPreviewComponent);
    component = fixture.componentInstance;
    component.ingestFromParent = logbookOperation;
    fixture.detectChanges();
  });

  it('should be truthy', () => {
    expect(component).toBeTruthy();
  });

  it('should have ingestFromParent defined', () => {
    expect(component.ingestFromParent).toEqual(logbookOperation);
  });
});
