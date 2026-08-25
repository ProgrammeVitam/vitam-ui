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
import { Clipboard } from '@angular/cdk/clipboard';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { rxResource, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, of } from 'rxjs';
import type { Unit } from 'vitamui-library';
import { AccessContract, AccessContractService, ApplicationId, ApplicationService, SnackBarService } from 'vitamui-library';
import { ArchiveUnitLifecycleHistoryService } from './archive-unit-lifecycle-history.service';
import { LifecycleOrigin, OperationLifecycleGroup } from './archive-unit-lifecycle-history.model';

@Component({
  selector: 'app-archive-unit-history-tab',
  templateUrl: './archive-unit-history-tab.component.html',
  styleUrls: ['./archive-unit-history-tab.component.scss'],
  standalone: false,
})
export class ArchiveUnitHistoryTabComponent {
  private route = inject(ActivatedRoute);
  private accessContractService = inject(AccessContractService);
  private applicationService = inject(ApplicationService);
  private lifecycleHistoryService = inject(ArchiveUnitLifecycleHistoryService);
  private clipboard = inject(Clipboard);
  private snackBarService = inject(SnackBarService);

  archiveUnit = input<Unit>();

  originFilter = signal<LifecycleOrigin[]>([]);
  operationTypeFilter = signal<string[]>([]);

  private readonly tenantIdentifier = +this.route.snapshot.params['tenantIdentifier'];

  private readonly logError = (e: unknown) => {
    console.error(e);
    return of(undefined);
  };

  private readonly logbookOperationAppUrl = toSignal(
    this.applicationService
      .getUrl$({ appId: ApplicationId.LOGBOOK_OPERATION_APP, tenantIdentifier: this.tenantIdentifier })
      .pipe(catchError(this.logError)),
  );

  private readonly accessContract = toSignal(this.accessContractService.currentAccessContract$.pipe(catchError(this.logError)));

  private readonly historyResource = rxResource<OperationLifecycleGroup[], { archiveUnit: Unit; accessContract: AccessContract }>({
    params: () => {
      const archiveUnit = this.archiveUnit();
      const accessContract = this.accessContract();
      return archiveUnit && accessContract ? { archiveUnit, accessContract } : undefined;
    },
    stream: ({ params }) =>
      this.lifecycleHistoryService.getConsolidatedHistory(
        params.archiveUnit['#id'],
        params.archiveUnit['#object'] || null,
        params.accessContract.id,
        this.tenantIdentifier,
      ),
  });

  loading = computed(() => this.historyResource.isLoading());

  operationGroups = computed(() => (this.historyResource.hasValue() ? this.historyResource.value() : []));

  availableOperationTypes = computed(() => Array.from(new Set(this.operationGroups().map((group) => group.evTypeProc))).sort());

  filteredOperationGroups = computed(() => {
    const operationTypeFilter = this.operationTypeFilter();
    const originFilter = this.originFilter();
    return this.operationGroups()
      .filter((group) => !operationTypeFilter.length || operationTypeFilter.includes(group.evTypeProc))
      .map((group) => ({
        ...group,
        events: group.events.filter((event) => !originFilter.length || originFilter.includes(event.origin)),
      }))
      .filter((group) => group.events.length > 0);
  });

  constructor() {
    effect(() => {
      const error = this.historyResource.error();
      if (error) {
        console.error(error);
        this.snackBarService.open({ message: 'ARCHIVE_SEARCH.ARCHIVE_UNIT_PREVIEW.HISTORY.LOAD_ERROR', duration: 10_000 });
      }
    });
  }

  getOperationLogbookUrl(evIdProc: string): string {
    const logbookOperationAppUrl = this.logbookOperationAppUrl();
    return logbookOperationAppUrl ? `${logbookOperationAppUrl}?guid=${evIdProc}` : null;
  }

  copyToClipboard(text: string): void {
    this.clipboard.copy(text);
  }
}
