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
import { ScrollDispatcher, ScrollingModule } from '@angular/cdk/scrolling';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  forwardRef,
  Injector,
  Input,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatOption, MatOptionModule, MatOptionSelectionChange } from '@angular/material/core';
import { MatSelect, MatSelectModule } from '@angular/material/select';
import { ItemFlatNode, ItemNode, SearchBarComponent } from '../../../app/modules';
import { MatTreeFlatDataSource, MatTreeFlattener, MatTreeModule } from '@angular/material/tree';
import { FlatTreeControl } from '@angular/cdk/tree';
import { SelectionModel } from '@angular/cdk/collections';
import { partition } from 'lodash-es';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, map } from 'rxjs/operators';
import { AbstractFormInputDirective } from '../abstract-form-input.directive';
import { normalizeString } from '../../utils/string.util';
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
import { PipesModule } from '../../../app/modules/pipes/pipes.module';
import { TranslatePipe } from '@ngx-translate/core';

const VITAMUI_SELECT_WITH_TREE_VALUE_ACCESSOR = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => SelectWithTreeComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-select-with-tree',
  templateUrl: './select-with-tree.component.html',
  styleUrls: ['./select-with-tree.component.scss'],
  imports: [
    AutocompletePositionDirectiveModule,
    CommonModule,
    CommonTooltipModule,
    EllipsisDirectiveModule,
    FormErrorsComponent,
    FormsModule,
    MatAutocompleteModule,
    MatAutocompleteModule,
    MatCheckboxModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatInputModule,
    MatInputModule,
    MatListModule,
    MatOptionModule,
    MatSelectModule,
    MatTreeModule,
    PipesModule,
    ReactiveFormsModule,
    ScrollingModule,
    SearchBarComponent,
    TranslatePipe,
  ],
  providers: [VITAMUI_SELECT_WITH_TREE_VALUE_ACCESSOR],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelectWithTreeComponent<T> extends AbstractFormInputDirective implements ControlValueAccessor, AfterViewInit, OnDestroy {
  @Input() placeholder: string;
  @Input() searchBarPlaceHolder: string;

  @Input() set multiple(multiple: boolean) {
    this._multiple = multiple;
    if (this._multiple) {
      if (this.enableDisplaySelected === undefined) this.enableDisplaySelected = true;
    }
  }
  get multiple(): boolean {
    return this._multiple;
  }

  @Input() enableSearch = true;
  @Input() enableDisplaySelected = true;

  @Input({ required: true })
  set options(itemNodes: ItemNode<T>[]) {
    this.dataSource.data = itemNodes;
  }

  protected nbSelectedItemsMap: { [k: string]: string } = {
    '=1': 'SELECT.SELECTED_ELEMENT.SINGULAR',
    other: 'SELECT.SELECTED_ELEMENT.PLURAL',
  };

  /**
   * A function that must return the value to display from an item
   */
  @Input({ required: true }) getDisplayValue: (item: T) => string;
  /**
   * A function that must return the value from which to search when filtering. Defaults to getDisplayValue if not set.
   */
  @Input() getSearchValue: (item: T) => string;

  @Input() searchMinLength = 3;
  @Input() searchDebounceTimeMs = 100;

  searchText = '';
  showOnlySelectedOption = false;

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

  private _multiple = false;

  constructor(
    injector: Injector,
    private cd: ChangeDetectorRef,
    readonly sd: ScrollDispatcher,
  ) {
    super(injector);
    this.treeFlattener = new MatTreeFlattener(this.transformer, this.getLevel, this.isExpandable, this.getChildren);
    this.treeControl = new FlatTreeControl<ItemFlatNode<T>>(this.getLevel, this.isExpandable);
    this.dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  }

  ngOnDestroy() {
    this.searchSubscription?.unsubscribe();
  }

  ngAfterViewInit(): void {
    if (!this.getSearchValue) this.getSearchValue = this.getDisplayValue;

    this.checklistSelection.changed.asObservable().subscribe((_change) => {
      this.updateMatSelectTriggerContent();
    });

    this.matSelect.options.changes.subscribe(() => this.updatedSelectedOptions());
    this.updatedSelectedOptions();
    this.updateMatSelectTriggerContent();

    const searchObservable = this.search$.pipe(debounceTime(this.searchDebounceTimeMs), distinctUntilChanged());
    this.searchSubscription = searchObservable.subscribe(() => this.doSearch());

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

  private matchSearch(node: ItemNode<T> | ItemFlatNode<T>, search: string) {
    const nodeNormalizedValue = normalizeString(this.getSearchValue(node.item));
    const searchNormalizedValue = normalizeString(search);
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
      if (this.multiple) {
        this.checklistSelection.toggle(node);
      } else {
        if (this.checklistSelection.selected.includes(node)) {
          this.checklistSelection.clear(true);
          this.matSelect.options.forEach((item) => item.deselect());
        } else {
          this.checklistSelection.setSelection(node);
        }
      }
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

  writeValue(initialValues?: T | T[]) {
    const correspondingFlatNodes = this.treeControl.dataNodes
      .filter((n) => !n.expandable)
      .filter((n) => (Array.isArray(initialValues) ? initialValues : [initialValues])?.includes(n.item));
    this.checklistSelection.setSelection(...correspondingFlatNodes);

    this.updatedSelectedOptions();
  }

  private updatedSelectedOptions() {
    if (this.matSelect?.options) {
      this.checklistSelection.selected.forEach((n) => this.matSelect.options.find((option) => option.value === n.item)?.select());
    }
  }

  protected getSelectedOptionsCount(): number {
    return this.checklistSelection.selected.length;
  }

  protected toggleShowOnlySelectedOption(): void {
    this.showOnlySelectedOption = !this.showOnlySelectedOption;
    this.updateAfterToggleShowOnlySelectedOption();
  }

  private updateAfterToggleShowOnlySelectedOption() {
    if (this.showOnlySelectedOption) {
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

    this.showOnlySelectedOption = false;
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
    this.showOnlySelectedOption = false;
    this.updateAfterToggleShowOnlySelectedOption();
  }

  protected readonly Validators = Validators;
}
