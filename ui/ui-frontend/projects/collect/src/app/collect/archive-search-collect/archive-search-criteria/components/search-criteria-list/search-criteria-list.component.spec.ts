/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { DatePipe } from '@angular/common';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { environment } from 'projects/collect/src/environments/environment';
import { Observable, of } from 'rxjs';
import {
  CriteriaDataType,
  CriteriaOperator,
  InjectorModule,
  LoggerModule,
  SearchCriteriaEltements,
  SearchCriteriaHistory,
} from 'vitamui-library';
import { VitamUISnackBar } from '../../../../shared/vitamui-snack-bar/vitamui-snack-bar.service';
import { VitamInternalFields } from '../../models/utils';
import { ArchiveSharedDataService } from '../../services/archive-shared-data.service';
import { SearchCriteriaSaverService } from '../../services/search-criteria-saver.service';
import { SearchCriteriaListComponent } from './search-criteria-list.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

@Pipe({
  name: 'truncate',
  standalone: true,
})
class MockTruncatePipe implements PipeTransform {
  transform(value: number): number {
    return value;
  }
}

const translations: any = { TEST: 'Mock translate test' };

class FakeLoader implements TranslateLoader {
  getTranslation(): Observable<any> {
    return of(translations);
  }
}

describe('SearchCriteriaListComponent', () => {
  let component: SearchCriteriaListComponent;
  let fixture: ComponentFixture<SearchCriteriaListComponent>;

  const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['open']);
  matDialogRefSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
  matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

  const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

  const SearchCriteriaSaverServiceStub = {
    getSearchCriteriaHistory: () => of([]),

    deleteSearchCriteriaHistory: () => of(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        MatSnackBarModule,
        InjectorModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: FakeLoader },
        }),
        RouterTestingModule,
        SearchCriteriaListComponent,
        MockTruncatePipe,
      ],
      providers: [
        ArchiveSharedDataService,
        DatePipe,
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MatDialog, useValue: matDialogRefSpy },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: SearchCriteriaSaverService, useValue: SearchCriteriaSaverServiceStub },
        {
          provide: ActivatedRoute,
          useValue: { params: of({ tenantIdentifier: 1 }), data: of({ appId: 'ARCHIVE_SEARCH_MANAGEMENT_APP' }) },
        },
        { provide: environment, useValue: environment },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchCriteriaListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('clearElement', () => {
    let searchCriteriaHistory$: SearchCriteriaHistory[] = [];
    let criteriaList$: SearchCriteriaEltements[] = [];
    beforeEach(() => {
      // Given
      criteriaList$ = [
        {
          criteria: 'Title',
          values: [
            { value: 'vdsvdv', id: 'vdsvdv' },
            { value: 'dfbdfd', id: 'dfbdfd' },
          ],
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
          category: 'FIELDS',
        },
        {
          criteria: 'Description',
          values: [{ value: 'dfddfgdfdgg', id: 'dfddfgdfdgg' }],
          category: 'FIELDS',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
        {
          criteria: VitamInternalFields.OPI,
          values: [
            { value: 'dfgdfgdfgdfgdfgfdg', id: 'dfgdfgdfgdfgdfgfdg' },
            { value: 'gggggggggg', id: 'gggggggggg' },
          ],
          category: 'FIELDS',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
        ,
        {
          criteria: 'NODE',
          values: [
            { value: 'node1', id: 'node1' },
            { value: 'node2', id: 'node2' },
            { value: 'node3', id: 'node3' },
          ],
          category: 'NODES',
          dataType: CriteriaDataType.STRING,
          operator: CriteriaOperator.EQ,
          keyTranslated: false,
          valueTranslated: false,
        },
      ];

      searchCriteriaHistory$ = [
        {
          id: 'id1',
          name: 'First Svae',
          savingDate: new Date().toISOString(),
          searchCriteriaList: criteriaList$,
        },
        {
          id: 'id2',
          name: 'Second Svae',
          savingDate: new Date().toISOString(),
          searchCriteriaList: criteriaList$,
        },
      ];

      component.searchCriteriaHistory = searchCriteriaHistory$;
    });

    describe('deleteSearchCriteriaHistory', () => {
      it('should delete searchCriteria', () => {
        component.clearElement(searchCriteriaHistory$[0].id);
        expect(component.searchCriteriaHistory.length).toEqual(1);
        expect(component.searchCriteriaHistory[0].name).toEqual('Second Svae');
      });
    });
  });
});
