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
import { first, switchMap } from 'rxjs/operators';

import { ArchiveUnitModule } from './archive-unit/archive-unit.module';
import { AuthService } from './auth.service';
import { ChipComponent } from './components/chip/chip.component';

import { FileSelectorComponent } from './components/file-selector/file-selector.component';
import { HeaderModule } from './components/header/header.module';

import { LogbookOperationFacetComponent } from './components/logbook-operation-facet/logbook-operation-facet.component';
import { VitamuiIntervalDatePickerComponent } from './components/vitamui-interval-date-picker/vitamui-interval-date-picker.component';
import { VitamuiMultiInputsModule } from './components/vitamui-multi-inputs/vitamui-multi-inputs.module';

import { ConfigService } from './config.service';

import { DragAndDropDirective } from './directives/drag-and-drop/drag-and-drop.directive';
import { ClickOutsideDirective } from './directives/click-outside/click-outside.directive';

import { ENVIRONMENT, SUBROGRATION_REFRESH_RATE_MS, WINDOW_LOCATION } from './injection-tokens';
import { LogbookModule } from './logbook/logbook.module';
import { LoggerModule } from './logger/logger.module';
import { ObjectEditorModule } from './object-editor/object-editor.module';
import { ObjectViewerModule } from './object-viewer/object-viewer.module';
import { PipesModule } from './pipes/pipes.module';

import { StartupService } from './startup.service';
import { SubrogationModule } from './subrogation/subrogation.module';
import { VitamUIHttpInterceptor } from './vitamui-http-interceptor';
import { BadgeComponent } from './components/badge/badge.component';
import { InformationBlocComponent } from './components/information-bloc/information-bloc.component';
import { InformationDetailComponent } from './components/information-detail/information-detail.component';
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
import { EventTypeLabelComponent } from './logbook/event-type-label/event-type-label.component';
import { HistoryEventsComponent } from './logbook/history/history-events/history-events.component';
import { TranslatePipe } from '@ngx-translate/core';

import { AutocompletePositionDirective } from './directives/autocomplete-position/autocomplete-position.directive';
import { ClosePopupDialogComponent } from './components/common-confirm-dialog/close-popup-dialog.component';
import { CollapseComponent } from './components/collapse/collapse.component';
import { CollapseContainerDirective } from './directives/collapse/collapse-container.directive';
import { CollapseDirective } from './directives/collapse/collapse.directive';
import { CollapseTriggerForDirective } from './directives/collapse/collapse-trigger-for.directive';
import { CommonConfirmDialogComponent } from './components/common-confirm-dialog/common-confirm-dialog.component';
import { CommonTooltipComponent } from './components/common-tooltip/common-tooltip.component';
import { DialogHeaderComponent } from '../../lib/components/dialog/dialog-header/dialog-header.component';
import { DownloadSnackBarComponent } from './components/download-snack-bar/download-snack-bar.component';
import { EditableButtonToggleComponent } from './components/editable-field/editable-toggle-group/editable-button-toggle.component';
import { EditableEmailInputComponent } from './components/editable-field/editable-email-input/editable-email-input.component';
import { EditableFieldComponent } from './components/editable-field/editable-field.component';
import { EditableFileComponent } from './components/editable-field/editable-file/editable-file.component';
import { EditableInputComponent } from './components/editable-field/editable-input/editable-input.component';
import { EditableLevelInputComponent } from './components/editable-field/editable-level-input/editable-level-input.component';
import { EditableTextareaComponent } from './components/editable-field/editable-textarea/editable-textarea.component';
import { EditableToggleGroupComponent } from './components/editable-field/editable-toggle-group/editable-toggle-group.component';
import { EllipsisDirective } from './directives/ellipsis/ellipsis.directive';
import { FilingPlanComponent } from '../../lib/components/filing-plan/filing-plan.component';
import { InfiniteScrollDirective } from './directives/infinite-scroll/infinite-scroll.directive';
import { LevelInputComponent } from './components/editable-field/level-input/level-input.component';
import { RoleComponent } from './components/role-toggle/role.component';
import { RoleToggleComponent } from './components/role-toggle/role-toggle.component';
import { RowCollapseContainerDirective } from './directives/row-collapse/row-collapse-container.directive';
import { RowCollapseDirective } from './directives/row-collapse/row-collapse.directive';
import { RowCollapseTriggerForDirective } from './directives/row-collapse/row-collapse-trigger-for.directive';
import { SelectLanguageComponent } from './components/header/select-language/select-language.component';
import { SubLevelPipe } from './components/editable-field/editable-level-input/sub-level.pipe';
import { TooltipDirective } from './components/common-tooltip/tooltip.directive';
import { UserPhotoComponent } from './components/header/user-photo/user-photo.component';
import { VitamuiSidenavHeaderComponent } from './components/vitamui-sidenav-header/vitamui-sidenav-header.component';
import { VitamUIRadioComponent } from '../../lib/components/vitamui-radio/vitamui-radio.component';
import { VitamUIRadioGroupComponent } from '../../lib/components/vitamui-radio-group/vitamui-radio-group.component';

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
  exports: [
    AccordionComponent,
    ApplicationCardComponent,
    ArchiveUnitModule,
    BadgeComponent,
    InformationBlocComponent,
    InformationDetailComponent,
    CdkStepperModule,
    ChipComponent,
    ElementsComponent,
    ClickOutsideDirective,
    CommonProgressBarComponent,
    DataComponent,
    DatepickerComponent,
    DragAndDropDirective,
    EventTypeLabelComponent,
    FileSelectorComponent,
    FooterComponent,
    HeaderModule,
    HistoryEventsComponent,
    InputComponent,
    LogbookModule,
    LogbookOperationFacetComponent,
    LoggerModule,
    ObjectEditorModule,
    ObjectViewerModule,
    OrderByButtonComponent,
    PipesModule,
    SearchBarComponent,
    SlideToggleComponent,
    StepperComponent,
    SubrogationModule,
    TableFilterComponent,
    TableFilterDirective,
    TableFilterOptionComponent,
    TableFilterSearchComponent,
    VitamUIFieldErrorComponent,
    SnackBarComponent,
    VitamuiBannerComponent,
    VitamuiBodyComponent,
    VitamuiDragDropFileComponent,
    VitamuiFacetComponent,
    VitamuiIntervalDatePickerComponent,
    VitamuiMenuButtonComponent,
    VitamuiMultiInputsModule,
    VitamuiSupHeaderComponent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiTreeNodeComponent,

    AutocompletePositionDirective,
    ClosePopupDialogComponent,
    CollapseComponent,
    CollapseContainerDirective,
    CollapseDirective,
    CollapseTriggerForDirective,
    CommonConfirmDialogComponent,
    CommonTooltipComponent,
    DialogHeaderComponent,
    DownloadSnackBarComponent,
    EditableButtonToggleComponent,
    EditableEmailInputComponent,
    EditableFieldComponent,
    EditableFileComponent,
    EditableInputComponent,
    EditableLevelInputComponent,
    EditableTextareaComponent,
    EditableToggleGroupComponent,
    EllipsisDirective,
    FilingPlanComponent,
    InfiniteScrollDirective,
    LevelInputComponent,
    RoleComponent,
    RoleToggleComponent,
    RowCollapseContainerDirective,
    RowCollapseDirective,
    RowCollapseTriggerForDirective,
    SelectLanguageComponent,
    SubLevelPipe,
    TooltipDirective,
    UserPhotoComponent,
    VitamuiSidenavHeaderComponent,
    VitamUIRadioComponent,
    VitamUIRadioGroupComponent,
  ],
  imports: [
    AccordionComponent,
    ApplicationCardComponent,
    ArchiveUnitModule,
    BadgeComponent,
    InformationBlocComponent,
    InformationDetailComponent,
    ChipComponent,
    ElementsComponent,
    ClickOutsideDirective,
    CommonModule,
    CommonProgressBarComponent,
    DataComponent,
    DatepickerComponent,
    DragAndDropDirective,
    EventTypeLabelComponent,
    FileSelectorComponent,
    FooterComponent,
    HeaderModule,
    HistoryEventsComponent,
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
    SearchBarComponent,
    SlideToggleComponent,
    StepperComponent,
    SubrogationModule,
    TableFilterComponent,
    TableFilterDirective,
    TableFilterOptionComponent,
    TableFilterSearchComponent,
    VitamUIFieldErrorComponent,
    SnackBarComponent,
    VitamuiBannerComponent,
    VitamuiBodyComponent,
    VitamuiDragDropFileComponent,
    VitamuiFacetComponent,
    VitamuiMenuButtonComponent,
    VitamuiMultiInputsModule,
    VitamuiSupHeaderComponent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiTreeNodeComponent,
    TranslatePipe,
    VitamuiIntervalDatePickerComponent,

    AutocompletePositionDirective,
    ClosePopupDialogComponent,
    CollapseComponent,
    CollapseContainerDirective,
    CollapseDirective,
    CollapseTriggerForDirective,
    CommonConfirmDialogComponent,
    CommonTooltipComponent,
    DialogHeaderComponent,
    DownloadSnackBarComponent,
    EditableButtonToggleComponent,
    EditableEmailInputComponent,
    EditableFieldComponent,
    EditableFileComponent,
    EditableInputComponent,
    EditableLevelInputComponent,
    EditableTextareaComponent,
    EditableToggleGroupComponent,
    EllipsisDirective,
    FilingPlanComponent,
    InfiniteScrollDirective,
    LevelInputComponent,
    RoleComponent,
    RoleToggleComponent,
    RowCollapseContainerDirective,
    RowCollapseDirective,
    RowCollapseTriggerForDirective,
    SelectLanguageComponent,
    SubLevelPipe,
    TooltipDirective,
    UserPhotoComponent,
    VitamuiSidenavHeaderComponent,
    VitamUIRadioComponent,
    VitamUIRadioGroupComponent,
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
        provideAppInitializer(async () => {
          const configService = inject(ConfigService);
          const environment = inject(ENVIRONMENT);
          const startupService = inject(StartupService);
          const authService = inject(AuthService);

          await loadConfigFactory(configService, environment)();
          return await startupServiceFactory(startupService, authService)();
        }),
        { provide: HTTP_INTERCEPTORS, useClass: VitamUIHttpInterceptor, multi: true },
      ],
    };
  }
}
