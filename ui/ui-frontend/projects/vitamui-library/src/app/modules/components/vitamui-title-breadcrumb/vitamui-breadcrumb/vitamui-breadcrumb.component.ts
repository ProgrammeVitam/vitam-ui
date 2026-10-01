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
import { Component, computed, EventEmitter, inject, input, Output, Signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { ApplicationId } from '../../../application-id.enum';
import { ApplicationService } from '../../../application.service';
import { Application } from '../../../models/application/application.interface';
import { BreadCrumbData } from '../../../models/breadcrumb/breadcrumb.interface';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'vitamui-breadcrumb',
  templateUrl: './vitamui-breadcrumb.component.html',
  styleUrls: ['./vitamui-breadcrumb.component.scss'],
  imports: [CommonModule, TranslatePipe],
})
export class VitamuiBreadcrumbComponent {
  private route = inject(ActivatedRoute);
  private applicationService = inject(ApplicationService);

  public data = input<BreadCrumbData[]>();

  @Output()
  public selected = new EventEmitter<BreadCrumbData>();

  private readonly appId: string = this.route.snapshot.data['appId'];

  private readonly app: Signal<Application | undefined> = this.appId ? toSignal(this.applicationService.getAppById(this.appId)) : undefined;

  protected readonly breadcrumb: Signal<BreadCrumbData[] | undefined> = computed(() => {
    const inputData = this.data();
    if (inputData?.length) return inputData;
    if (!this.appId) {
      console.error('NO appId');
      return undefined;
    }
    const app = this.app?.();
    if (!app) return undefined;
    return [{ identifier: ApplicationId.PORTAL_APP }, { label: app.name, identifier: this.appId }];
  });

  public onClick(d: BreadCrumbData, emit: boolean): void {
    if (emit) {
      this.selected.emit(d);
    }
  }
}
