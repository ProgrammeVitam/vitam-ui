/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslateModule } from '@ngx-translate/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { Direction, InfiniteScrollTable, PageRequest, SearchService, TableFilterModule } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { AccessionRegistersService } from '../accession-register.service';
import { AccessionRegisterListComponent } from './accession-register-list.component';

describe('AccessionRegisterListComponent', () => {
  let fixture: ComponentFixture<AccessionRegisterListComponent>;
  let component: AccessionRegisterListComponent;

  let accessionRegistersService: {
    getAccessionRegisterStatus: () => Observable<any>;
    getDateIntervalChanges: () => BehaviorSubject<any>;
    getAdvancedSearchData: () => BehaviorSubject<any>;
  };
  let searchService: { search: () => Observable<{}> };

  beforeEach(waitForAsync(() => {
    accessionRegistersService = {
      getAccessionRegisterStatus: () => of({}),
      getDateIntervalChanges: () => new BehaviorSubject<any>({}),
      getAdvancedSearchData: () => new BehaviorSubject<any>({}),
    };
    searchService = {
      search: () => of({}),
    };

    TestBed.configureTestingModule({
      declarations: [AccessionRegisterListComponent],
      imports: [TranslateModule.forRoot(), VitamUICommonTestModule, MatProgressSpinnerModule, HttpClientTestingModule, TableFilterModule],
      providers: [
        { provide: AccessionRegistersService, useValue: accessionRegistersService },
        { provide: SearchService, useValue: searchService },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();
    fixture = TestBed.createComponent(AccessionRegisterListComponent);
    component = fixture.componentInstance;
  }));

  describe('searchRequest', () => {
    it('searchRequest should work', () => {
      // Given
      const searchEmit = spyOn(InfiniteScrollTable.prototype, 'search');
      accessionRegistersService.getDateIntervalChanges = () =>
        new BehaviorSubject({
          endDateMin: '',
          endDateMax: '14/09/1988',
        });
      // When
      component.searchRequest();
      // Then
      expect(searchEmit).toHaveBeenCalledWith(
        new PageRequest(
          0,
          20,
          'EndDate',
          Direction.DESCENDANT,
          '{"filters":{},"endDateInterval":{"endDateMin":"","endDateMax":"14/09/1988"}}',
        ),
      );
    });
  });
});
