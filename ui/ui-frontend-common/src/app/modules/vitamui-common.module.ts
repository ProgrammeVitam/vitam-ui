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
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { VitamUIDisplayNodeModule } from './components/vitamui-display-node/vitamui-display-node.module';
import { LoggerModule } from './logger/logger.module';

import { AccountModule } from './account/account.module';
import { ApplicationSelectContentModule } from './components/application-select-content/application-select-content.module';
import { BlankComponent } from './components/blank/blank.component';
import { CancelledSnackBarModule } from './components/cancelled-snack-bar/cancelled-snack-bar.module';
import { CollapseModule } from './components/collapse/collapse.module';
import { ConfirmDialogModule } from './components/confirm-dialog/confirm-dialog.module';
import { CountryModule } from './components/country/country.module';
import { CustomerSelectContentModule } from './components/customer-select-content/customer-select-content.module';
import { DownloadSnackBarModule } from './components/download-snack-bar/download-snack-bar.module';
import { EditableFieldModule } from './components/editable-field/editable-field.module';
import { LevelInputModule } from './components/editable-field/level-input/level-input.module';
import { NavbarModule } from './components/navbar/navbar.module';
import { OrderByButtonModule } from './components/order-by-button/order-by-button.module';
import { OrderDropdownModule } from './components/order-dropdown/order-dropdown.module';
import { SearchBarModule } from './components/search-bar/search-bar.module';
import { SlideToggleModule } from './components/slide-toggle/slide-toggle.module';
import { StepperModule } from './components/stepper/stepper.module';
import { VitamUIAutocompleteModule } from './components/vitamui-autocomplete/vitamui-autocomplete.module';
import { VitamUICustomerSelectModule } from './components/vitamui-customer-select/vitamui-customer-select.module';
import { VitamUIDurationInputModule } from './components/vitamui-duration-input/vitamui-duration-input.module';
import { VitamUIFieldErrorModule } from './components/vitamui-field-error/vitamui-field-error.module';
import { VitamUIInputModule } from './components/vitamui-input/vitamui-input.module';
import { VitamUIListInputModule } from './components/vitamui-list-input/vitamui-list-input.module';
import { VitamUIMenuTileModule } from './components/vitamui-menu-tile/vitamui-menu-tile.module';
import { VitamUISnackBarModule } from './components/vitamui-snack-bar/vitamui-snack-bar.module';
import { VitamUITenantSelectModule } from './components/vitamui-tenant-select/vitamui-tenant-select.module';
import { CollapseDirectiveModule } from './directives/collapse/collapse.directive.module';
import { DragAndDropModule } from './directives/drag-and-drop/drag-and-drop.module';
import { InfiniteScrollModule } from './directives/infinite-scroll/infinite-scroll.module';
import { RowCollapseModule } from './directives/row-collapse/row-collapse.module';
import { TooltipModule } from './directives/tooltip/tooltip.module';
import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { SUBROGRATION_REFRESH_RATE_MS, WINDOW_LOCATION } from './injection-tokens';
import { LogbookModule } from './logbook/logbook.module';
import { BytesPipe } from './pipes/bytes.pipe';
import { StrongifyPipe } from './pipes/strongify.pipe';
import { TruncatePipe } from './pipes/truncate.pipe';
import { SecurityModule } from './security/security.module';
import { StartupService } from './startup.service';
import { SubrogationModule } from './subrogation/subrogation.module';
import { VitamUIHttpInterceptor } from './vitamui-http-interceptor';

export function startupServiceFactory(startupService: StartupService) {
  // leave it like this due to run packagr issue :
  // https://github.com/ng-packagr/ng-packagr/issues/696 & https://github.com/angular/angular/issues/23629
  const val = () => startupService.load();

  return val;
}

@NgModule({
  imports: [
    CommonModule,
    HttpClientModule,
    MatDialogModule,
    MatSnackBarModule,
    AccountModule,
    ApplicationSelectContentModule,
    CancelledSnackBarModule,
    CollapseDirectiveModule,
    ConfirmDialogModule,
    CustomerSelectContentModule,
    VitamUICustomerSelectModule,
    VitamUIDisplayNodeModule,
    VitamUIDurationInputModule,
    VitamUIFieldErrorModule,
    VitamUIInputModule,
    VitamUIMenuTileModule,
    VitamUIListInputModule,
    VitamUISnackBarModule,
    VitamUITenantSelectModule,
    DownloadSnackBarModule,
    DragAndDropModule,
    EditableFieldModule,
    InfiniteScrollModule,
    LevelInputModule,
    LogbookModule,
    LoggerModule,
    NavbarModule,
    OrderByButtonModule,
    OrderDropdownModule,
    RowCollapseModule,
    SearchBarModule,
    SecurityModule,
    SlideToggleModule,
    StepperModule,
    SubrogationModule,
    TooltipModule,
    CountryModule,
    VitamUIAutocompleteModule
  ],
  declarations: [
    BlankComponent,
    BytesPipe,
    ErrorDialogComponent,
    StrongifyPipe,
    TruncatePipe
  ],
  entryComponents: [
    ErrorDialogComponent
  ],
  exports: [
    AccountModule,
    ApplicationSelectContentModule,
    BlankComponent,
    BytesPipe,
    ConfirmDialogModule,
    CollapseModule,
    CollapseDirectiveModule,
    VitamUICustomerSelectModule,
    VitamUIDisplayNodeModule,
    VitamUIDurationInputModule,
    VitamUIFieldErrorModule,
    VitamUIInputModule,
    VitamUIListInputModule,
    VitamUIMenuTileModule,
    VitamUITenantSelectModule,
    DragAndDropModule,
    EditableFieldModule,
    InfiniteScrollModule,
    LevelInputModule,
    LogbookModule,
    LoggerModule,
    NavbarModule,
    OrderByButtonModule,
    OrderDropdownModule,
    RowCollapseModule,
    SearchBarModule,
    SecurityModule,
    SlideToggleModule,
    StepperModule,
    StrongifyPipe,
    SubrogationModule,
    TooltipModule,
    TruncatePipe,
    CountryModule,
    VitamUIAutocompleteModule
  ],
  providers: [
    { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 10000 },
    { provide: WINDOW_LOCATION, useValue: window.location },
    {
      provide: APP_INITIALIZER,
      useFactory: startupServiceFactory,
      deps: [StartupService],
      multi: true
    },
    { provide: HTTP_INTERCEPTORS, useClass: VitamUIHttpInterceptor, multi: true }
  ]
})
export class VitamUICommonModule { }
