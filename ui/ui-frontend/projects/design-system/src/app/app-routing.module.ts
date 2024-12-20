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
import { RouterModule, Routes } from '@angular/router';
import { QuicklinkModule, QuicklinkStrategy } from 'ngx-quicklink';
import { TableComponent } from './components/organisms/table/table.component';
import { BreadcrumbsComponent } from './components/molecules/breadcrumbs/breadcrumbs.component';
import { ButtonsComponent } from './components/atoms/buttons/buttons.component';
import { ColorsComponent } from './components/tokens/colors/colors.component';
import { ShadowsComponent } from './components/tokens/shadows/shadows.component';
import { IconsComponent } from './components/atoms/icons/icons.component';
import { MiscellaneousComponent } from './components/miscellaneous/miscellaneous.component';
import { LoadersSteppersComponent } from './components/molecules/loaders-steppers/loaders-steppers.component';
import { TooltipComponent } from './components/atoms/tooltip/tooltip.component';
import { TranslationComponent } from './components/translation/translation.component';
import { TypographyComponent } from './components/tokens/typography/typography.component';
import { DesignSystemComponent } from './components/design-system/design-system.component';
import { DesignSystemChipsComponent } from './components/atoms/chips/design-system-chips.component';
import { SpacingComponent } from './components/tokens/spacing/spacing.component';
import { DesignSystemCheckboxComponent } from './components/molecules/inputs/checkbox/design-system-checkbox.component';
import { DesignSystemSelectComponent } from './components/molecules/inputs/select/design-system-select.component';
import { DesignSystemAutocompleteMultiSelectTreeComponent } from './components/molecules/inputs/autocomplete-multi-select-tree/design-system-autocomplete-multi-select-tree.component';
import { DesignSystemOldInputsComponent } from './components/molecules/inputs/old-input/design-system-old-inputs.component';
import { DesignSystemRepeatableInputComponent } from './components/molecules/inputs/repeatable-input/design-system-repeatable-input.component';
import { DesignSystemSearchWithTypeSelectorComponent } from './components/molecules/inputs/search-with-type-selector/design-system-search-with-type-selector.component';
import { DesignSystemMultipleOptionsDatepickerComponent } from './components/molecules/inputs/multiple-options-datepicker/design-system-multiple-options-datepicker.component';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';
import { TabGroupComponent } from './components/organisms/tab-group/tab-group.component';

const routes: Routes = [
  { path: '', component: DesignSystemComponent },
  {
    path: 'tokens',
    children: [
      { path: '', redirectTo: 'colors', pathMatch: 'full' },
      { path: 'colors', component: ColorsComponent },
      { path: 'shadows', component: ShadowsComponent },
      { path: 'typography', component: TypographyComponent },
      { path: 'spacing', component: SpacingComponent },
    ],
  },
  {
    path: 'atoms',
    children: [
      { path: '', redirectTo: 'buttons', pathMatch: 'full' },
      { path: 'buttons', component: ButtonsComponent },
      { path: 'chips', component: DesignSystemChipsComponent },
      { path: 'icons', component: IconsComponent },
      { path: 'tooltip', component: TooltipComponent },
    ],
  },
  {
    path: 'molecules',
    children: [
      { path: '', redirectTo: 'breadcrumbs', pathMatch: 'full' },
      { path: 'breadcrumbs', component: BreadcrumbsComponent },
      { path: 'loaders-steppers', component: LoadersSteppersComponent },
      {
        path: 'inputs',
        children: [
          { path: '', redirectTo: 'select', pathMatch: 'full' },
          { path: 'select', component: DesignSystemSelectComponent },
          { path: 'autocomplete-multi-select-tree', component: DesignSystemAutocompleteMultiSelectTreeComponent },
          { path: 'checkboxes', component: DesignSystemCheckboxComponent },
          { path: 'old-input', component: DesignSystemOldInputsComponent },
          { path: 'multiple-options-datepicker', component: DesignSystemMultipleOptionsDatepickerComponent },
          { path: 'repeatable-input', component: DesignSystemRepeatableInputComponent },
          { path: 'search-with-type-selector', component: DesignSystemSearchWithTypeSelectorComponent },
        ],
      },
    ],
  },
  {
    path: 'organisms',
    children: [
      { path: '', redirectTo: 'table', pathMatch: 'full' },
      { path: 'table', component: TableComponent },
      { path: 'tab-group', component: TabGroupComponent },
    ],
  },
  { path: 'miscellaneous', component: MiscellaneousComponent },
  { path: 'translation', component: TranslationComponent },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [
    QuicklinkModule,
    RouterModule.forRoot(routes, {
      preloadingStrategy: QuicklinkStrategy,
    }),
  ],
  exports: [RouterModule],
  providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }],
})
export class AppRoutingModule {}
