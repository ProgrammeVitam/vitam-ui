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
  DestroyRef,
  effect,
  forwardRef,
  HostListener,
  inject,
  Injector,
  Resource,
  Signal,
  input,
  signal,
  SimpleChanges,
  viewChild,
  viewChildren,
} from '@angular/core';
import { FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule, Validators } from '@angular/forms';
import { filter } from 'rxjs/operators';
import { Option } from '../../../app/modules/components/autocomplete/utils/option.interface';
import { SearchBarComponent } from '../../../app/modules/components/search-bar/search-bar.component';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';

import { CommonModule } from '@angular/common';

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
import { TranslatePipe } from '@ngx-translate/core';
import { normalizeString } from '../../utils/string.util';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TooltipDirective } from '../../../app/modules/components/common-tooltip/tooltip.directive';
import { EllipsisDirective } from '../../../app/modules/directives/ellipsis/ellipsis.directive';
import { queryChanges } from '../../../app/modules/utils/query-changes.util';

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
    CommonModule,
    TooltipDirective,
    EllipsisDirective,
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
  private readonly cd = inject(ChangeDetectorRef);
  private destroyed = false;
  readonly sd = inject(ScrollDispatcher);

  readonly placeholder = input<string>(undefined);
  readonly searchBarPlaceHolder = input<string>(undefined);

  readonly multiple = input(false);

  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly enableSelectAllInput = input<boolean>(undefined, { alias: 'enableSelectAll' });
  readonly enableSelectAll = signal<boolean>(undefined);

  readonly enableSearch = input(true);

  // eslint-disable-next-line @angular-eslint/no-input-rename
  readonly enableDisplaySelectedInput = input<boolean>(undefined, { alias: 'enableDisplaySelected' });
  readonly enableDisplaySelected = signal<boolean>(undefined);

  private isResource(
    optionsParam: VitamuiSelectOptions | any[] | Resource<VitamuiSelectOptions | any[]>,
  ): optionsParam is Resource<VitamuiSelectOptions | any[]> {
    return !!(optionsParam as Resource<VitamuiSelectOptions | any[]>)?.isLoading;
  }

  readonly options = input.required<VitamuiSelectOptions | any[] | Resource<VitamuiSelectOptions | any[]>>();

  override ngOnChanges(changes: SimpleChanges): void {
    // Handle the inputs in the order they were set, as the former input setters did
    for (const inputName of Object.keys(changes)) {
      switch (inputName) {
        case 'multiple':
          this.onMultipleChange();
          break;
        case 'enableSelectAllInput':
          this.onEnableSelectAllChange();
          break;
        case 'enableSearch':
          if (!this.enableSearch()) {
            this.resizeContainerHeightInSearchView();
          }
          break;
        case 'enableDisplaySelectedInput':
          this.onEnableDisplaySelectedChange();
          break;
        case 'options':
          this.onOptionsChange();
          break;
      }
    }
    super.ngOnChanges(changes);
  }

  private onMultipleChange() {
    if (!this.multiple()) return;
    if (this.enableSelectAll() === undefined) this.enableSelectAll.set(true);
    if (this.enableDisplaySelected() === undefined) this.enableDisplaySelected.set(true);
  }

  private onEnableSelectAllChange() {
    this.enableSelectAll.set(this.enableSelectAllInput());
    if (!this.enableSelectAll()) {
      this.visibleItemsInSearchView = 5;
      this.resizeContainerHeightInSearchView();
    }
  }

  private onEnableDisplaySelectedChange() {
    this.enableDisplaySelected.set(this.enableDisplaySelectedInput());
    if (!this.enableDisplaySelected()) {
      this.resizeContainerHeightInSearchView();
    }
  }

  private onOptionsChange() {
    const optionsParam = this.options();
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
        if (this.destroyed) return;
        this.updateMatSelectTriggerContent();
        this.cd.detectChanges();
      });
    }
    this.addEventListeners();
  }

  private optionsResource: Resource<VitamuiSelectOptions | any[]>;

  readonly selectAllLabel = input('SELECT.SELECT_ALL');
  readonly allSelectedLabel = input<string>(undefined);
  loading: Signal<boolean> = computed(() => (this.optionsResource ? this.optionsResource.isLoading() : false));

  public displayedOptions: Option[] = [];

  protected nbSelectedItemsMap: { [k: string]: string } = {
    '=1': 'SELECT.SELECTED_ELEMENT.SINGULAR',
    other: 'SELECT.SELECTED_ELEMENT.PLURAL',
  };
  // Text searched in the options
  protected readonly searchText = signal<string | null>(null);
  protected showOnlySelectedOption = false;
  protected selectedOptions: Option[] = [];
  protected containerHeightInSearchView = '0px';
  protected containerHeightInSelectedItemsView = '0px';
  protected readonly SELECT_ALL_OPTIONS = 'SELECT_ALL_OPTIONS';
  protected allOptions: Option[] = [];

  private visibleItemsInSearchView = 5;
  private preselectedOptionKeys: string[] = [];
  private customSorting: (a: Option, b: Option) => number;
  private isUpdatingTrigger = false; // Prevent infinite recursion

  readonly searchBar = viewChild<SearchBarComponent>('searchBar');
  private readonly cdkVirtualScrollViewport = viewChild<CdkVirtualScrollViewport>('scrollViewport');
  readonly optionKeys = viewChildren(MatOption);
  private readonly optionKeysChanges = queryChanges(this.optionKeys);
  readonly matSelect = viewChild<MatSelect>('matSelect');
  readonly viewport = viewChild(CdkVirtualScrollViewport);

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      const focusedOptionIndex = this.optionKeys().find((option) => option.active === true);
      if (focusedOptionIndex) {
        this.cdkVirtualScrollViewport().scrollToIndex(+focusedOptionIndex.id);
      }
    }
  }

  constructor() {
    const injector = inject(Injector);

    super(injector);
    inject(DestroyRef).onDestroy(() => (this.destroyed = true));

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
      .pipe(filter((scrollable) => this.cdkVirtualScrollViewport() === scrollable))
      .subscribe(() => {
        this.updateSelectAll();
        this.syncRenderedOptionsSelection();
      });

    this.optionKeysChanges.subscribe(() => this.syncRenderedOptionsSelection());

    this.addEventListeners();

    // Force value display after complete initialization
    if (this.control?.value) {
      Promise.resolve().then(() => {
        if (this.destroyed) return;
        this.synchronizeSelectedOptions();
        this.updateMatSelectTriggerContent();
        this.syncRenderedOptionsSelection();
        this.cd.detectChanges();
      });
    }
  }

  private addEventListeners() {
    const eventListener = this.onKeydown.bind(this);

    const searchInputElement: HTMLInputElement | undefined = this.searchBar()?.searchInput()?.nativeElement;
    const matSelectElement: HTMLInputElement | undefined = this.matSelect()?._elementRef?.nativeElement;

    searchInputElement?.removeEventListener('keydown', eventListener);
    matSelectElement?.removeEventListener('keydown', eventListener);

    searchInputElement?.addEventListener('keydown', eventListener, { capture: true });
    matSelectElement?.addEventListener('keydown', eventListener, { capture: true });
  }

  override writeValue(preselectedOptionKeys: string | string[]) {
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
          this.viewport()?.checkViewportSize();

          if (this.selectedOptions.length > 0) {
            this.scrollToSelectedOption();
          }
        }
      });
    }

    if (opened && this.enableSearch()) {
      this.searchBar()?.onFocus();
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
      this.searchText.set(null);
    } else {
      this.searchBar()?.reset();
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
    this.onChange(this.multiple() ? [] : undefined);
  }

  protected onSearch(value: string): void {
    this.searchText.set(value ? value : null);
    if (this.searchText()) {
      this.displayedOptions = this.allOptions.filter(
        (option) => normalizeString(option.label).indexOf(normalizeString(this.searchText())) !== -1,
      );
      this.resizeContainerHeightInSearchView();
    } else {
      this.resetSearchBar();
    }
  }

  protected onSelectClosed(): void {
    this.onTouched();
    this.showOnlySelectedOption = false;
    const searchBar = this.searchBar();
    if (searchBar && this.enableSearch()) {
      searchBar.reset();
    }
    this.searchText.set(null);
  }

  protected resetSearchBar(): void {
    this.searchText.set(null);
    this.displayedOptions = this.allOptions;
    this.resizeContainerHeightInSearchView();
    this.searchBar()?.onFocus();
  }

  protected onSelectionChange(change: MatOptionSelectionChange) {
    if (!change.isUserInput) {
      return;
    }

    const value = change.source.value;
    const uncheckingOption = this.selectedOptions.some((selectedOption) => selectedOption.key === value);

    if (this.multiple()) {
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
      this.updateMatSelectTriggerContent(this.multiple() ? [] : undefined);
    } else {
      const selectedKeys = [...this.selectedOptions.map((option) => option.key)].sort();
      const valueToPropagate = this.multiple() ? selectedKeys : selectedKeys[0];
      this.onChange(valueToPropagate);
      this.updateMatSelectTriggerContent(valueToPropagate);
    }
    this.syncRenderedOptionsSelection();
  }

  public readonly compareOptions = (o1: any, o2: any): boolean => {
    if (o1 == null || o2 == null) {
      return o1 === o2;
    }

    if (this.multiple() && Array.isArray(o1) && Array.isArray(o2)) {
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
      this.matSelect()._onBlur(); // Required to prevent the label to keep floating when resetting the value
    };
  }

  private updateSelectedOptionsFromValue(value: any) {
    (this.control as any).resetValue = value;
    this.selectedOptions = this.multiple()
      ? this.allOptions.filter((option) => (value || []).includes(option.key))
      : this.allOptions.filter((option) => option.key === value);
  }

  private updateSelectAll(): void {
    const optionKeys = this.optionKeys();
    if (!this.showOnlySelectedOption && optionKeys && optionKeys.some((optionKey) => optionKey.value === this.SELECT_ALL_OPTIONS)) {
      const selectedOptionsCount = this.getSelectedOptionsCount();
      if (selectedOptionsCount === this.allOptions.length) {
        optionKeys.find((optionKey) => optionKey.value === this.SELECT_ALL_OPTIONS).select(false);
      } else {
        optionKeys.find((optionKey) => optionKey.value === this.SELECT_ALL_OPTIONS).deselect(false);
      }
      this.cd.detectChanges();
    }
  }

  private selectAll(value: boolean): void {
    if (value) {
      this.selectedOptions = [...this.allOptions];

      const selectedKeys = this.selectedOptions.map((o) => o.key).sort();
      this.onChange(selectedKeys);

      // visual synchronization
      this.optionKeys()?.forEach((option) => {
        if (option.value !== this.SELECT_ALL_OPTIONS) {
          option.select(false);
        }
      });
    } else {
      this.clearAllSelectedOptions();

      this.optionKeys()?.forEach((option) => {
        option.deselect(false);
      });
    }

    this.resizeContainerHeightInSelectedItemsView();
    this.cd.detectChanges();
  }

  private resizeContainerHeightInSearchView(): void {
    this.containerHeightInSearchView = this.calculateContainerHeight(
      this.displayedOptions.length + (this.multiple() && this.enableSelectAll() ? 1 : 0),
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
    const cdkVirtualScrollViewport = this.cdkVirtualScrollViewport();
    if (cdkVirtualScrollViewport) {
      cdkVirtualScrollViewport.checkViewportSize();
    }
  }

  private onKeydown(event: KeyboardEvent) {
    if (event.key === 'a' && event.ctrlKey) {
      // Prevent mat-select to select/deselect everything with CTRL+A shortcut
      event.stopImmediatePropagation();
    }
    const searchBar = this.searchBar();
    const focusInSearchInput = [searchBar?.searchInput()?.nativeElement].includes(event.target);
    if (focusInSearchInput) {
      if (['ArrowDown'].includes(event.code)) {
        // Get out of searchInput if arrow down
        this.matSelect()._elementRef.nativeElement.focus();
      }
      if (['Enter'].includes(event.code)) {
        // Trigger search (native listener: refresh the view, zoneless)
        this.onSearch(searchBar.searchValue());
        this.cd.markForCheck();
      }
      if (!['ArrowDown', 'ArrowUp', 'Escape'].includes(event.code)) {
        // Prevent most keyboard keypress to be interpreted by the mat-select, otherwise it would "search" in options or open/close toggles
        event.stopPropagation();
      }
    }
  }

  private updateMatSelectTriggerContent(valueOverride?: any): void {
    const matSelect = this.matSelect();
    if (!matSelect || this.isUpdatingTrigger) return;

    Object.defineProperties(matSelect, {
      empty: {
        value: this.selectedOptions.length <= 0,
        writable: true,
      },
    });

    const valueToDisplay = valueOverride ?? this.control?.value;

    if (valueToDisplay != null) {
      this.isUpdatingTrigger = true;

      try {
        matSelect.value = valueToDisplay;
        matSelect._onChange(valueToDisplay);
        matSelect.stateChanges.next();
      } finally {
        this.isUpdatingTrigger = false;
      }
    }
  }

  focus() {
    this.matSelect().focus();
  }

  private scrollToSelectedOption(): void {
    if (!this.viewport() || this.selectedOptions.length === 0) return;

    const firstSelectedOption = this.selectedOptions[0];
    const index = this.displayedOptions.findIndex((opt) => opt.key === firstSelectedOption.key);

    if (index >= 0) {
      setTimeout(() => {
        this.viewport().scrollToIndex(index, 'smooth');
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
    if (!this.matSelect() || !this.control?.value) return;

    setTimeout(() => {
      if (this.destroyed) return;
      this.updateMatSelectTriggerContent();
      if (this.multiple() && Array.isArray(this.control.value)) {
        this.selectedOptions = this.allOptions.filter((option) => this.control.value.includes(option.key));
      }
      this.syncRenderedOptionsSelection();
      this.cd.detectChanges();
    });
  }

  private syncRenderedOptionsSelection(): void {
    const optionKeys = this.optionKeys();
    if (!optionKeys) return;

    const selectedKeys = new Set(this.selectedOptions.map((option) => String(option.key)));
    optionKeys.forEach((optionKey) => {
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
