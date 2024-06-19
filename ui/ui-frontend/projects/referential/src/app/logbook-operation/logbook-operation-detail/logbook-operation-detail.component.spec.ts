/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { HttpClientModule } from '@angular/common/http';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AuthService, BASE_URL, ExternalParametersService, InjectorModule, LogbookService, LoggerModule } from 'vitamui-library';
import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { LogbookDownloadService } from '../logbook-download.service';
import { LogbookOperationDetailComponent } from './logbook-operation-detail.component';
import { TranslateModule } from '@ngx-translate/core';
import { EventTypeBadgeClassPipe } from '../../shared/pipes/event-type-badge-class.pipe';
import { LastEventPipe } from '../../shared/pipes/last-event.pipe';

@Pipe({ name: 'truncate' })
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

describe('LogbookOperationDetailComponent', () => {
  let component: LogbookOperationDetailComponent;
  let fixture: ComponentFixture<LogbookOperationDetailComponent>;

  beforeEach(async () => {
    const parameters: Map<string, string> = new Map<string, string>();
    const externalParametersServiceMock = {
      getUserExternalParameters: () => of(parameters),
    };

    await TestBed.configureTestingModule({
      declarations: [LogbookOperationDetailComponent, EventTypeBadgeClassPipe, LastEventPipe, MockTruncatePipe],
      imports: [
        MatSnackBarModule,
        InjectorModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        VitamUICommonTestModule,
        BrowserAnimationsModule,
        LoggerModule.forRoot(),
        RouterTestingModule,
        NoopAnimationsModule,
        HttpClientModule,
      ],
      providers: [
        { provide: LogbookService, useValue: {} },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: LogbookDownloadService, useValue: { logbookOperationsReloaded: of([{ id: 'event-01' }]) } },
        { provide: AuthService, useValue: {} },
        { provide: ActivatedRoute, useValue: {} },
        { provide: ExternalParametersService, useValue: externalParametersServiceMock },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LogbookOperationDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('Component should be created', () => {
    expect(component).toBeTruthy();
  });
  it('Download button should not be null', () => {
    expect(component.downloadButtonTitle).not.toBeNull();
  });
});
