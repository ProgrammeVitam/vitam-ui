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

import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Directive, Input } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule, NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
import { InjectorModule, LoggerModule, VitamuiMissingTranslationHandler } from 'ui-frontend-common';
import { InheritedPropertyDto, Unit, UnitRuleDto } from '../../../models/unit.interface';
import { ArchiveUnitRulesInformationsTabComponent } from './archive-unit-rules-informations-tab.component';

export function httpLoaderFactory(httpClient: HttpClient): MultiTranslateHttpLoader {
  return new MultiTranslateHttpLoader(httpClient, [
    { prefix: './assets/shared-i18n/', suffix: '.json' },
    { prefix: './assets/i18n/', suffix: '.json' },
  ]);
}

describe('ArchiveUnitRulesInformationsTabComponent', () => {
  let component: ArchiveUnitRulesInformationsTabComponent;
  let fixture: ComponentFixture<ArchiveUnitRulesInformationsTabComponent>;

  @Directive({ selector: '[app-VitamuiCommonCollapseTriggerFor]' })
  class CollapseTriggerForStubDirective {
    @Input() vitamuiCommonCollapseTriggerFor: any;
  }

  @Directive({ selector: '[app-vitamuiCommonCollapse]', exportAs: 'vitamuiCommonCollapse' })
  class CollapseStubDirective {
    @Input() vitamuiCommonCollapse: any;
  }

  const inheritedPropertyCarried: InheritedPropertyDto = {
    PropertyName: 'name',
    PropertyValue: {},
    Paths: ['firstId'],
  };
  const inheritedProperty: InheritedPropertyDto = {
    PropertyName: 'name',
    PropertyValue: {},
    Paths: [['firstId', 'secondId', 'thirdId']],
  };

  const archiveUnit: Unit = {
    '#allunitups': [],
    '#id': 'id',
    '#object': '',
    '#unitType': '',
    '#unitups': [],
    '#opi': '',
    Title_: { fr: 'Teste', en: 'Test' },
    Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
  };

  const unitRuleDto: UnitRuleDto = {
    Rule: 'ruleId',
    StartDate: '22/02/2015',
    EndDate: '01/09/2050',
    Paths: [],
    status: 'Portée',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchiveUnitRulesInformationsTabComponent, CollapseStubDirective, CollapseTriggerForStubDirective],
      imports: [
        InjectorModule,
        MatSnackBarModule,
        NoopAnimationsModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
        LoggerModule.forRoot(),
        TranslateModule.forRoot({
          missingTranslationHandler: { provide: MissingTranslationHandler, useClass: VitamuiMissingTranslationHandler },
          defaultLanguage: 'fr',
          loader: {
            provide: TranslateLoader,
            useFactory: httpLoaderFactory,
            deps: [HttpClient],
          },
        }),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ArchiveUnitRulesInformationsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should return Hérité as Final Action value', () => {
    expect(component.getFinalActionStatus(inheritedProperty)).toEqual('Hérité');
  });

  it('should return Elimination as Rule Property Name', () => {
    expect(component.getPropertyValue('DESTROY')).toEqual('Elimination');
  });

  it('should return DUA as Rule Category Name', () => {
    expect(component.getRuleCategoryName('AppraisalRule')).toEqual('DUA');
  });

  it('should return Sort final as Rule Property Name', () => {
    expect(component.getPropertyName('FinalAction')).toEqual('Sort final');
  });

  it('should return Conservation as Rule Property Name', () => {
    expect(component.getPropertyValue('KEEP')).toEqual('Conservation');
  });

  it('should return Porté as Final Action value', () => {
    expect(component.getFinalActionStatus(inheritedPropertyCarried)).toEqual('Porté');
  });

  it('the parameter of showing properties list should be true', () => {
    component.ruleCategory = 'AppraisalRule';
    component.getInheritedRulesDetails(archiveUnit);
    expect(component.isToShowPropertiesList).toBeTruthy();
  });

  it('the value of listOfPropertiesCollapsed should be false', () => {
    component.getHoldRuleInformations(unitRuleDto);
    expect(component.listOfPropertiesCollapsed).not.toBeTruthy();
  });

  it('the value of listOfPropertiesCollapsed should be false', () => {
    const property = 'UnknownProperty';
    expect(component.getClassificationRulePropertyStatus(property)).toThrowError('No property found');
  });
});
