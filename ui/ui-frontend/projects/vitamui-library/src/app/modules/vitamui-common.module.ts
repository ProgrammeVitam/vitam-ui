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
import { APP_INITIALIZER, ModuleWithProviders, NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyDialogModule as MatDialogModule } from '@angular/material/legacy-dialog';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { first, switchMap } from 'rxjs/operators';
import { AccountModule } from './account/account.module';
import { ArchiveUnitModule } from './archive-unit/archive-unit.module';
import { AuthService } from './auth.service';
import { AccordionModule } from './components/accordion/accordion.module';
import { ApplicationCardModule } from './components/application';
import { ApplicationSelectContentModule } from './components/application-select-content/application-select-content.module';
import {
  VitamUIAutocompleteModule,
  VitamUIAutocompleteMultiSelectModule,
  VitamUIAutocompleteMultiSelectTreeModule,
} from './components/autocomplete';
import { BlankComponent } from './components/blank/blank.component';
import { CancelledSnackBarModule } from './components/cancelled-snack-bar/cancelled-snack-bar.module';
import { ChipComponent } from './components/chip/chip.component';
import { CollapseModule } from './components/collapse/collapse.module';
import { CommonProgressBarModule } from './components/common-progress-bar/common-progress-bar.module';
import { CommonTooltipModule } from './components/common-tooltip/common-tooltip.module';
import { ConfirmDialogModule } from './components/confirm-dialog/confirm-dialog.module';
import { CustomerSelectContentModule } from './components/customer-select-content/customer-select-content.module';
import { DataModule } from './components/data/data.module';
import { DatepickerModule } from './components/datepicker/datepicker.module';
import { DownloadSnackBarModule } from './components/download-snack-bar/download-snack-bar.module';
import { EditableFieldModule } from './components/editable-field/editable-field.module';
import { FileSelectorComponent } from './components/file-selector/file-selector.component';
import { FooterModule } from './components/footer/footer.module';
import { HeaderModule } from './components/header/header.module';
import { SelectLanguageModule } from './components/header/select-language/select-language.module';
import { SelectTenantDialogModule } from './components/header/select-tenant-dialog/select-tenant-dialog.module';
import { UserPhotoModule } from './components/header/user-photo/user-photo.module';
import { LogbookOperationFacetModule } from './components/logbook-operation-facet/logbook-operation-facet.module';
import { MultipleOptionsDatepickerModule } from './components/multiple-options-datepicker/multiple-options-datepicker.module';
import { NavbarModule } from './components/navbar/navbar.module';
import { OrderByButtonModule } from './components/order-by-button/order-by-button.module';
import { OrderDropdownModule } from './components/order-dropdown/order-dropdown.module';
import { ScrollTopModule } from './components/scroll-top/scroll-top.module';
import { SearchBarWithSiblingButtonModule } from './components/search-bar-with-sibling-button/search-bar-with-sibling-button.module';
import { SearchBarModule } from './components/search-bar/search-bar.module';
import { SlideToggleModule } from './components/slide-toggle/slide-toggle.module';
import { StepperModule } from './components/stepper/stepper.module';
import { UserAlertCardModule } from './components/user-alerts/user-alerts-card';
import { VitamuiBodyModule } from './components/vitamui-body/vitamui-body.module';
import { VitamuiCommonBannerModule } from './components/vitamui-common-banner/vitamui-common-banner.module';
import { VitamuiCommonSelectModule } from './components/vitamui-common-select/vitamui-common-select.module';
import { VitamuiContentBreadcrumbModule } from './components/vitamui-content-breadcrumb/vitamui-content-breadcrumb.module';
import { VitamUICustomerSelectModule } from './components/vitamui-customer-select/vitamui-customer-select.module';
import { VitamUIDisplayNodeModule } from './components/vitamui-display-node/vitamui-display-node.module';
import { VitamuiDragDropFileModule } from './components/vitamui-drag-drop-file/vitamui-drag-drop-file.module';
import { VitamUIDurationInputModule } from './components/vitamui-duration-input/vitamui-duration-input.module';
import { VitamuiFacetModule } from './components/vitamui-facet/vitamui-facet.module';
import { VitamUIFieldErrorModule } from './components/vitamui-field-error/vitamui-field-error.module';
import { VitamUICommonInputModule } from './components/vitamui-input/vitamui-common-input.module';
import { VitamuiIntervalDatePickerComponent } from './components/vitamui-interval-date-picker/vitamui-interval-date-picker.component';
import { VitamUIListInputModule } from './components/vitamui-list-input/vitamui-list-input.module';
import { VitamuiMenuButtonModule } from './components/vitamui-menu-button/vitamui-menu-button.module';
import { VitamUIMenuTileModule } from './components/vitamui-menu-tile/vitamui-menu-tile.module';
import { VitamuiMultiInputsModule } from './components/vitamui-multi-inputs/vitamui-multi-inputs.module';
import { VitamuiRepeatableInputModule } from './components/vitamui-repeatable-input/vitamui-repeatable-input.module';
import { VitamuiSidenavHeaderModule } from './components/vitamui-sidenav-header/vitamui-sidenav-header.module';
import { VitamUISnackBarModule } from './components/vitamui-snack-bar/vitamui-snack-bar.module';
import { VitamUITenantSelectModule } from './components/vitamui-tenant-select/vitamui-tenant-select.module';
import { VitamuiTreeNodeModule } from './components/vitamui-tree-node';
import { ConfigService } from './config.service';
import { AutocompletePositionDirectiveModule } from './directives/autocomplete-position/autocomplete-position.directive.module';
import { CollapseDirectiveModule } from './directives/collapse/collapse.directive.module';
import { DragAndDropDirective } from './directives/drag-and-drop/drag-and-drop.directive';
import { EllipsisDirectiveModule } from './directives/ellipsis/ellipsis.directive.module';
import { InfiniteScrollModule } from './directives/infinite-scroll/infinite-scroll.module';
import { ResizeSidebarModule } from './directives/resize-sidebar/resize-sidebar.module';
import { RowCollapseModule } from './directives/row-collapse/row-collapse.module';
import { TooltipModule } from './directives/tooltip/tooltip.module';
import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { ENVIRONMENT, SUBROGRATION_REFRESH_RATE_MS, WINDOW_LOCATION } from './injection-tokens';
import { LogbookModule } from './logbook/logbook.module';
import { LoggerModule } from './logger/logger.module';
import { ObjectEditorModule } from './object-editor/object-editor.module';
import { ObjectViewerModule } from './object-viewer/object-viewer.module';
import { PipesModule } from './pipes/pipes.module';
import { SecurityModule } from './security/security.module';
import { StartupService } from './startup.service';
import { SubrogationModule } from './subrogation/subrogation.module';
import { VitamUIHttpInterceptor } from './vitamui-http-interceptor';

export function loadConfigFactory(configService: ConfigService, environment: any) {
  const p = () => configService.load(environment.configUrls).toPromise();

  return p;
}

export function startupServiceFactory(startupService: StartupService, authService: AuthService) {
  // leave it like this due to run packagr issue :
  // https://github.com/ng-packagr/ng-packagr/issues/696 & https://github.com/angular/angular/issues/
  const p = () =>
    new Promise((resolve) => {
      authService
        .login()
        .pipe(
          first((authenticated) => authenticated),
          switchMap(() => startupService.load()),
        )
        .subscribe(() => resolve(true));
    });

  return p;
}

@NgModule({
  declarations: [BlankComponent, ErrorDialogComponent, VitamuiIntervalDatePickerComponent],
  imports: [
    AccordionModule,
    AccountModule,
    ApplicationCardModule,
    ApplicationSelectContentModule,
    ArchiveUnitModule,
    AutocompletePositionDirectiveModule,
    CancelledSnackBarModule,
    CollapseDirectiveModule,
    CommonModule,
    CommonProgressBarModule,
    ConfirmDialogModule,
    CustomerSelectContentModule,
    DataModule,
    DatepickerModule,
    MultipleOptionsDatepickerModule,
    DownloadSnackBarModule,
    DragAndDropDirective,
    EditableFieldModule,
    EllipsisDirectiveModule,
    FooterModule,
    HeaderModule,
    HttpClientModule,
    InfiniteScrollModule,
    LogbookModule,
    LogbookOperationFacetModule,
    LoggerModule,
    MatDatepickerModule,
    MatDialogModule,
    MatSnackBarModule,
    NavbarModule,
    ObjectEditorModule,
    ObjectViewerModule,
    OrderByButtonModule,
    OrderDropdownModule,
    PipesModule,
    ReactiveFormsModule,
    ResizeSidebarModule,
    RowCollapseModule,
    ScrollTopModule,
    SearchBarModule,
    SearchBarWithSiblingButtonModule,
    SecurityModule,
    SelectTenantDialogModule,
    SlideToggleModule,
    StepperModule,
    SubrogationModule,
    TooltipModule,
    TranslateModule,
    UserAlertCardModule,
    UserPhotoModule,
    VitamUIAutocompleteModule,
    VitamUIAutocompleteMultiSelectModule,
    VitamUIAutocompleteMultiSelectTreeModule,
    VitamuiBodyModule,
    VitamuiCommonBannerModule,
    VitamUICommonInputModule,
    VitamuiCommonSelectModule,
    VitamuiContentBreadcrumbModule,
    VitamUICustomerSelectModule,
    VitamUIDisplayNodeModule,
    VitamuiDragDropFileModule,
    VitamUIDurationInputModule,
    VitamuiFacetModule,
    VitamUIFieldErrorModule,
    VitamUIListInputModule,
    VitamuiMenuButtonModule,
    VitamUIMenuTileModule,
    VitamuiRepeatableInputModule,
    VitamuiSidenavHeaderModule,
    VitamUISnackBarModule,
    VitamUITenantSelectModule,
    FileSelectorComponent,
    ChipComponent,
  ],
  exports: [
    AccordionModule,
    AccountModule,
    ApplicationCardModule,
    ApplicationSelectContentModule,
    ArchiveUnitModule,
    AutocompletePositionDirectiveModule,
    BlankComponent,
    CollapseDirectiveModule,
    CollapseModule,
    CommonProgressBarModule,
    CommonTooltipModule,
    ConfirmDialogModule,
    DataModule,
    DatepickerModule,
    MultipleOptionsDatepickerModule,
    DragAndDropDirective,
    EditableFieldModule,
    EllipsisDirectiveModule,
    FooterModule,
    HeaderModule,
    InfiniteScrollModule,
    LogbookModule,
    LogbookOperationFacetModule,
    LoggerModule,
    NavbarModule,
    ObjectEditorModule,
    ObjectViewerModule,
    OrderByButtonModule,
    OrderDropdownModule,
    PipesModule,
    ResizeSidebarModule,
    RowCollapseModule,
    ScrollTopModule,
    SearchBarModule,
    SearchBarWithSiblingButtonModule,
    SecurityModule,
    SelectLanguageModule,
    SelectTenantDialogModule,
    SlideToggleModule,
    StepperModule,
    SubrogationModule,
    TooltipModule,
    TranslateModule,
    UserAlertCardModule,
    UserPhotoModule,
    VitamUIAutocompleteModule,
    VitamUIAutocompleteMultiSelectModule,
    VitamUIAutocompleteMultiSelectTreeModule,
    VitamuiBodyModule,
    VitamuiCommonBannerModule,
    VitamUICommonInputModule,
    VitamuiCommonSelectModule,
    VitamuiContentBreadcrumbModule,
    VitamUICustomerSelectModule,
    VitamUIDisplayNodeModule,
    VitamuiDragDropFileModule,
    VitamUIDurationInputModule,
    VitamuiFacetModule,
    VitamUIFieldErrorModule,
    VitamuiIntervalDatePickerComponent,
    VitamUIListInputModule,
    VitamuiMenuButtonModule,
    VitamUIMenuTileModule,
    VitamuiMultiInputsModule,
    VitamuiRepeatableInputModule,
    VitamuiSidenavHeaderModule,
    VitamUISnackBarModule,
    VitamUITenantSelectModule,
    VitamuiTreeNodeModule,
    FileSelectorComponent,
    ChipComponent,
  ],
})
export class VitamUICommonModule {
  static forRoot(): ModuleWithProviders<VitamUICommonModule> {
    return {
      ngModule: VitamUICommonModule,
      providers: [
        { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 10000 },
        { provide: WINDOW_LOCATION, useValue: window.location },
        {
          provide: APP_INITIALIZER,
          useFactory: loadConfigFactory,
          deps: [ConfigService, ENVIRONMENT],
          multi: true,
        },
        {
          provide: APP_INITIALIZER,
          useFactory: startupServiceFactory,
          deps: [StartupService, AuthService],
          multi: true,
        },
        { provide: HTTP_INTERCEPTORS, useClass: VitamUIHttpInterceptor, multi: true },
      ],
    };
  }
}
