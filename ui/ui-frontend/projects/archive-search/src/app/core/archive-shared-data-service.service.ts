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
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Direction } from 'ui-frontend-common';
import { FilingHoldingSchemeNode } from '../archive/models/node.interface';
import { NodeData } from '../archive/models/nodedata.interface';
import { SearchCriteriaHistory } from '../archive/models/search-criteria-history.interface';
import { ResultFacet, SearchCriteriaAddAction, SearchCriteriaRemoveAction } from '../archive/models/search.criteria';
import { Unit } from '../archive/models/unit.interface';

@Injectable({
  providedIn: 'root',
})
export class ArchiveSharedDataServiceService {
  private sourceNode = new BehaviorSubject<NodeData>(new NodeData());
  private filingHoldingNodesSubject = new BehaviorSubject<FilingHoldingSchemeNode[]>(null);
  private targetNode = new BehaviorSubject<string>('');
  private facetsSubject = new BehaviorSubject<ResultFacet[]>([]);
  private toggleSubject = new BehaviorSubject<boolean>(true);
  private toggleReverseSubject = new BehaviorSubject<boolean>(true);
  private archiveUnitTpPreviewSubject = new BehaviorSubject<Unit>(null);
  private toggleArchiveUnitSubject = new BehaviorSubject<boolean>(true);
  private storedSearchCriteriaHistorySubject = new BehaviorSubject<SearchCriteriaHistory>(null);
  private allSearchCriteriaHistorySubject = new BehaviorSubject<SearchCriteriaHistory[]>([]);

  private simpleSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private appraisalSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);
  private accessSearchCriteriaAddSubject = new BehaviorSubject<SearchCriteriaAddAction>(null);

  private searchAppraisalCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);
  private searchAccessCriteriaActionFromMainSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);

  private searchCriteriaRemoveFromChildSubject = new BehaviorSubject<SearchCriteriaRemoveAction>(null);

  private entireNodes = new BehaviorSubject<string[]>([]);

  private auTitleSubject = new BehaviorSubject<string>('');

  private actionSubject = new BehaviorSubject<string>('');

  currentNode = this.sourceNode.asObservable();
  currentNodeTarget = this.targetNode.asObservable();
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
  accessFromMainSearchCriteriaObservable = this.searchAccessCriteriaActionFromMainSubject.asObservable();
  removeFromApraisalSearchCriteriaObservable = this.searchCriteriaRemoveFromChildSubject.asObservable();

  filingHoldingNodes = this.filingHoldingNodesSubject.asObservable();

  entireNodesObservable = this.entireNodes.asObservable();

  constructor() {}

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

  addAccessSearchCriteriaSubject(searchCriteria: SearchCriteriaAddAction) {
    this.accessSearchCriteriaAddSubject.next(searchCriteria);
  }

  receiveAccessSearchCriteriaSubject(): Observable<SearchCriteriaAddAction> {
    return this.accessSearchCriteriaAddSubject.asObservable();
  }
  sendAppraisalFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchAppraisalCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveAppraisalFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchAppraisalCriteriaActionFromMainSubject.asObservable();
  }

  sendAccessFromMainSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchAccessCriteriaActionFromMainSubject.next(searchCriteriaAction);
  }

  receiveAccessFromMainSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchAccessCriteriaActionFromMainSubject.asObservable();
  }

  sendRemoveFromChildSearchCriteriaAction(searchCriteriaAction: SearchCriteriaRemoveAction) {
    this.searchCriteriaRemoveFromChildSubject.next(searchCriteriaAction);
  }

  receiveRemoveFromChildSearchCriteriaSubject(): Observable<SearchCriteriaRemoveAction> {
    return this.searchCriteriaRemoveFromChildSubject.asObservable();
  }
}
