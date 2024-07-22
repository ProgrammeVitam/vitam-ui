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
import { ScrollDispatcher } from '@angular/cdk/scrolling';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  forwardRef,
  Input,
  OnDestroy,
  ViewChild,
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
import { MatLegacyOption as MatOption, MatLegacyOptionSelectionChange as MatOptionSelectionChange } from '@angular/material/legacy-core';
import { MatLegacySelect as MatSelect } from '@angular/material/legacy-select';
import { SearchBarComponent } from '../../search-bar/search-bar.component';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { FlatTreeControl } from '@angular/cdk/tree';
import { SelectionModel } from '@angular/cdk/collections';
import { partition } from 'lodash-es';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, map } from 'rxjs/operators';

/**
 * Node for item
 */
export class ItemNode<T> {
  children: ItemNode<T>[];
  item: T;
}

/** Flat item node with expandable and level information */
class ItemFlatNode<T> {
  id: string;
  item: T;
  level: number;
  expandable: boolean;
  display: boolean;
}

@Component({
  selector: 'vitamui-common-autocomplete-multi-select-tree',
  templateUrl: './vitamui-autocomplete-multi-select-tree.component.html',
  styleUrls: ['./vitamui-autocomplete-multi-select-tree.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => VitamUiAutocompleteMultiSelectTreeComponent),
      multi: true,
    },
    {
      provide: NG_VALIDATORS,
      useExisting: forwardRef(() => VitamUiAutocompleteMultiSelectTreeComponent),
      multi: true,
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VitamUiAutocompleteMultiSelectTreeComponent<T> implements ControlValueAccessor, Validator, AfterViewInit, OnDestroy {
  @Input({ required: true })
  set multiSelectOptions(itemNodes: ItemNode<T>[]) {
    this.dataSource.data = itemNodes;
  }
  /**
   * A function that must return the value to display from an item
   */
  @Input({ required: true }) getDisplayValue: (item: T) => string;
  /**
   * A function that must return the value from which to search when filtering. Defaults to getDisplayValue if not set.
   */
  @Input() getSearchValue: (item: T) => string;

  @Input() enableDisplaySelected = true;
  @Input() enableSearch = true;
  @Input({ transform: coerceBooleanProperty }) required = false;
  @Input() placeholder: string;
  @Input() searchBarPlaceHolder: string;
  @Input() searchMinLength = 3;
  @Input() searchDebounceTimeMs = 100;

  nbSelectedItemsMap: { [k: string]: string } = {
    '=1': 'MULTIPLE_SELECT_AUTOCOMPLETE.SELECTED_ELEMENT.SINGULAR',
    other: 'MULTIPLE_SELECT_AUTOCOMPLETE.SELECTED_ELEMENT.PLURAL',
  };
  control = new FormControl([]);
  searchText = '';
  showOnlySelected = false;

  @ViewChild('searchBar') searchBar: SearchBarComponent;
  @ViewChild('matSelect') matSelect: MatSelect;

  treeControl: FlatTreeControl<ItemFlatNode<T>>;
  dataSource: MatTreeFlatDataSource<ItemNode<T>, ItemFlatNode<T>>;
  private readonly treeFlattener: MatTreeFlattener<ItemNode<T>, ItemFlatNode<T>>;

  /** The selection for checklist */
  checklistSelection = new SelectionModel<ItemFlatNode<T>>(true /* multiple */);
  /** Map from nested node to flattened node. This helps us to keep the same object for selection */
  private nestedNodeMap = new Map<ItemNode<T>, ItemFlatNode<T>>();

  private idIncrement = 0;

  private search$: Subject<string> = new Subject();
  private searchSubscription: Subscription;

  constructor(
    private cd: ChangeDetectorRef,
    readonly sd: ScrollDispatcher,
  ) {
    this.treeFlattener = new MatTreeFlattener(this.transformer, this.getLevel, this.isExpandable, this.getChildren);
    this.treeControl = new FlatTreeControl<ItemFlatNode<T>>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  }

  ngOnDestroy() {
    this.searchSubscription?.unsubscribe();
  }

  ngAfterViewInit(): void {
    if (!this.getSearchValue) this.getSearchValue = this.getDisplayValue;

    this.checklistSelection.changed.asObservable().subscribe((change) => {
      this.onChange(change.source.selected.map((v) => v.item));
      this.updateMatSelectTriggerContent();
    });

    this.matSelect.options.changes.subscribe(() => this.updatedSelectedOptions());
    this.updatedSelectedOptions();
    this.updateMatSelectTriggerContent();

    const searchObservable = this.search$.pipe(debounceTime(this.searchDebounceTimeMs), distinctUntilChanged());
    if (this.enableSearch) {
      this.searchSubscription = searchObservable.subscribe(() => this.doSearch());
    }

    // Follow the active option (scrolls to keep the active option in the view) when navigating with arrow keys.
    this.matSelect._keyManager.change.asObservable().subscribe((index) => {
      const option = this.matSelect.options.get(index);
      option?._getHostElement()?.scrollIntoView({ block: 'nearest', inline: 'nearest' });
    });

    // Options order in matSelect won't reflect the order in the view after adding/removing Options (via tree node toggles) because of QueryList behaviour.
    // So, we have to update it to reflect the real order if we want to have keyboard accessibility and be able to navigate the options in the correct order with keyboard arrows.
    this.matSelect.options.changes
      .pipe(
        map((options) => options.toArray()),
        filter((options: MatOption[]) => options.length > 1),
      )
      .subscribe((options) => {
        const sortedOptions = options.sort((a: MatOption, b: MatOption) => {
          const position = a._getHostElement().compareDocumentPosition(b._getHostElement());
          return position & Node.DOCUMENT_POSITION_FOLLOWING ? -1 : 1;
        });
        this.matSelect.options.reset(sortedOptions);
        this.syncActiveItem();
      });

    this.searchBar?.searchInput?.nativeElement?.addEventListener('keydown', this.onKeydown.bind(this), { capture: true });
    this.matSelect._elementRef.nativeElement.addEventListener('keydown', this.onKeydown.bind(this), { capture: true });
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
        this.onSearch(this.searchBar.searchValue, true);
      }
      if (!['ArrowDown', 'ArrowUp', 'Escape'].includes(event.code)) {
        // Prevent most keyboard keypress to be interpreted by the mat-select, otherwise it would "search" in options or open/close toggles
        event.stopPropagation();
      }
    }
    if (!focusInSearchInput && ['Space', 'Enter', 'ArrowLeft', 'ArrowRight'].includes(event.code)) {
      const index = this.matSelect._keyManager.activeItemIndex;
      const option = this.matSelect.options.get(index);

      const node = this.treeControl.dataNodes.find((node) => node.id === option.value);
      if (['Space', 'Enter'].includes(event.code)) {
        this.treeControl.toggle(node);
      }
      if (['ArrowLeft'].includes(event.code)) {
        this.treeControl.collapse(node);
      }
      if (['ArrowRight'].includes(event.code)) {
        this.treeControl.expand(node);
      }

      this.syncActiveItem();
    }
  }

  /**
   * This syncs the active item for accessibility. It is required after sorting the matSelect Options if we want to stay on the same element.
   */
  private syncActiveItem() {
    const activeItem = this.matSelect._keyManager.activeItem;
    setTimeout(() => {
      const newIndex = this.matSelect.options.toArray().indexOf(activeItem);
      this.matSelect._keyManager.setActiveItem(newIndex);
    });
  }

  /**
   * Transformer to convert nested node to flat node. Record the nodes in maps for later use.
   */
  private transformer = (node: ItemNode<T>, level: number) => {
    const existingNode = this.nestedNodeMap.get(node);
    const flatNode = existingNode && existingNode.item === node.item ? existingNode : new ItemFlatNode<T>();
    flatNode.id = `node-${this.idIncrement++}`;
    flatNode.item = node.item;
    flatNode.level = level;
    flatNode.expandable = !!node.children?.length;
    flatNode.display = true;
    this.nestedNodeMap.set(node, flatNode);
    return flatNode;
  };
  private getLevel = (node: ItemFlatNode<T>) => node.level;
  private isExpandable = (node: ItemFlatNode<T>) => node.expandable;
  private getChildren = (node: ItemNode<T>): ItemNode<T>[] => node.children;
  hasChild = (_: number, _nodeData: ItemFlatNode<T>) => _nodeData.expandable;
  trackBy = (_: number, _nodeData: ItemFlatNode<T>) => _nodeData.id;

  private normalizeString(value?: string): string {
    return (
      value
        ?.normalize('NFD')
        ?.replace(/[\u0300-\u036f]/g, '')
        ?.toLowerCase() || ''
    );
  }

  private matchSearch(node: ItemNode<T> | ItemFlatNode<T>, search: string) {
    const nodeNormalizedValue = this.normalizeString(this.getSearchValue(node.item));
    const searchNormalizedValue = this.normalizeString(search);
    return nodeNormalizedValue.indexOf(searchNormalizedValue) !== -1;
  }

  private getParentNodes(node: ItemFlatNode<T>): ItemFlatNode<T>[] {
    const getParentNode = (node: ItemFlatNode<T>): ItemFlatNode<T> | null => {
      const currentLevel = this.getLevel(node);

      if (currentLevel < 1) {
        return null;
      }

      const startIndex = this.treeControl.dataNodes.indexOf(node) - 1;

      for (let i = startIndex; i >= 0; i--) {
        const currentNode = this.treeControl.dataNodes[i];

        if (this.getLevel(currentNode) < currentLevel) {
          return currentNode;
        }
      }
      return null;
    };

    const parentNode = getParentNode(node);
    return parentNode ? [parentNode, ...this.getParentNodes(parentNode)] : [];
  }

  /** Toggle a leaf item selection */
  leafItemSelectionToggle(event: MatOptionSelectionChange<string>, node: ItemFlatNode<T>): void {
    if (event.isUserInput) {
      this.checklistSelection.toggle(node);
    }
  }

  private updateMatSelectTriggerContent(): void {
    Object.defineProperties(this.matSelect, {
      empty: {
        value: this.checklistSelection.selected.length <= 0,
        writable: true,
      },
    });
  }

  public openedChange(opened: boolean): void {
    if (opened && this.enableSearch) {
      this.searchBar.onFocus();
    }
  }

  writeValue(initialValues?: T[]) {
    const correspondingFlatNodes = this.treeControl.dataNodes.filter((n) => !n.expandable).filter((n) => initialValues?.includes(n.item));
    this.checklistSelection.setSelection(...correspondingFlatNodes);

    this.updatedSelectedOptions();
  }

  private updatedSelectedOptions() {
    if (this.matSelect?.options) {
      this.checklistSelection.selected.forEach((n) => this.matSelect.options.find((option) => option.value === n.id)?.select());
    }
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
    if (this.required && this.checklistSelection.selected.length === 0) {
      return { required: true };
    }
    return null;
  }

  public toggleShowOnlySelectedOption(): void {
    this.showOnlySelected = !this.showOnlySelected;
    this.updateAfterToggleShowOnlySelectedOption();
  }

  private updateAfterToggleShowOnlySelectedOption() {
    if (this.showOnlySelected) {
      this.treeControl.dataNodes.forEach((n) => (n.display = false));
      this.checklistSelection.selected.forEach((n) => {
        const parentNodes = this.getParentNodes(n);
        [n, ...parentNodes].forEach((n) => {
          n.display = true;
          this.treeControl.expand(n);
        });
      });
    } else {
      this.searchBar?.reset();
      this.treeControl.collapseAll();
      this.treeControl.dataNodes.forEach((n) => (n.display = true));
    }
    this.syncActiveItem();
    this.matSelect._elementRef.nativeElement.focus();
  }

  public clearAllSelectedOptions(): void {
    this.checklistSelection.clear(true);
    this.matSelect.options.forEach((item) => item.deselect());

    this.showOnlySelected = false;
    this.updateAfterToggleShowOnlySelectedOption();
  }

  public onSearch(search: string, forceSearch = false): void {
    if (search && (search.length >= this.searchMinLength || forceSearch)) {
      this.searchText = search;
    } else {
      this.searchText = undefined;
    }

    this.search$.next(this.searchText);
  }

  private doSearch(): void {
    if (this.searchText) {
      const [nodesMatchingSearch, otherNodes] = partition(this.treeControl.dataNodes, (n) => this.matchSearch(n, this.searchText));

      const parentNodes = [...new Set(nodesMatchingSearch.flatMap((matchingNode) => this.getParentNodes(matchingNode)))];
      const descendantNodes = [...new Set(nodesMatchingSearch.flatMap((matchingNode) => this.treeControl.getDescendants(matchingNode)))];

      const matchingNodesAndParents = [...nodesMatchingSearch, ...parentNodes];
      const matchingNodesParentsAndDescendants = [...matchingNodesAndParents, ...descendantNodes];
      const nodesToHide = otherNodes.filter((n) => !matchingNodesParentsAndDescendants.includes(n));
      nodesToHide.forEach((n) => {
        n.display = false;
        this.treeControl.collapse(n);
      });
      matchingNodesParentsAndDescendants.forEach((n) => (n.display = true));
      matchingNodesAndParents.forEach((n) => this.treeControl.expand(n));
    } else {
      this.treeControl.collapseAll();
      this.treeControl.dataNodes.forEach((n) => (n.display = true));
    }
    this.cd.detectChanges();
  }

  public onSelectClosed(): void {
    this.onTouched();
    this.showOnlySelected = false;
    this.updateAfterToggleShowOnlySelectedOption();
  }

  private onChange = (_: any) => {};

  private onTouched = () => {};
}
