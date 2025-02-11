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
import { NgModule } from '@angular/core';

import { ConfirmActionModule } from './components/confirm-action/confirm-action.module';
import { FilingPlanModule } from './components/filing-plan/filing-plan.module';
import { VitamUIInputModule } from './components/vitamui-input/vitamui-input.module';
import { VitamUIRadioGroupModule } from './components/vitamui-radio-group/vitamui-radio-group.module';
import { VitamUIRadioModule } from './components/vitamui-radio/vitamui-radio.module';
import { VitamUISelectAllOptionModule } from './components/vitamui-select-all-option/vitamui-select-all-option.module';
import { MAT_TABS_CONFIG } from '@angular/material/tabs';
import { SelectComponent } from './components/select/select.component';
import { MAT_DIALOG_DEFAULT_OPTIONS } from '@angular/material/dialog';
import { MAT_RADIO_DEFAULT_OPTIONS } from '@angular/material/radio';
import { DialogHeaderComponent } from './components/dialog-header/dialog-header.component';
import { PreviousStepComponent } from './components/previous-step/previous-step.component';
import { NextStepComponent } from './components/next-step/next-step.component';
import { SelectWithTreeComponent } from './components/select-with-tree/select-with-tree.component';
import { SearchWithTypeSelectorComponent } from './components/search-with-type-selector/search-with-type-selector.component';
import { FormFieldValueWrapperComponent } from './components/form-field-value-wrapper/form-field-value-wrapper.component';
import { MAT_CHECKBOX_DEFAULT_OPTIONS } from '@angular/material/checkbox';
import { PatternComponent } from './components/pattern/pattern.component';

const components = [
  ConfirmActionModule,
  DialogHeaderComponent,
  FilingPlanModule,
  FormFieldValueWrapperComponent,
  NextStepComponent,
  PatternComponent,
  PreviousStepComponent,
  SearchWithTypeSelectorComponent,
  SelectComponent,
  SelectWithTreeComponent,
  VitamUIInputModule,
  VitamUIRadioGroupModule,
  VitamUIRadioModule,
  VitamUISelectAllOptionModule,
];

@NgModule({
  declarations: [],
  imports: components,
  exports: components,
  providers: [
    { provide: MAT_TABS_CONFIG, useValue: { stretchTabs: false } },
    { provide: MAT_CHECKBOX_DEFAULT_OPTIONS, useValue: { color: 'primary' } },
    { provide: MAT_RADIO_DEFAULT_OPTIONS, useValue: { color: 'primary' } },
    { provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: { autoFocus: false } },
  ],
})
export class VitamUILibraryModule {}
