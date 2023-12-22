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
import { coerceBooleanProperty } from '@angular/cdk/coercion';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  forwardRef,
  Input,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormControl,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import { SearchBarComponent } from '../../search-bar';
import { Option } from '../utils/option.interface';
import { VitamuiAutocompleteMultiselectOptions } from '../utils/vitamui-autocomplete-multiselect-options.interface';
import { MatOption, MatOptionSelectionChange } from '@angular/material/core';
import { CdkVirtualScrollViewport, ScrollDispatcher } from '@angular/cdk/scrolling';
import { filter } from 'rxjs/operators';
import { MatSelect } from '@angular/material/select';
import { merge } from 'rxjs';

export const VITAMUI_MULTISELECT_AUTOCOMPLETE_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUIAutocompleteMultiSelectComponent),
  multi: true,
};

export const VITAMUI_MULTISELECT_AUTOCOMPLETE_NG_VALIDATORS: any = {
  provide: NG_VALIDATORS,
  useExisting: forwardRef(() => VitamUIAutocompleteMultiSelectComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-common-autocomplete-multi-select',
  templateUrl: './vitamui-autocomplete-multi-select.component.html',
  styleUrls: ['./vitamui-autocomplete-multi-select.component.scss'],
  providers: [VITAMUI_MULTISELECT_AUTOCOMPLETE_VALUE_ACCESSOR, VITAMUI_MULTISELECT_AUTOCOMPLETE_NG_VALIDATORS],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VitamUIAutocompleteMultiSelectComponent implements ControlValueAccessor, Validator, AfterViewInit {
  @Input() placeholder: string;
  @Input() searchBarPlaceHolder: string;
  @Input() enableSelectAll = true;

  public nbSelectedItemsMap: { [k: string]: string } = {
    '=1': 'MULTIPLE_SELECT_AUTOCOMPLETE.SELECTED_ELEMENT.SINGULAR',
    other: 'MULTIPLE_SELECT_AUTOCOMPLETE.SELECTED_ELEMENT.PLURAL',
  };
  @ViewChild('searchBar') searchBar: SearchBarComponent;
  @ViewChild('scrollViewport') private cdkVirtualScrollViewport: CdkVirtualScrollViewport;
  @ViewChildren(MatOption) optionKeys: QueryList<MatOption>;
  @ViewChild('matSelect') matSelect: MatSelect;

  public readonly SELECT_ALL_OPTIONS = 'SELECT_ALL_OPTIONS';
  public control = new FormControl([]);
  public searchTextControl = new FormControl();
  public showOnlySelectedOption = false;
  public allOptions: Option[] = [];
  public displayedOptions: Option[] = [];
  public selectedOptions: Option[] = [];
  public containerHeightInSearchView: string = '0px';
  public containerHeightInSelectedItemsView: string = '0px';

  private initialHeightInSearchView = 111;
  private initialHeightInSelectedItemsView = 106;
  private preselectedOptionKeys: string[] = [];
  private customSorting: (a: Option, b: Option) => number;

  constructor(private cd: ChangeDetectorRef, readonly sd: ScrollDispatcher) {}

  @Input()
  set multiSelectOptions(multiselectOptions: VitamuiAutocompleteMultiselectOptions) {
    this.allOptions = multiselectOptions?.options != null ? multiselectOptions.options : [];
    if (multiselectOptions?.customSorting != null) {
      this.customSorting = multiselectOptions.customSorting;
      this.allOptions.sort(this.customSorting);
    }
    this.displayedOptions = this.allOptions;
    this.resizeContainerHeightInSearchView();
    this.selectedOptions = this.allOptions.filter((option) => this.preselectedOptionKeys?.includes(option.key));
    this.updateSelectAll();
    this.resizeContainerHeightInSelectedItemsView();
  }

  // tslint:disable-next-line:variable-name
  private _required = false;

  @Input()
  get required(): boolean {
    return this._required;
  }

  ngAfterViewInit(): void {
    merge(this.sd.scrolled().pipe(filter((scrollable) => this.cdkVirtualScrollViewport === scrollable)), this.optionKeys.changes).subscribe(
      () => this.updateCheckboxes()
    );
  }

  private updateCheckboxes(): void {
    if (this.optionKeys == null) {
      return;
    }

    let needUpdate = false;

    this.optionKeys.forEach((optionKey) => {
      const selected = this.selectedOptions.filter((selectedOption) => selectedOption.key === optionKey.value);

      if (selected.length > 0 && !optionKey.selected) {
        optionKey.select();
        needUpdate = true;
      } else if (selected.length === 0 && optionKey.selected) {
        optionKey.deselect();
        needUpdate = true;
      }
    });

    if (needUpdate) {
      this.cd.detectChanges();
    }

    this.updateMatSelectTriggerContent();
  }

  set required(value: boolean) {
    this._required = coerceBooleanProperty(value);
  }

  public openedChange(opened: boolean): void {
    if (opened) {
      this.searchBar.onFocus();
    }
  }

  writeValue(preselectedOptionKeys: string[]) {
    this.preselectedOptionKeys = preselectedOptionKeys;
    // When the component is reset this method is called with selectedOptionKeys = null
    if (this.preselectedOptionKeys == null) {
      this.selectedOptions = [];
      this.control.reset();
    } else {
      this.selectedOptions = this.allOptions.filter((option) => this.preselectedOptionKeys.includes(option.key));
    }

    this.updateSelectAll();
    this.resizeContainerHeightInSelectedItemsView();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean) {
    if (disabled) {
      this.control.disable({ emitEvent: false });
    } else {
      this.control.enable({ emitEvent: false });
    }
  }

  validate(_control: AbstractControl): ValidationErrors | null {
    if (this.required && this.selectedOptions.length === 0) {
      return { required: true };
    }
    return null;
  }

  public getSelectedOptionsCount(): number {
    return this.selectedOptions.filter((option) => option.key !== this.SELECT_ALL_OPTIONS).length;
  }

  public toogleShowOnlySelectedOption(): void {
    this.showOnlySelectedOption = !this.showOnlySelectedOption;
    if (!this.showOnlySelectedOption) {
      this.displayedOptions = this.allOptions;
      this.resizeContainerHeightInSearchView();
      this.searchTextControl.reset();
    } else {
      this.searchBar.reset();
    }
  }

  public toggleSelectAll(event: MatOptionSelectionChange): void {
    if (!event.isUserInput) {
      return;
    }

    this.selectAll(event.source.selected);
  }

  public clearAllSelectedOptions(): void {
    this.showOnlySelectedOption = false;
    this.control.reset();
    this.onChange([]);
    this.selectedOptions = [];
  }

  public onSearch(value: string): void {
    this.searchTextControl.setValue(value ? value : null);
    if (this.searchTextControl.value) {
      this.displayedOptions = this.allOptions.filter(
        (option) => option.label.toLowerCase().indexOf(this.searchTextControl.value.toLowerCase()) !== -1
      );
      this.resizeContainerHeightInSearchView();
    }
  }

  public onSelectClosed(): void {
    this.showOnlySelectedOption = false;
    if (this.searchBar) {
      this.searchBar.reset();
    }
    this.searchTextControl.reset();
  }

  public resetSearchBar(): void {
    this.searchTextControl.reset();
    this.displayedOptions = this.allOptions;
    this.resizeContainerHeightInSearchView();
    this.searchBar.onFocus();
  }

  public onSelectionChange(change: MatOptionSelectionChange) {
    if (!change.isUserInput) {
      return;
    }

    const selected = this.selectedOptions.filter((selectedOption) => selectedOption.key === change.source.value);

    if (selected.length > 0) {
      this.selectedOptions = this.selectedOptions.filter((selectedOption) => selectedOption.key !== change.source.value);
    } else {
      this.selectedOptions.push(this.allOptions.filter((selectedOption) => selectedOption.key === change.source.value)[0]);
    }

    this.resizeContainerHeightInSelectedItemsView();

    if (this.selectedOptions.length === 0) {
      this.showOnlySelectedOption = false;
      this.onChange([]);
    } else {
      const selectedKeys = [...this.selectedOptions.map((option) => option.key)].sort();
      this.onChange(selectedKeys);
    }

    this.updateSelectAll();
    this.updateMatSelectTriggerContent();
  }

  public calculateContainerHeight(initialHeight: number, optionLenght: number): string {
    const itemHeight = 48;
    const visibleItems = 5;

    if (optionLenght <= visibleItems) {
      return `${initialHeight + itemHeight * optionLenght}px`;
    }

    return `${initialHeight + itemHeight * visibleItems}px`;
  }

  private onChange = (_: any) => {};

  private onTouched = () => {};

  private updateSelectAll(): void {
    const selectedOptionsCount: number = this.getSelectedOptionsCount();
    if (selectedOptionsCount > 0 && selectedOptionsCount === this.allOptions.length) {
      this.selectAll(true);
    } else {
      const index = this.selectedOptions.findIndex((option) => option.key === this.SELECT_ALL_OPTIONS);
      if (index !== -1) {
        this.selectedOptions.splice(index, 1);
        const selectedKeys = [...this.selectedOptions.map((option) => option.key)].sort();
        this.onChange(selectedKeys);
        this.updateCheckboxes();
      }
    }
  }

  private selectAll(value: boolean): void {
    if (value) {
      this.selectedOptions = [...this.allOptions, { key: this.SELECT_ALL_OPTIONS }];
      const selectedKeys = [...this.selectedOptions.map((option) => option.key)].filter((key) => key !== this.SELECT_ALL_OPTIONS).sort();
      this.onChange(selectedKeys);
      this.updateCheckboxes();
    } else {
      this.clearAllSelectedOptions();
    }
  }

  private updateMatSelectTriggerContent(): void {
    Object.defineProperties(this.matSelect, {
      empty: {
        value: this.selectedOptions.length <= 0,
        writable: true,
      },
    });
  }

  private resizeContainerHeightInSearchView(): void {
    this.containerHeightInSearchView = this.calculateContainerHeight(this.initialHeightInSearchView, this.displayedOptions.length);
    this.checkViewportSize();
  }

  private resizeContainerHeightInSelectedItemsView(): void {
    this.containerHeightInSelectedItemsView = this.calculateContainerHeight(
      this.initialHeightInSelectedItemsView,
      this.selectedOptions.length
    );
    this.checkViewportSize();
  }

  private checkViewportSize(): void {
    if (this.cdkVirtualScrollViewport) {
      this.cdkVirtualScrollViewport.checkViewportSize();
    }
  }
}
