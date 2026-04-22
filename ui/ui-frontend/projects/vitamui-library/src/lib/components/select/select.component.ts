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
  computed,
  effect,
  forwardRef,
  HostListener,
  Injector,
  Input,
  QueryList,
  ResourceRef,
  Signal,
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
import { MatOption, MatOptionModule, MatOptionSelectionChange, MatPseudoCheckboxModule } from '@angular/material/core';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { PipesModule } from '../../../app/modules/pipes/pipes.module';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { normalizeString } from '../../utils/string.util';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

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
    MatPseudoCheckboxModule,
    MatSelectModule,
    PipesModule,
    ReactiveFormsModule,
    ScrollingModule,
    SearchBarComponent,
    TranslatePipe,
    MatProgressSpinner,
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

  private isResource(
    optionsParam: VitamuiSelectOptions | any[] | ResourceRef<VitamuiSelectOptions | any[]>,
  ): optionsParam is ResourceRef<VitamuiSelectOptions | any[]> {
    return !!(optionsParam as ResourceRef<VitamuiSelectOptions | any[]>)?.isLoading;
  }

  @Input({ required: true })
  set options(optionsParam: VitamuiSelectOptions | any[] | ResourceRef<VitamuiSelectOptions | any[]>) {
    if (this.isResource(optionsParam)) {
      this.optionsResource = optionsParam;
    } else {
      this.handleOptions(optionsParam);
    }
  }

  private handleOptions(optionsParam: VitamuiSelectOptions | any[]) {
    const options: VitamuiSelectOptions = this.normalizeSelection(optionsParam);

    this.allOptions = options?.options != null ? options.options : [];
    if (options?.customSorting != null) {
      this.customSorting = options.customSorting;
      this.allOptions.sort(this.customSorting);
    }
    this.displayedOptions = this.allOptions;
    this.resizeContainerHeightInSearchView();
    this.synchronizeSelectedOptions();
    this.resizeContainerHeightInSelectedItemsView();

    // Force display update after options are loaded
    if (this.control?.value && this.allOptions.length > 0) {
      Promise.resolve().then(() => {
        this.updateMatSelectTriggerContent();
        this.cd.detectChanges();
      });
    }
    this.addEventListeners();
  }

  private optionsResource: ResourceRef<VitamuiSelectOptions | any[]>;

  @Input() selectAllLabel = this.translateService.instant('SELECT.SELECT_ALL');
  @Input() allSelectedLabel?: string;
  loading: Signal<boolean> = computed(() => (this.optionsResource ? this.optionsResource.isLoading() : false));

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
  private isUpdatingTrigger = false; // Prevent infinite recursion

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

    effect(() => {
      if (this.optionsResource) {
        if (this.optionsResource.isLoading()) {
          this.handleOptions([]);
        } else {
          this.handleOptions(this.optionsResource.value());
        }
      }
    });
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
        this.updateSelectAll();
        this.syncRenderedOptionsSelection();
      });

    this.optionKeys.changes.subscribe(() => this.syncRenderedOptionsSelection());

    this.addEventListeners();

    // Force value display after complete initialization
    if (this.control?.value) {
      Promise.resolve().then(() => {
        this.synchronizeSelectedOptions();
        this.updateMatSelectTriggerContent();
        this.syncRenderedOptionsSelection();
        this.cd.detectChanges();
      });
    }
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
    } else if (this.allOptions.length > 0) {
      this.selectedOptions = this.allOptions.filter((option) => this.preselectedOptionKeys.includes(option.key));
    }

    this.resizeContainerHeightInSelectedItemsView();
    this.updateMatSelectTriggerContent();
    this.cd.markForCheck();
  }

  protected openedChange(opened: boolean): void {
    if (opened) {
      setTimeout(() => {
        if (!this.loading()) {
          this.ensureValueDisplay();
          this.viewport?.checkViewportSize();

          if (this.selectedOptions.length > 0) {
            this.scrollToSelectedOption();
          }
        }
      });
    }

    if (opened && this.enableSearch) {
      this.searchBar?.onFocus();
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
  protected toggleSelectAll(event: MouseEvent): void {
    event.preventDefault();
    event.stopPropagation();

    // Toggle: if all are selected, deselect all. Otherwise, select all.
    this.selectAll(!this.isAllSelected());
  }

  protected isAllSelected(): boolean {
    return this.allOptions.length > 0 && this.getSelectedOptionsCount() === this.allOptions.length;
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
        const optionToAdd = this.allOptions.find((selectedOption) => selectedOption.key === value);
        if (optionToAdd) {
          this.selectedOptions.push(optionToAdd);
        }
      }
    } else {
      this.selectedOptions = uncheckingOption ? [] : this.allOptions.filter((selectedOption) => selectedOption.key === value);
    }

    this.resizeContainerHeightInSelectedItemsView();

    if (this.selectedOptions.length === 0) {
      this.clearAllSelectedOptions();
      this.updateMatSelectTriggerContent(this._multiple ? [] : undefined);
    } else {
      const selectedKeys = [...this.selectedOptions.map((option) => option.key)].sort();
      const valueToPropagate = this._multiple ? selectedKeys : selectedKeys[0];
      this.onChange(valueToPropagate);
      this.updateMatSelectTriggerContent(valueToPropagate);
    }
    this.syncRenderedOptionsSelection();
  }

  public readonly compareOptions = (o1: any, o2: any): boolean => {
    if (o1 == null || o2 == null) {
      return o1 === o2;
    }

    if (this.multiple && Array.isArray(o1) && Array.isArray(o2)) {
      if (o1.length !== o2.length) return false;
      return o1.every((val) => o2.includes(val));
    }

    const val1 = typeof o1 === 'object' ? o1.key : o1;
    const val2 = typeof o2 === 'object' ? o2.key : o2;

    return String(val1) === String(val2);
  };

  private normalizeSelection(optionsParam: VitamuiSelectOptions | any[]): VitamuiSelectOptions {
    if (optionsParam instanceof Array) {
      if (optionsParam.length === 0) {
        return { options: [] };
      }

      const firstItem = optionsParam[0];
      if (firstItem?.key != null && firstItem?.label != null) {
        return { options: optionsParam };
      } else {
        return {
          options: optionsParam.map((option) => ({
            key: option,
            label: option.toString(),
          })),
        };
      }
    }
    return optionsParam;
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
    } else {
      this.clearAllSelectedOptions();
    }
    this.resizeContainerHeightInSelectedItemsView();
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

  private updateMatSelectTriggerContent(valueOverride?: any): void {
    if (!this.matSelect || this.isUpdatingTrigger) return;

    Object.defineProperties(this.matSelect, {
      empty: {
        value: this.selectedOptions.length <= 0,
        writable: true,
      },
    });

    const valueToDisplay = valueOverride ?? this.control?.value;

    if (valueToDisplay != null) {
      this.isUpdatingTrigger = true;

      try {
        this.matSelect.value = valueToDisplay;
        this.matSelect._onChange(valueToDisplay);
        this.matSelect.stateChanges.next();
      } finally {
        this.isUpdatingTrigger = false;
      }
    }
  }

  focus() {
    this.matSelect.focus();
  }

  private scrollToSelectedOption(): void {
    if (!this.viewport || this.selectedOptions.length === 0) return;

    const firstSelectedOption = this.selectedOptions[0];
    const index = this.displayedOptions.findIndex((opt) => opt.key === firstSelectedOption.key);

    if (index >= 0) {
      setTimeout(() => {
        this.viewport.scrollToIndex(index, 'smooth');
      }, 100);
    }
  }

  private synchronizeSelectedOptions(): void {
    const keysToSelect = this.preselectedOptionKeys?.length
      ? this.preselectedOptionKeys
      : this.control?.value
        ? Array.isArray(this.control.value)
          ? this.control.value
          : [this.control.value]
        : [];

    if (keysToSelect.length > 0 && this.allOptions.length > 0) {
      this.selectedOptions = this.allOptions.filter((option) => keysToSelect.includes(option.key));
      this.updateMatSelectTriggerContent();
    }
  }

  private ensureValueDisplay(): void {
    if (!this.matSelect || !this.control?.value) return;

    setTimeout(() => {
      this.updateMatSelectTriggerContent();
      if (this.multiple && Array.isArray(this.control.value)) {
        this.selectedOptions = this.allOptions.filter((option) => this.control.value.includes(option.key));
      }
      this.syncRenderedOptionsSelection();
      this.cd.detectChanges();
    });
  }

  private syncRenderedOptionsSelection(): void {
    if (!this.optionKeys) return;

    const selectedKeys = new Set(this.selectedOptions.map((option) => String(option.key)));
    this.optionKeys.forEach((optionKey) => {
      if (optionKey.value === this.SELECT_ALL_OPTIONS) return;

      const shouldBeSelected = selectedKeys.has(String(optionKey.value));
      if (shouldBeSelected && !optionKey.selected) {
        optionKey.select(false);
      }
      if (!shouldBeSelected && optionKey.selected) {
        optionKey.deselect(false);
      }
    });
  }

  protected readonly Validators = Validators;
  protected readonly String = String;
}
