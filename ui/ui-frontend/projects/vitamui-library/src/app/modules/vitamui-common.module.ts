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
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { inject, ModuleWithProviders, NgModule, provideAppInitializer } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { TranslateModule } from '@ngx-translate/core';
import { first, switchMap } from 'rxjs/operators';
import { AccountModule } from './account/account.module';
import { ArchiveUnitModule } from './archive-unit/archive-unit.module';
import { AuthService } from './auth.service';
import { ChipComponent } from './components/chip/chip.component';
import { CollapseModule } from './components/collapse/collapse.module';
import { CommonTooltipModule } from './components/common-tooltip/common-tooltip.module';
import { ConfirmDialogModule } from './components/confirm-dialog/confirm-dialog.module';
import { DownloadSnackBarModule } from './components/download-snack-bar/download-snack-bar.module';
import { EditableFieldModule } from './components/editable-field/editable-field.module';
import { FileSelectorComponent } from './components/file-selector/file-selector.component';
import { HeaderModule } from './components/header/header.module';
import { SelectLanguageModule } from './components/header/select-language/select-language.module';
import { UserPhotoModule } from './components/header/user-photo/user-photo.module';
import { LogbookOperationFacetComponent } from './components/logbook-operation-facet/logbook-operation-facet.component';
import { VitamuiIntervalDatePickerComponent } from './components/vitamui-interval-date-picker/vitamui-interval-date-picker.component';
import { VitamuiMultiInputsModule } from './components/vitamui-multi-inputs/vitamui-multi-inputs.module';
import { VitamuiSidenavHeaderModule } from './components/vitamui-sidenav-header/vitamui-sidenav-header.module';
import { ConfigService } from './config.service';
import { AutocompletePositionDirectiveModule } from './directives/autocomplete-position/autocomplete-position.directive.module';
import { CollapseDirectiveModule } from './directives/collapse/collapse.directive.module';
import { DragAndDropDirective } from './directives/drag-and-drop/drag-and-drop.directive';
import { ClickOutsideDirective } from './directives/click-outside/click-outside.directive';
import { EllipsisDirectiveModule } from './directives/ellipsis/ellipsis.directive.module';
import { InfiniteScrollModule } from './directives/infinite-scroll/infinite-scroll.module';
import { ResizeSidebarModule } from './directives/resize-sidebar/resize-sidebar.module';
import { RowCollapseModule } from './directives/row-collapse/row-collapse.module';
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
import { BadgeComponent } from './components/badge/badge.component';
import { DataComponent } from './components/data/data.component';
import { ApplicationCardComponent } from './components/application-card/application-card.component';
import { AccordionComponent } from './components/accordion/accordion.component';
import { CommonProgressBarComponent } from './components/common-progress-bar/common-progress-bar.component';
import { DatepickerComponent } from './components/datepicker/datepicker.component';
import { FooterComponent } from './components/footer/footer.component';
import { OrderByButtonComponent } from './components/order-by-button/order-by-button.component';
import { VitamuiBodyComponent } from './components/vitamui-body/vitamui-body.component';
import { SlideToggleComponent } from '../../lib/components/slide-toggle/slide-toggle.component';
import { StepperComponent } from './components/stepper/stepper.component';
import { CdkStepperModule } from '@angular/cdk/stepper';
import { TableFilterComponent } from './components/table-filter/table-filter.component';
import { TableFilterOptionComponent } from './components/table-filter/table-filter-option/table-filter-option.component';
import { TableFilterSearchComponent } from './components/table-filter/table-filter-search.component';
import { TableFilterDirective } from './components/table-filter/table-filter.directive';
import { SearchBarComponent } from './components/search-bar/search-bar.component';
import { VitamuiBannerComponent } from './components/vitamui-banner/vitamui-banner.component';
import { VitamuiTitleBreadcrumbComponent } from './components/vitamui-title-breadcrumb/vitamui-title-breadcrumb.component';
import { VitamuiDragDropFileComponent } from './components/vitamui-drag-drop-file/vitamui-drag-drop-file.component';
import { VitamuiFacetComponent } from './components/vitamui-facet/vitamui-facet.component';
import { VitamuiMenuButtonComponent } from './components/vitamui-menu-button/vitamui-menu-button.component';
import { SnackBarComponent } from './components/snack-bar/snack-bar.component';
import { VitamuiSupHeaderComponent } from './components/vitamui-sup-header/vitamui-sup-header.component';
import { VitamuiTreeNodeComponent } from './components/vitamui-tree-node/vitamui-tree-node.component';
import { InputComponent } from '../../lib/components/input/input.component';
import { VitamUIFieldErrorComponent } from './components/vitamui-field-error/vitamui-field-error.component';
import { ElementsComponent } from './components/elements/elements.component';

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
  declarations: [VitamuiIntervalDatePickerComponent],
  exports: [
    AccordionComponent,
    AccountModule,
    ApplicationCardComponent,
    ArchiveUnitModule,
    AutocompletePositionDirectiveModule,
    BadgeComponent,
    CdkStepperModule,
    ChipComponent,
    ElementsComponent,
    ClickOutsideDirective,
    CollapseDirectiveModule,
    CollapseModule,
    CommonProgressBarComponent,
    CommonTooltipModule,
    ConfirmDialogModule,
    DataComponent,
    DatepickerComponent,
    DragAndDropDirective,
    EditableFieldModule,
    EllipsisDirectiveModule,
    FileSelectorComponent,
    FooterComponent,
    HeaderModule,
    InfiniteScrollModule,
    InputComponent,
    LogbookModule,
    LogbookOperationFacetComponent,
    LoggerModule,
    ObjectEditorModule,
    ObjectViewerModule,
    OrderByButtonComponent,
    PipesModule,
    ResizeSidebarModule,
    RowCollapseModule,
    SearchBarComponent,
    SecurityModule,
    SelectLanguageModule,
    SlideToggleComponent,
    StepperComponent,
    SubrogationModule,
    TableFilterComponent,
    TableFilterDirective,
    TableFilterOptionComponent,
    TableFilterSearchComponent,
    TranslateModule,
    UserPhotoModule,
    VitamUIFieldErrorComponent,
    SnackBarComponent,
    VitamuiBannerComponent,
    VitamuiBodyComponent,
    VitamuiDragDropFileComponent,
    VitamuiFacetComponent,
    VitamuiIntervalDatePickerComponent,
    VitamuiMenuButtonComponent,
    VitamuiMultiInputsModule,
    VitamuiSidenavHeaderModule,
    VitamuiSupHeaderComponent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiTreeNodeComponent,
  ],
  imports: [
    AccordionComponent,
    AccountModule,
    ApplicationCardComponent,
    ArchiveUnitModule,
    AutocompletePositionDirectiveModule,
    BadgeComponent,
    ChipComponent,
    ElementsComponent,
    ClickOutsideDirective,
    CollapseDirectiveModule,
    CommonModule,
    CommonProgressBarComponent,
    ConfirmDialogModule,
    DataComponent,
    DatepickerComponent,
    DownloadSnackBarModule,
    DragAndDropDirective,
    EditableFieldModule,
    EllipsisDirectiveModule,
    FileSelectorComponent,
    FooterComponent,
    HeaderModule,
    InfiniteScrollModule,
    InputComponent,
    LogbookModule,
    LogbookOperationFacetComponent,
    LoggerModule,
    MatDatepickerModule,
    MatDialogModule,
    ObjectEditorModule,
    ObjectViewerModule,
    OrderByButtonComponent,
    PipesModule,
    ReactiveFormsModule,
    ResizeSidebarModule,
    RowCollapseModule,
    SearchBarComponent,
    SecurityModule,
    SlideToggleComponent,
    StepperComponent,
    SubrogationModule,
    TableFilterComponent,
    TableFilterDirective,
    TableFilterOptionComponent,
    TableFilterSearchComponent,
    TranslateModule,
    UserPhotoModule,
    VitamUIFieldErrorComponent,
    SnackBarComponent,
    VitamuiBannerComponent,
    VitamuiBodyComponent,
    VitamuiDragDropFileComponent,
    VitamuiFacetComponent,
    VitamuiMenuButtonComponent,
    VitamuiMultiInputsModule,
    VitamuiSidenavHeaderModule,
    VitamuiSupHeaderComponent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiTreeNodeComponent,
  ],
  providers: [provideHttpClient(withInterceptorsFromDi())],
})
export class VitamUICommonModule {
  static forRoot(): ModuleWithProviders<VitamUICommonModule> {
    return {
      ngModule: VitamUICommonModule,
      providers: [
        { provide: SUBROGRATION_REFRESH_RATE_MS, useValue: 10000 },
        { provide: WINDOW_LOCATION, useValue: window.location },
        provideAppInitializer(() => {
          const initializerFn = loadConfigFactory(inject(ConfigService), inject(ENVIRONMENT));
          return initializerFn();
        }),
        provideAppInitializer(() => {
          const initializerFn = startupServiceFactory(inject(StartupService), inject(AuthService));
          return initializerFn();
        }),
        { provide: HTTP_INTERCEPTORS, useClass: VitamUIHttpInterceptor, multi: true },
      ],
    };
  }
}
