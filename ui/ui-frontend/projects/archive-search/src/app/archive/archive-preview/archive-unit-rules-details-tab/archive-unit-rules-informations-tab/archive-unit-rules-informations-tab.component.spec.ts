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
import {
  InheritedPropertyDto, InjectorModule, LoggerModule, ManagementRule, RuleCategoryVitamUiDto, Unit, UnitRuleDto, UnitType,
  VitamuiMissingTranslationHandler,
} from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
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

  // tslint:disable-next-line:directive-selector
  @Directive({ selector: '[app-VitamuiCommonCollapseTriggerFor]' })
  class CollapseTriggerForStubDirective {
    @Input() vitamuiCommonCollapseTriggerFor: any;
  }

  // tslint:disable-next-line:directive-selector
  @Directive({ selector: '[app-vitamuiCommonCollapse]', exportAs: 'vitamuiCommonCollapse' })
  class CollapseStubDirective {
    @Input() vitamuiCommonCollapse: any;
  }

  const inheritedPropertyCarried: InheritedPropertyDto = {
    PropertyName: 'name',
    PropertyValue: {},
    Paths: [['firstId']],
  };
  const inheritedProperty: InheritedPropertyDto = {
    PropertyName: 'name',
    PropertyValue: {},
    Paths: [
      ['firstId', 'secondId', 'thirdId'],
      ['firstId2', 'secondId2', 'thirdId2'],
    ],
  };

  const archiveUnit: Unit = {
    '#management': null,
    '#allunitups': [],
    '#id': 'id',
    '#object': '',
    '#unitType': UnitType.INGEST,
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
        VitamUICommonTestModule,
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

  it('the component should be created', () => {
    expect(component).toBeTruthy();
  });

  it('should the returned key as Final Action value be', () => {
    expect(component.getFinalActionStatus(inheritedProperty)).toEqual(
      'ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.INHERITED'
    );
  });

  it('should return Elimination as Rule Property Name', () => {
    expect(component.getPropertyValue('DESTROY')).not.toBeNull();
    expect(component.getPropertyValue('DESTROY')).toEqual('ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.DESTROY');
    expect(component.getPropertyValue('DESTROY')).toBeDefined();
  });

  it('should the returned key as Rule Category Name be', () => {
    expect(component.getRuleCategoryName('AppraisalRule')).toBeDefined();
    expect(component.getRuleCategoryName('AppraisalRule')).toEqual('ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.CATEGORY_NAME.APPRAISALRULE');
    expect(component.getRuleCategoryName('AppraisalRule')).not.toBeNull();
  });

  it('should return Sort final as Rule Property Name', () => {
    expect(component.getPropertyName('FinalAction')).not.toBeNull();
    expect(component.getPropertyName('FinalAction')).not.toBeNaN();
    expect(component.getPropertyName('FinalAction')).toBeDefined();
    expect(component.getPropertyName('FinalAction')).toEqual('ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.FINAL_ACTION_VALUE');
  });

  it('should the returned key as Rule Property Name be', () => {
    expect(component.getPropertyValue('KEEP')).not.toBeNull();
    expect(component.getPropertyValue('KEEP')).not.toBeNaN();
    expect(component.getPropertyValue('KEEP')).toBeDefined();
    expect(component.getPropertyValue('KEEP')).toEqual('ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.KEEP');
  });

  it('should the returned key as Final Action be carried when the paths contain one element', () => {
    expect(component.getFinalActionStatus(inheritedPropertyCarried)).not.toBeNull();
    expect(component.getFinalActionStatus(inheritedPropertyCarried)).not.toBeNaN();
    expect(component.getFinalActionStatus(inheritedPropertyCarried)).toBeDefined();
    expect(component.getFinalActionStatus(inheritedPropertyCarried)).toEqual(
      'ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.CARRIED'
    );
  });

  it('should the returned key as Final Action be inherited when the paths contains more than 1 element', () => {
    expect(component.getFinalActionStatus(inheritedProperty)).not.toBeNull();
    expect(component.getFinalActionStatus(inheritedProperty)).not.toBeNaN();
    expect(component.getFinalActionStatus(inheritedProperty)).toBeDefined();
    expect(component.getFinalActionStatus(inheritedProperty)).toEqual(
      'ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.RULES_FINAL_ACTION.INHERITED'
    );
  });

  it('the parameter of showing properties list should be true', () => {
    // Given
    component.ruleCategory = 'AppraisalRule';
    // When
    component.archiveUnitRules = archiveUnit;

    // Then
    expect(component.isToShowPropertiesList).not.toBeDefined();
    expect(component.getInheritedRulesDetails(archiveUnit)).not.toBeDefined();
    expect(component.isToShowPropertiesList).not.toBeNull();
  });

  it('the value of listOfPropertiesCollapsed should be false', () => {
    // When
    component.getHoldRuleInformations(unitRuleDto);
    // Then
    expect(component.listOfPropertiesCollapsed).not.toBeTruthy();
  });

  it('the returned value of getClassificationRulePropertyStatus should be undefined when the property is unknown', () => {
    // Given
    const property = 'UnknownProperty';
    // When
    component.archiveUnitRules = archiveUnit;
    // Then
    expect(component.getClassificationRulePropertyStatus(property)).not.toBeDefined();
    expect(component.getClassificationRulePropertyStatus(property)).not.toBeNull();
  });

  it('should have the correct values', () => {
    component.initializeParameters();

    expect(component.unitRuleDTO).toBeNull();
    expect(component.listOfPropertiesCollapsed).toBeFalsy();
    expect(component.propertiesList).toBeNull();
    expect(component.isPreventInheritance).toBeFalsy();
    expect(component.isShowHoldRuleDetails).toBeFalsy();
    expect(component.holdRuleDetails).toBeNull();
  });

  it('should be false', () => {
    component.listOfRulesCollapsed = true;
    component.showlistOfRulesBloc();

    expect(component.listOfRulesCollapsed).toBeFalsy();
  });

  it('should be true', () => {
    component.listOfPropertiesCollapsed = false;
    component.showListOfPropertiesBloc();

    expect(component.listOfPropertiesCollapsed).toBeTruthy();
  });

  it('should return the rigth key to translate', () => {
    // Given
    const expectedResponse = 'ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.CLASSIFICATION_RULE_PROPERTIES.TEST';
    // When
    const response = component.getClassificationRulePropertyName('test');
    // Then
    expect(response).toEqual(expectedResponse);
  });

  // new tests
  it('the returned key of getClassificationRulePropertyStatus when the proprty is ClassificationAudience should be', () => {
    // Given
    const property = 'ClassificationAudience';
    const classificationRule: RuleCategoryVitamUiDto = {
      Rules: [],
      FinalAction: 'sort final',
      ClassificationLevel: 'ClassificationLevel',
      ClassificationOwner: 'ClassificationOwner',
      ClassificationAudience: 'ClassificationAudience',
      ClassificationReassessingDate: '2020/12/12',
      NeedReassessingAuthorization: true,
      Inheritance: null,
    };

    const unitManagementRules: ManagementRule = {
      AppraisalRule: null,
      HoldRule: null,
      StorageRule: null,
      ReuseRule: null,
      ClassificationRule: classificationRule,
      DisseminationRule: null,
      AccessRule: null,
      UpdateOperation: null,
    };
    const archiveUnitDetails: Unit = {
      '#management': unitManagementRules,
      '#allunitups': [],
      '#id': 'id',
      '#object': '',
      '#unitType': UnitType.INGEST,
      '#unitups': [],
      '#opi': '',
      Title_: { fr: 'Teste', en: 'Test' },
      Description_: { fr: 'DescriptionFr', en: 'DescriptionEn' },
    };
    // When
    component.archiveUnitRules = archiveUnitDetails;

    // Then
    expect(component.getClassificationRulePropertyStatus(property)).toBeDefined();
    expect(component.getClassificationRulePropertyStatus(property)).not.toBeNull();
    expect(component.getClassificationRulePropertyStatus(property)).toEqual(
      'ARCHIVE_SEARCH.ARCHIVE_UNIT_RULES_DETAILS.RULE_STATUS.CARRIED'
    );
  });

  describe('DOM', () => {
    it('should have 2 columns ', () => {
      // When
      const nativeElement = fixture.nativeElement;
      const elementColumn = nativeElement.querySelectorAll('.col');

      // Then
      expect(elementColumn.length).toBe(2);
    });
  });
});
