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

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  CriteriaSearchCriteria,
  Direction,
  FilingHoldingSchemeNode,
  QueryParamsService,
  ResultFacet,
  SearchCriteriaAddAction,
  SearchCriteriaDto,
  SearchCriteriaHistory,
  SearchCriteriaRemoveAction,
  Unit,
} from 'vitamui-library';
import { NodeData } from '../archive-search-collect/archive-search-criteria/models/nodedata.interface';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSharedDataService {
  private sourceNode = new BehaviorSubject<NodeData>(new NodeData());
  private filingHoldingNodesSubject = new BehaviorSubject<FilingHoldingSchemeNode[]>(null);
  private selectedUnitSubject = new BehaviorSubject<Unit>(null);
  private targetNode = new BehaviorSubject<string>('');
  private facetsSubject = new BehaviorSubject<ResultFacet[]>([]);
  private totalResultsSubject = new BehaviorSubject<number>(null);
  private toggleSubject = new BehaviorSubject<boolean>(true);
  public numberOfAUsWithoutAttachment = new BehaviorSubject<number>(0);
  private lastSearchCriterias = new BehaviorSubject<SearchCriteriaDto>(null);
  private storedSearchCriteriaHistorySubject = new BehaviorSubject<SearchCriteriaHistory>(null);
  private allSearchCriteriaHistorySubject = new BehaviorSubject<SearchCriteriaHistory[]>([]);

  private simpleSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);

  private searchAppraisalCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchStorageCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchAccessCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchReuseCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchDisseminationCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);

  private searchCriteriaRemoveFromChildSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchCriteriaSubject = new BehaviorSubject<Map<string, CriteriaSearchCriteria>>(null);

  private ruleCategory = new BehaviorSubject<string>('');
  public selectedUnit$ = this.selectedUnitSubject.asObservable();
  public numberOfAUsWithoutAttachment$ = this.numberOfAUsWithoutAttachment.asObservable();

  get searchCriteria(): Map<string, CriteriaSearchCriteria> {
    return this.searchCriteriaSubject.getValue();
  }
  searchCriteria$ = this.searchCriteriaSubject.asObservable();

  appraisalFromMainSearchCriteriaObservable = this.searchAppraisalCriteriaActionFromMainSubject.asObservable();
  storageFromMainSearchCriteriaObservable = this.searchStorageCriteriaActionFromMainSubject.asObservable();

  accessFromMainSearchCriteriaObservable = this.searchAccessCriteriaActionFromMainSubject.asObservable();
  reuseFromMainSearchCriteriaObservable = this.searchReuseCriteriaActionFromMainSubject.asObservable();
  disseminationFromMainSearchCriteriaObservable = this.searchDisseminationCriteriaActionFromMainSubject.asObservable();

  constructor(private queryParamsService: QueryParamsService) {}

  emitNumberOfAUsWithoutAttachment(value: number) {
    this.numberOfAUsWithoutAttachment.next(value);
  }

  emitRuleCategory(ruleCategory: string) {
    this.ruleCategory.next(ruleCategory);
  }

  getRuleCategory(): Observable<string> {
    return this.ruleCategory.asObservable();
  }

  emitFilingHoldingNodes(node: FilingHoldingSchemeNode[]) {
    this.filingHoldingNodesSubject.next(node);
  }

  getFilingHoldingNodes(): Observable<FilingHoldingSchemeNode[]> {
    return this.filingHoldingNodesSubject.asObservable();
  }

  emitSelectedUnit(node: Unit) {
    this.selectedUnitSubject.next(node);
  }

  emitNode(node: NodeData) {
    this.sourceNode.next(node);
  }

  getNodes(): Observable<NodeData> {
    return this.sourceNode.asObservable();
  }

  emitNodeTarget(nodeId: string) {
    this.targetNode.next(nodeId);
  }

  getNodesTarget(): Observable<string> {
    return this.targetNode.asObservable();
  }

  emitFacets(facets: ResultFacet[]) {
    this.facetsSubject.next(facets);
  }

  getFacets(): Observable<ResultFacet[]> {
    return this.facetsSubject.asObservable();
  }

  emitTotalResults(result: number) {
    this.totalResultsSubject.next(result);
  }

  getTotalResults(): Observable<number> {
    return this.totalResultsSubject.asObservable();
  }

  emitToggle(show: boolean) {
    this.toggleSubject.next(show);
  }

  getToggle(): Observable<boolean> {
    return this.toggleSubject.asObservable();
  }

  emitSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory) {
    this.storedSearchCriteriaHistorySubject.next(searchCriteriaHistory);
  }

  getSearchCriteriaHistoryShared(): Observable<SearchCriteriaHistory> {
    return this.storedSearchCriteriaHistorySubject.asObservable();
  }

  emitSearchCriterias(searchCriteriaDto: SearchCriteriaDto): void {
    this.lastSearchCriterias.next(searchCriteriaDto);
  }

  getSearchCriterias(): Observable<SearchCriteriaDto> {
    return this.lastSearchCriterias.asObservable();
  }

  emitAllSearchCriteriaHistory(searchCriteriaHistory: SearchCriteriaHistory[]) {
    this.allSearchCriteriaHistorySubject.next(searchCriteriaHistory);
  }

  getAllSearchCriteriaHistoryShared(): Observable<SearchCriteriaHistory[]> {
    return this.allSearchCriteriaHistorySubject.asObservable();
  }

  nbFilters(searchCriteriaHistory: SearchCriteriaHistory): number {
    let sum = 0;
    if (searchCriteriaHistory.searchCriteriaList.length > 0) {
      searchCriteriaHistory.searchCriteriaList.forEach((criteria) => {
        sum += criteria.values.length;
      });
    }

    return sum;
  }

  sort(direction: Direction, searchCriteriaHistory: SearchCriteriaHistory[]): SearchCriteriaHistory[] {
    switch (direction) {
      case Direction.ASCENDANT:
        searchCriteriaHistory.sort((a, b) => {
          return <any>new Date(b.savingDate) - <any>new Date(a.savingDate);
        });
        break;
      case Direction.DESCENDANT:
        searchCriteriaHistory.sort((a, b) => {
          return <any>new Date(a.savingDate) - <any>new Date(b.savingDate);
        });
        break;
      default:
        break;
    }
    return searchCriteriaHistory;
  }

  addSimpleSearchCriteriaSubjects(searchCriteriaList: SearchCriteriaAddAction[]) {
    const builder = this.queryParamsService.builder();
    searchCriteriaList.forEach((searchCriteria) => {
      builder.addQueryParam(searchCriteria.valueElt.id, searchCriteria.valueElt.value);
      this.simpleSearchCriteriaAddSubject.next(searchCriteria);
    });
    builder.navigate();
  }

  addSimpleSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.addSimpleSearchCriteriaSubjects([searchCriteria]);
  }

  receiveSimpleSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.simpleSearchCriteriaAddSubject.asObservable();
  }

  sendAppraisalFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchAppraisalCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  sendStorageFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchStorageCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  sendAccessFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchAccessCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  sendReuseFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchReuseCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  sendDisseminationFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchDisseminationCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  sendRemoveFromChildSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    const builder = this.queryParamsService.builder();
    builder.removeQueryParam(searchCriteriaAction.valueElt.id, searchCriteriaAction.valueElt.value);
    this.searchCriteriaRemoveFromChildSubject.next(searchCriteriaAction);
    builder.navigate();
  }

  receiveRemoveFromChildSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchCriteriaRemoveFromChildSubject.asObservable();
  }

  emitSearchCriteriaChange(searchCriteria: Map<string, CriteriaSearchCriteria>) {
    this.searchCriteriaSubject.next(searchCriteria);
  }
}
