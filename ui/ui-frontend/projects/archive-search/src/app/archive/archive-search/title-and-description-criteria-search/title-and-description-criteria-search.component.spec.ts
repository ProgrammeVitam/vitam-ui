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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { CriteriaOperator, CriteriaValue, InjectorModule } from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { TitleAndDescriptionCriteriaSearchComponent } from './title-and-description-criteria-search.component';

describe('TitleAndDescriptionCriteriaSearchComponent', () => {
  let component: TitleAndDescriptionCriteriaSearchComponent;
  let fixture: ComponentFixture<TitleAndDescriptionCriteriaSearchComponent>;

  const archiveExchangeDataServiceMock = {
    addSimpleSearchCriteriaSubject: () => of(),
  };

  beforeEach(async () => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });
    await TestBed.configureTestingModule({
      providers: [
        FormBuilder,
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ArchiveSharedDataService, useValue: archiveExchangeDataServiceMock },
      ],
      imports: [InjectorModule, TranslateModule.forRoot(), TitleAndDescriptionCriteriaSearchComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TitleAndDescriptionCriteriaSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('component should create', () => {
    expect(component).toBeTruthy();
  });

  it('should previousTitleDescriptionCriteriaValue to be not null', () => {
    expect(component.previousTitleDescriptionCriteriaValue).not.toBeNull();
  });

  it('should not call addSimpleSearchCriteriaSubject when keyElt is null', () => {
    // Given
    const criteriaValue: CriteriaValue = {
      id: 'criteriaId',
      value: 'criteriaValue',
    };
    spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria(null, criteriaValue, 'labelElt', true, CriteriaOperator.EQ, true);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).not.toHaveBeenCalled();
  });

  it('should emptyTitleDescriptionCriteriaForm to be not null', () => {
    expect(component.emptyTitleDescriptionCriteriaForm).not.toBeNull();
  });

  it('should call addSimpleSearchCriteriaSubject when keyElt and CriteriaValue are not null', () => {
    // Given
    const criteriaValue: CriteriaValue = {
      id: 'criteriaId',
      value: 'criteriaValue',
    };
    spyOn(archiveExchangeDataServiceMock, 'addSimpleSearchCriteriaSubject').and.callThrough();

    // When
    component.addCriteria('keyElt', criteriaValue, 'labelElt', true, CriteriaOperator.EQ, true);

    // Then
    expect(archiveExchangeDataServiceMock.addSimpleSearchCriteriaSubject).toHaveBeenCalled();
  });

  describe('DOM', () => {
    it('should have 1 vitamui editable input ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementRow = nativeElement.querySelectorAll('vitamui-common-editable-input');

      // Then
      expect(elementRow.length).toBe(1);
    });
  });
});
