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
import { CdkVirtualScrollViewport, ScrollDispatcher, ScrollingModule } from '@angular/cdk/scrolling';
import {
  AfterViewChecked,
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  forwardRef,
  HostListener,
  Injector,
  Input,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { FormControl, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule, Validators } from '@angular/forms';
import { filter } from 'rxjs/operators';
import { Option, SearchBarComponent } from '../../../app/modules';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';
import { AutocompletePositionDirectiveModule } from '../../../app/modules/directives/autocomplete-position/autocomplete-position.directive.module';
import { CommonModule } from '@angular/common';
import { CommonTooltipModule } from '../../../app/modules/components/common-tooltip/common-tooltip.module';
import { EllipsisDirectiveModule } from '../../../app/modules/directives/ellipsis/ellipsis.directive.module';
import { FormErrorsComponent } from '../form-errors/form-errors.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatOption, MatOptionModule, MatOptionSelectionChange } from '@angular/material/core';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { PipesModule } from '../../../app/modules/pipes/pipes.module';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { normalizeString } from '../../utils/string.util';

export const VITAMUI_SELECT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => SelectComponent),
  multi: true,
};

export interface VitamuiSelectOptions {
  options: Option[];
  customSorting?: (a: Option, b: Option) => number;
}

@Component({
  selector: 'vitamui-select',
  templateUrl: './select.component.html',
  styleUrls: ['./select.component.scss'],
  imports: [
    AutocompletePositionDirectiveModule,
    CommonModule,
    CommonTooltipModule,
    EllipsisDirectiveModule,
    FormErrorsComponent,
    FormsModule,
    MatAutocompleteModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatInputModule,
    MatListModule,
    MatOptionModule,
    MatSelectModule,
    PipesModule,
    ReactiveFormsModule,
    ScrollingModule,
    SearchBarComponent,
    TranslatePipe,
  ],
  providers: [
    VITAMUI_SELECT_VALUE_ACCESSOR,
    {
      provide: AbstractFormInputDirective,
      useExisting: forwardRef(() => SelectComponent),
    }, // This provider is required in order for the FormFieldValueWrapperComponent to be able to find a reference to that component
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelectComponent extends AbstractFormInputDirective implements AfterViewInit, AfterViewChecked {
  @Input() placeholder: string;
  @Input() searchBarPlaceHolder: string;

  @Input() set multiple(multiple: boolean) {
    this._multiple = multiple;
    if (this._multiple) {
      if (this.enableSelectAll === undefined) this.enableSelectAll = true;
      if (this.enableDisplaySelected === undefined) this.enableDisplaySelected = true;
    }
  }

  get multiple(): boolean {
    return this._multiple;
  }

  @Input() set enableSelectAll(enableSelectAll: boolean) {
    this._enableSelectAll = enableSelectAll;
    if (!this._enableSelectAll) {
      this.visibleItemsInSearchView = 5;
      this.resizeContainerHeightInSearchView();
    }
  }

  get enableSelectAll(): boolean {
    return this._enableSelectAll;
  }

  @Input() set enableSearch(enableSearch: boolean) {
    this._enableSearch = enableSearch;
    if (!this._enableSearch) {
      this.resizeContainerHeightInSearchView();
    }
  }

  get enableSearch(): boolean {
    return this._enableSearch;
  }

  @Input() set enableDisplaySelected(enableDisplaySelected: boolean) {
    this._enableDisplaySelected = enableDisplaySelected;
    if (!this._enableDisplaySelected) {
      this.resizeContainerHeightInSearchView();
    }
  }

  get enableDisplaySelected(): boolean {
    return this._enableDisplaySelected;
  }

  @Input({ required: true })
  set options(optionsParam: VitamuiSelectOptions | any[]) {
    const options: VitamuiSelectOptions =
      optionsParam instanceof Array
        ? optionsParam[0]?.key != null && optionsParam[0]?.label != null
          ? { options: optionsParam }
          : {
              options: optionsParam.map((option) => ({
                key: option,
                label: option.toString(),
              })),
            }
        : optionsParam;
    this.allOptions = options?.options != null ? options.options : [];
    if (options?.customSorting != null) {
      this.customSorting = options.customSorting;
      this.allOptions.sort(this.customSorting);
    }
    this.displayedOptions = this.allOptions;
    this.resizeContainerHeightInSearchView();
    if (!this.selectedOptions.length)
      this.selectedOptions = this.allOptions.filter((option) => this.preselectedOptionKeys?.includes(option.key));
    if (this.control) this.control.setValue(this.control.value); // We force-update the control value after updating the options to make sure the mat-select updates the displayed value
    this.resizeContainerHeightInSelectedItemsView();
    this.addEventListeners();
  }

  @Input() selectAllLabel = this.translateService.instant('SELECT.SELECT_ALL');
  @Input() allSelectedLabel?: string;

  public displayedOptions: Option[] = [];

  protected nbSelectedItemsMap: { [k: string]: string } = {
    '=1': 'SELECT.SELECTED_ELEMENT.SINGULAR',
    other: 'SELECT.SELECTED_ELEMENT.PLURAL',
  };
  protected searchTextControl = new FormControl();
  protected showOnlySelectedOption = false;
  protected selectedOptions: Option[] = [];
  protected containerHeightInSearchView = '0px';
  protected containerHeightInSelectedItemsView = '0px';
  protected readonly SELECT_ALL_OPTIONS = 'SELECT_ALL_OPTIONS';
  protected allOptions: Option[] = [];

  private visibleItemsInSearchView = 5;
  private preselectedOptionKeys: string[] = [];
  private customSorting: (a: Option, b: Option) => number;
  private _multiple = false;
  private _enableSearch = true;
  private _enableSelectAll?: boolean;
  private _enableDisplaySelected?: boolean;

  @ViewChild('searchBar') searchBar: SearchBarComponent;
  @ViewChild('scrollViewport') private cdkVirtualScrollViewport: CdkVirtualScrollViewport;
  @ViewChildren(MatOption) optionKeys: QueryList<MatOption>;
  @ViewChild('matSelect') matSelect: MatSelect;
  @ViewChild(CdkVirtualScrollViewport) viewport: CdkVirtualScrollViewport;

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      const focusedOptionIndex = this.optionKeys.toArray().find((option) => option.active === true);
      if (focusedOptionIndex) {
        this.cdkVirtualScrollViewport.scrollToIndex(+focusedOptionIndex.id);
      }
    }
  }

  constructor(
    injector: Injector,
    private cd: ChangeDetectorRef,
    readonly sd: ScrollDispatcher,
    private translateService: TranslateService,
  ) {
    super(injector);
  }

  ngAfterViewChecked(): void {
    this.updateSelectAll();
  }

  ngAfterViewInit(): void {
    this.updateSelectedOptionsFromValue(this.control.value);
    this.overrideControlMethods();

    this.sd
      .scrolled()
      .pipe(filter((scrollable) => this.cdkVirtualScrollViewport === scrollable))
      .subscribe(() => {
        this.updateCheckboxes();
        this.updateSelectAll();
      });

    this.addEventListeners();
  }

  private addEventListeners() {
    const eventListener = this.onKeydown.bind(this);

    const searchInputElement: HTMLInputElement | undefined = this.searchBar?.searchInput?.nativeElement;
    const matSelectElement: HTMLInputElement | undefined = this.matSelect?._elementRef?.nativeElement;

    searchInputElement?.removeEventListener('keydown', eventListener);
    matSelectElement?.removeEventListener('keydown', eventListener);

    searchInputElement?.addEventListener('keydown', eventListener, { capture: true });
    matSelectElement?.addEventListener('keydown', eventListener, { capture: true });
  }

  writeValue(preselectedOptionKeys: string | string[]) {
    this.preselectedOptionKeys = preselectedOptionKeys
      ? Array.isArray(preselectedOptionKeys)
        ? preselectedOptionKeys.sort()
        : [preselectedOptionKeys]
      : null;
    // When the component is reset this method is called with selectedOptionKeys = null
    if (this.preselectedOptionKeys == null) {
      this.selectedOptions = [];
    } else {
      this.selectedOptions = this.allOptions.filter((option) => this.preselectedOptionKeys.includes(option.key));
    }
    this.updateCheckboxes();
    this.updateSelectAll();

    this.resizeContainerHeightInSelectedItemsView();
  }

  protected openedChange(opened: boolean): void {
    // Attend que overlay du select soit rendu
    setTimeout(() => {
      this.viewport.checkViewportSize();
    });

    if (opened && this.enableSearch) {
      this.searchBar.onFocus();
    }
  }

  protected getSelectedOptionsCount(): number {
    return this.selectedOptions.length;
  }

  protected toggleShowOnlySelectedOption(): void {
    this.showOnlySelectedOption = !this.showOnlySelectedOption;
    if (!this.showOnlySelectedOption) {
      this.displayedOptions = this.allOptions;
      this.resizeContainerHeightInSearchView();
      this.searchTextControl.reset();
    } else {
      this.searchBar?.reset();
    }
  }

  protected toggleSelectAll(event: MatOptionSelectionChange): void {
    if (!event.isUserInput) {
      return;
    }

    this.selectAll(event.source.selected);
  }

  protected clearAllSelectedOptions(): void {
    this.showOnlySelectedOption = false;
    this.control.reset();
    this.onChange(this._multiple ? [] : undefined);
  }

  protected onSearch(value: string): void {
    this.searchTextControl.setValue(value ? value : null);
    if (this.searchTextControl.value) {
      this.displayedOptions = this.allOptions.filter(
        (option) => normalizeString(option.label).indexOf(normalizeString(this.searchTextControl.value)) !== -1,
      );
      this.resizeContainerHeightInSearchView();
    } else {
      this.resetSearchBar();
    }
  }

  protected onSelectClosed(): void {
    this.onTouched();
    this.showOnlySelectedOption = false;
    if (this.searchBar && this.enableSearch) {
      this.searchBar.reset();
    }
    this.searchTextControl.reset();
  }

  protected resetSearchBar(): void {
    this.searchTextControl.reset();
    this.displayedOptions = this.allOptions;
    this.resizeContainerHeightInSearchView();
    this.searchBar?.onFocus();
  }

  protected onSelectionChange(change: MatOptionSelectionChange) {
    if (!change.isUserInput) {
      return;
    }

    const value = change.source.value;
    const uncheckingOption = this.selectedOptions.some((selectedOption) => selectedOption.key === value);

    if (this._multiple) {
      if (uncheckingOption) {
        this.selectedOptions = this.selectedOptions.filter((selectedOption) => selectedOption.key !== value);
      } else {
        this.selectedOptions.push(this.allOptions.filter((selectedOption) => selectedOption.key === value)[0]);
      }
    } else {
      this.selectedOptions = uncheckingOption ? [] : this.allOptions.filter((selectedOption) => selectedOption.key === value);
    }

    this.resizeContainerHeightInSelectedItemsView();

    if (this.selectedOptions.length === 0) {
      this.clearAllSelectedOptions();
    } else {
      const selectedKeys = [...this.selectedOptions.map((option) => option.key)].sort();
      this.onChange(this._multiple ? selectedKeys : selectedKeys[0]);
    }

    this.updateMatSelectTriggerContent();
    this.normalizeSelection();
  }

  protected compareOptions(o1: { key: string } | null, o2: { key: string } | null): boolean {
    return !!o1 && !!o2 ? o1.key === o2.key : o1 === o2;
  }

  private normalizeSelection() {
    if (this.multiple && Array.isArray(this.control.value)) {
      const set = new Set(this.control.value);
      const normalized = this.allOptions.map((o) => o.key).filter((k) => set.has(k));
      this.control.setValue(normalized, { emitEvent: false });
    }
  }

  private overrideControlMethods() {
    const previousSetValue = this.control.setValue;
    this.control.setValue = (value: any, options?: any) => {
      const filteredValue = value instanceof Array ? value.filter((v) => v !== this.SELECT_ALL_OPTIONS) : value;
      previousSetValue.bind(this.control)(filteredValue, { ...options, emitModelToViewChange: true });
    };

    const previousReset = this.control.reset;
    this.control.reset = (value?: any, options?: any) => {
      this.updateSelectedOptionsFromValue(value);
      previousReset.bind(this.control)(value, options);
      this.matSelect._onBlur(); // Required to prevent the label to keep floating when resetting the value
    };
  }

  private updateSelectedOptionsFromValue(value: any) {
    (this.control as any).resetValue = value;
    this.selectedOptions = this._multiple
      ? this.allOptions.filter((option) => (value || []).includes(option.key))
      : this.allOptions.filter((option) => option.key === value);
  }

  private updateSelectAll(): void {
    if (
      !this.showOnlySelectedOption &&
      this.optionKeys &&
      this.optionKeys.filter((optionKey) => optionKey.value === this.SELECT_ALL_OPTIONS).length !== 0
    ) {
      const selectedOptionsCount = this.getSelectedOptionsCount();
      if (selectedOptionsCount === this.allOptions.length) {
        this.optionKeys.find((optionKey) => optionKey.value === this.SELECT_ALL_OPTIONS).select(false);
      } else {
        this.optionKeys.find((optionKey) => optionKey.value === this.SELECT_ALL_OPTIONS).deselect(false);
      }
      this.cd.detectChanges();
    }
  }

  private selectAll(value: boolean): void {
    if (value) {
      this.selectedOptions = [...this.allOptions];
      const selectedKeys = [...this.selectedOptions.map((option) => option.key)].sort();
      this.onChange(selectedKeys);
      this.updateCheckboxes();
    } else {
      this.clearAllSelectedOptions();
    }
    this.resizeContainerHeightInSelectedItemsView();
    this.normalizeSelection();
  }

  private resizeContainerHeightInSearchView(): void {
    this.containerHeightInSearchView = this.calculateContainerHeight(
      this.displayedOptions.length + (this._multiple && this.enableSelectAll ? 1 : 0),
      this.visibleItemsInSearchView,
    );
    this.checkViewportSize();
  }

  private resizeContainerHeightInSelectedItemsView(): void {
    this.containerHeightInSelectedItemsView = this.calculateContainerHeight(this.selectedOptions.length, 5);
    this.checkViewportSize();
  }

  private calculateContainerHeight(optionLength: number, visibleItems: number): string {
    const itemHeight = 49;

    return `${itemHeight * (optionLength <= visibleItems ? optionLength : visibleItems) - 1}px`;
  }

  private checkViewportSize(): void {
    if (this.cdkVirtualScrollViewport) {
      this.cdkVirtualScrollViewport.checkViewportSize();
    }
  }

  private onKeydown(event: KeyboardEvent) {
    if (event.key === 'a' && event.ctrlKey) {
      // Prevent mat-select to select/deselect everything with CTRL+A shortcut
      event.stopImmediatePropagation();
    }
    const focusInSearchInput = [this.searchBar?.searchInput?.nativeElement].includes(event.target);
    if (focusInSearchInput) {
      if (['ArrowDown'].includes(event.code)) {
        // Get out of searchInput if arrow down
        this.matSelect._elementRef.nativeElement.focus();
      }
      if (['Enter'].includes(event.code)) {
        // Trigger search
        this.onSearch(this.searchBar.searchValue);
      }
      if (!['ArrowDown', 'ArrowUp', 'Escape'].includes(event.code)) {
        // Prevent most keyboard keypress to be interpreted by the mat-select, otherwise it would "search" in options or open/close toggles
        event.stopPropagation();
      }
    }
  }

  private updateCheckboxes(): void {
    if (this.optionKeys == null) {
      return;
    }

    let needUpdate = false;

    this.optionKeys.forEach((optionKey) => {
      const selected = this.selectedOptions.filter((selectedOption) => selectedOption.key === optionKey.value);

      if (selected.length > 0 && !optionKey.selected) {
        optionKey.select(false);
        needUpdate = true;
      } else if (selected.length === 0 && optionKey.selected) {
        optionKey.deselect(false);
        needUpdate = true;
      }
    });

    if (needUpdate) {
      this.cd.detectChanges();
    }

    this.updateMatSelectTriggerContent();
  }

  private updateMatSelectTriggerContent(): void {
    if (this.matSelect)
      Object.defineProperties(this.matSelect, {
        empty: {
          value: this.selectedOptions.length <= 0,
          writable: true,
        },
      });
  }

  focus() {
    this.matSelect.focus();
  }

  protected readonly Validators = Validators;
  protected readonly String = String;
}
