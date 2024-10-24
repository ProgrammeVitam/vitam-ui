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

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BASE_URL, SearchCriteriaHistory } from 'vitamui-library';
import { SearchCriteriaSaverService } from '../../services/search-criteria-saver.service';

describe('SearchCriteriaSaverService', () => {
  let service: SearchCriteriaSaverService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [{ provide: BASE_URL, useValue: '/fake-api' }],
    });
    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    service = TestBed.inject(SearchCriteriaSaverService);
  });

  it('the SearchCriteriaSaver Service should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('POST', () => {
    it('should call /fake-api/projects/archive-units/searchcriteriahistory', () => {
      const saveSearchCriteriaHistory: SearchCriteriaHistory = {
        id: 'saveSearchCriteriaId',
        name: 'saveSearchCriteriaName',
        userId: 'userId',
        savingDate: '02/05/2022',
        searchCriteriaList: [],
      };

      const expectedSaveSearchCriteriaHistory: SearchCriteriaHistory = {
        id: 'saveSearchCriteriaId',
        name: 'saveSearchCriteriaName',
        userId: 'userId',
        savingDate: '02/05/2022',
        searchCriteriaList: [],
      };
      service
        .saveSearchCriteriaHistory(saveSearchCriteriaHistory)
        .subscribe((searchCriteriaSaved) => expect(searchCriteriaSaved).toEqual(expectedSaveSearchCriteriaHistory), fail);
      const req = httpTestingController.expectOne('/fake-api/projects/archive-units/searchcriteriahistory');
      expect(req.request.method).toEqual('POST');
      req.flush(expectedSaveSearchCriteriaHistory);
    });
  });

  describe('PUT', () => {
    it('should call /fake-api/projects/archive-units/searchcriteriahistory/saveSearchCriteriaId', () => {
      const saveSearchCriteriaHistory: SearchCriteriaHistory = {
        id: 'saveSearchCriteriaId',
        name: 'saveSearchCriteriaName',
        userId: 'userId',
        savingDate: '02/05/2022',
        searchCriteriaList: [],
      };

      const expectedSaveSearchCriteriaHistory: SearchCriteriaHistory = {
        id: 'saveSearchCriteriaId',
        name: 'saveSearchCriteriaName',
        userId: 'userId',
        savingDate: '02/05/2022',
        searchCriteriaList: [],
      };
      service
        .updateSearchCriteriaHistory(saveSearchCriteriaHistory)
        .subscribe((searchCriteriaSaved) => expect(searchCriteriaSaved).toEqual(expectedSaveSearchCriteriaHistory), fail);
      const req = httpTestingController.expectOne('/fake-api/projects/archive-units/searchcriteriahistory/saveSearchCriteriaId');
      expect(req.request.method).toEqual('PUT');
      req.flush(expectedSaveSearchCriteriaHistory);
    });
  });
});
