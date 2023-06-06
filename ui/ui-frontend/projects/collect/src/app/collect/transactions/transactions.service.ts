/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { DEFAULT_PAGE_SIZE, Direction, PageRequest, PaginatedResponse, Project, SearchService, Transaction } from 'ui-frontend-common';
import { ProjectsApiService } from '../core/api/project-api.service';
import { TransactionApiService } from '../core/api/transaction-api.service';

@Injectable({
  providedIn: 'root',
})
export class TransactionsService extends SearchService<Transaction> {
  transactions$: BehaviorSubject<Transaction[]> = new BehaviorSubject<Transaction[]>([]);
  project$: BehaviorSubject<Project> = new BehaviorSubject<Project>(null);

  constructor(http: HttpClient, private transactionApiService: TransactionApiService, private projectApiService: ProjectsApiService) {
    super(http, transactionApiService, 'ALL');
  }

  public getTransactionsByProjectId(
    projectId: string,
    pageRequest: PageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT)
  ): Observable<PaginatedResponse<Transaction>> {
    this.pageRequest = pageRequest;
    return this.projectApiService.getTransactionsByProjectId(this.pageRequest, projectId);
  }

  public getProject$(): Observable<Project> {
    return this.project$;
  }

  public search(_: PageRequest = null): Observable<Transaction[]> {
    return this.transactions$;
  }

  public getTransactionById(transactionById: string) {
    return this.transactionApiService.getTransactionById(transactionById);
  }

  validateTransaction(id: string) {
    return this.transactionApiService.validateTransaction(id);
  }

  sendTransaction(id: string) {
    return this.transactionApiService.sendTransaction(id);
  }

  editTransaction(id: string) {
    return this.transactionApiService.editTransaction(id);
  }

  abortTransaction(id: string) {
    return this.transactionApiService.abortTransaction(id);
  }

  loadDataForTransactions(transactions: Transaction[], project: Project) {
    this.transactions$.next(transactions);
    this.project$.next(project);
  }
}
