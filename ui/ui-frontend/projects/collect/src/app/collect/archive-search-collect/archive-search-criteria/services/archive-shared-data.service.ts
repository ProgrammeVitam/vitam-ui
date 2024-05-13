/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  Direction,
  FilingHoldingSchemeNode,
  ResultFacet,
  SearchCriteriaAddAction,
  SearchCriteriaDto,
  SearchCriteriaHistory,
  SearchCriteriaRemoveAction,
  Unit,
} from 'vitamui-library';
import { NodeData } from '../models/nodedata.interface';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSharedDataService {
  private sourceNode = new BehaviorSubject<NodeData>(new NodeData());
  private filingHoldingNodesSubject = new BehaviorSubject<FilingHoldingSchemeNode[]>(null);
  private targetNode = new BehaviorSubject<string>('');
  private facetsSubject = new BehaviorSubject<ResultFacet[]>([]);
  private totalResultsSubject = new BehaviorSubject<number>(null);
  private toggleSubject = new BehaviorSubject<boolean>(true);
  private toggleReverseSubject = new BehaviorSubject<boolean>(true);
  private archiveUnitTpPreviewSubject = new BehaviorSubject<Unit>(null);
  private toggleArchiveUnitSubject = new BehaviorSubject<boolean>(true);
  private lastSearchCriterias = new BehaviorSubject<SearchCriteriaDto>(null);
  private storedSearchCriteriaHistorySubject = new BehaviorSubject<SearchCriteriaHistory>(null);
  private allSearchCriteriaHistorySubject = new BehaviorSubject<SearchCriteriaHistory[]>([]);

  private simpleSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private appraisalSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private storageSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private accessSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private reuseSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private disseminationSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);

  private searchAppraisalCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchStorageCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchAccessCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchReuseCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchDisseminationCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);

  private searchCriteriaRemoveFromChildSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);

  private entireNodes = new BehaviorSubject<string[]>([]);

  private auTitleSubject = new BehaviorSubject<string>('');

  private actionSubject = new BehaviorSubject<string>('');

  private ruleCategory = new BehaviorSubject<string>('');

  currentNode = this.sourceNode.asObservable();
  facetsObservable = this.facetsSubject.asObservable();
  toggleObservable = this.toggleSubject.asObservable();
  toggleReverseObservable = this.toggleReverseSubject.asObservable();
  archiveUnitToPreviewObservable = this.archiveUnitTpPreviewSubject.asObservable();
  toggleArchiveUnitObservable = this.toggleArchiveUnitSubject.asObservable();
  storedSearchCriteriaHistoryObservable = this.storedSearchCriteriaHistorySubject.asObservable();
  auTitleObservable = this.auTitleSubject.asObservable();
  actionObservable = this.actionSubject.asObservable();
  allSearchCriteriaHistoryObservable = this.allSearchCriteriaHistorySubject.asObservable();

  simpleSearchCriteriaAddObservable = this.simpleSearchCriteriaAddSubject.asObservable();

  appraisalFromMainSearchCriteriaObservable = this.searchAppraisalCriteriaActionFromMainSubject.asObservable();
  storageFromMainSearchCriteriaObservable = this.searchStorageCriteriaActionFromMainSubject.asObservable();

  accessFromMainSearchCriteriaObservable = this.searchAccessCriteriaActionFromMainSubject.asObservable();
  reuseFromMainSearchCriteriaObservable = this.searchReuseCriteriaActionFromMainSubject.asObservable();
  disseminationFromMainSearchCriteriaObservable = this.searchDisseminationCriteriaActionFromMainSubject.asObservable();

  constructor() {}

  emitRuleCategory(ruleCategory: string) {
    this.ruleCategory.next(ruleCategory);
  }

  getRuleCategory(): Observable<string> {
    return this.ruleCategory.asObservable();
  }

  emitEntireNodes(nodes: string[]) {
    this.entireNodes.next(nodes);
  }

  getEntireNodes(): Observable<string[]> {
    return this.entireNodes.asObservable();
  }

  emitFilingHoldingNodes(node: FilingHoldingSchemeNode[]) {
    this.filingHoldingNodesSubject.next(node);
  }

  getFilingHoldingNodes(): Observable<FilingHoldingSchemeNode[]> {
    return this.filingHoldingNodesSubject.asObservable();
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

  emitArchiveUnitTitle(auTitle: string) {
    this.auTitleSubject.next(auTitle);
  }

  emitActionChosen(action: string) {
    this.auTitleSubject.next(action);
  }

  getActionChosen(): Observable<string> {
    return this.auTitleSubject.asObservable();
  }

  getArchiveUnitTitle(): Observable<string> {
    return this.auTitleSubject.asObservable();
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
          // tslint:disable-next-line:no-angle-bracket-type-assertion
          return <any>new Date(b.savingDate) - <any>new Date(a.savingDate);
        });
        break;
      case Direction.DESCENDANT:
        searchCriteriaHistory.sort((a, b) => {
          // tslint:disable-next-line:no-angle-bracket-type-assertion
          return <any>new Date(a.savingDate) - <any>new Date(b.savingDate);
        });
        break;
      default:
        break;
    }
    return searchCriteriaHistory;
  }

  addSimpleSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.simpleSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveSimpleSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.simpleSearchCriteriaAddSubject.asObservable();
  }

  addAppraisalSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.appraisalSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveAppraisalSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.appraisalSearchCriteriaAddSubject.asObservable();
  }

  addStorageSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.storageSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveStorageSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.storageSearchCriteriaAddSubject.asObservable();
  }

  addAccessSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.accessSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveAccessSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.accessSearchCriteriaAddSubject.asObservable();
  }

  addDisseminationSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.disseminationSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveDisseminationSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.disseminationSearchCriteriaAddSubject.asObservable();
  }

  addReuseSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.reuseSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveReuseSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.reuseSearchCriteriaAddSubject.asObservable();
  }

  sendAppraisalFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchAppraisalCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveAppraisalFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchAppraisalCriteriaActionFromMainSubject.asObservable();
  }

  sendStorageFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchStorageCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveStorageFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchStorageCriteriaActionFromMainSubject.asObservable();
  }

  sendAccessFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchAccessCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveAccessFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchAccessCriteriaActionFromMainSubject.asObservable();
  }

  sendReuseFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchReuseCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveReuseFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchReuseCriteriaActionFromMainSubject.asObservable();
  }

  sendDisseminationFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchDisseminationCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveDisseminationFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchDisseminationCriteriaActionFromMainSubject.asObservable();
  }

  sendRemoveFromChildSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchCriteriaRemoveFromChildSubject.next(searchCriteriaAction);
  }

  receiveRemoveFromChildSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchCriteriaRemoveFromChildSubject.asObservable();
  }
}
