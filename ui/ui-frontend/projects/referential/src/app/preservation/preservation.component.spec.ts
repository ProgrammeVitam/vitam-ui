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

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreservationComponent } from './preservation.component';
import {
  GriffinsService,
  InjectorModule,
  LoggerModule,
  PreservationScenariosService,
  SecurityService,
  SnackBarService,
  VitamUICommonModule,
} from 'vitamui-library';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('PreservationComponent', () => {
  let component: PreservationComponent;
  let fixture: ComponentFixture<PreservationComponent>;

  const securityServiceMock = {
    hasRole: vi.fn().mockReturnValue(true),
  };
  const griffinsServiceMock = {
    list: vi.fn().mockReturnValue(of([])),
  };
  const preservationScenariosServiceMock = {
    list: vi.fn().mockReturnValue(of([])),
  };
  const snackBarServiceMock = {
    notifyDownloadStarted: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PreservationComponent, LoggerModule.forRoot(), VitamUICommonModule, InjectorModule],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({ appId: 'referential' }),
            params: of({}),
            paramMap: of({}),
            snapshot: {
              data: { appId: 'referential' },
            },
          },
        },
        {
          provide: SecurityService,
          useValue: securityServiceMock,
        },
        { provide: GriffinsService, useValue: griffinsServiceMock },
        { provide: PreservationScenariosService, useValue: preservationScenariosServiceMock },
        { provide: SnackBarService, useValue: snackBarServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PreservationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('exports', () => {
    let downloadedFileName: string;

    beforeEach(() => {
      snackBarServiceMock.notifyDownloadStarted.mockClear();
      window.URL.createObjectURL = vi.fn().mockReturnValue('blob:mock');
      vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (this: HTMLAnchorElement) {
        downloadedFileName = this.download;
      });
    });

    afterEach(() => {
      vi.restoreAllMocks();
    });

    it('should download the griffins and notify the user that the download started', () => {
      griffinsServiceMock.list.mockReturnValueOnce(of([{ Identifier: 'GRI-1', Name: 'griffin' }]));

      component['exportGriffins']();

      expect(downloadedFileName).toBe('griffins.json');
      expect(snackBarServiceMock.notifyDownloadStarted).toHaveBeenCalledTimes(1);
    });

    it('should download the scenarios and notify the user that the download started', () => {
      preservationScenariosServiceMock.list.mockReturnValueOnce(of([{ Identifier: 'PSC-1', Name: 'scenario' }]));

      component['exportScenarios']();

      expect(downloadedFileName).toBe('scenarios.json');
      expect(snackBarServiceMock.notifyDownloadStarted).toHaveBeenCalledTimes(1);
    });
  });
});
