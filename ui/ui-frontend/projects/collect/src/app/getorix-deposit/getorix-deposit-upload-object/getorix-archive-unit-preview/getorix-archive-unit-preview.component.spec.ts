/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Pipe, PipeTransform, SimpleChange, SimpleChanges } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { Unit } from 'ui-frontend-common';
import { GetorixDepositService } from '../../getorix-deposit.service';
import { GetorixArchiveUnitPreviewComponent } from './getorix-archive-unit-preview.component';

describe('GetorixArchiveUnitPreviewComponent', () => {
  let component: GetorixArchiveUnitPreviewComponent;
  let fixture: ComponentFixture<GetorixArchiveUnitPreviewComponent>;

  @Pipe({ name: 'truncate' })
  class MockTruncatePipe implements PipeTransform {
    transform(value: number): number {
      return value;
    }
  }

  const unit: Unit = {
    '#allunitups': [],
    '#id': 'id',
    '#object': '',
    '#unitType': null,
    '#unitups': [],
    '#opi': '',
    Title: 'title',
    Title_: { fr: 'Teste', en: 'Test' },
    Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
  };

  const getorixDepositMockService = {
    getGetorixDepositById: () => of({}),
    getObjectGroupDetailsById: () => of({}),
    getUnitFullPath: () => of({}),
    getCollectUnitDetails: () => of(unit),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GetorixArchiveUnitPreviewComponent, MockTruncatePipe],
      imports: [HttpClientTestingModule, TranslateModule.forRoot()],
      providers: [{ provide: GetorixDepositService, useValue: getorixDepositMockService }],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GetorixArchiveUnitPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('component should work normaly after archiveUnitId change', () => {
    let archiveUnitIdSimplesChange: SimpleChange = {
      previousValue: 'archiveUnitIdFirst',
      currentValue: 'archiveUnitIdIdNew',
      firstChange: false,
      isFirstChange: () => false,
    };
    let SimpleChanges: SimpleChanges = {
      archiveUnitId: archiveUnitIdSimplesChange,
    };

    component.ngOnChanges(SimpleChanges);
    expect(component).toBeTruthy();
  });

  it('should work', () => {
    component.emitClose();
    expect(component).toBeTruthy();
  });
});
