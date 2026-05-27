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
import { Component, OnInit, inject } from '@angular/core';

import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Agency } from '../../../../../vitamui-library/src/lib/models/agency';
import { ApplicationId } from '../../../../../vitamui-library/src/app/modules/application-id.enum';
import { BreadCrumbData } from '../../../../../vitamui-library/src/app/modules/models/breadcrumb/breadcrumb.interface';
import { TenantSelectionService } from '../../../../../vitamui-library/src/app/modules/tenant-selection.service';
import { VitamUICommonModule } from '../../../../../vitamui-library/src/app/modules/vitamui-common.module';
import { VitamUILibraryModule } from '../../../../../vitamui-library/src/lib/vitamui-library.module';
import { AgencyService } from '../../../../../vitamui-library/src/app/modules/agencies/agency.service';
import { agencyTemplate } from '../agency.template';
import { of, switchMap } from 'rxjs';

@Component({
  selector: 'app-view-agency',
  templateUrl: 'view-agency.component.html',
  styleUrls: ['view-agency.component.scss'],
  imports: [RouterModule, VitamUICommonModule, VitamUILibraryModule],
})
export class ViewAgencyComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private tenantSelectionService = inject(TenantSelectionService);
  private agencyService = inject(AgencyService);

  readonly agencyTemplate = agencyTemplate;
  breadcrumbData: BreadCrumbData[] = [{ identifier: ApplicationId.PORTAL_APP }, { identifier: ApplicationId.AGENCIES_APP }];
  agency: Agency;

  constructor() {
    this.agency = this.router.getCurrentNavigation()?.extras?.state?.agency;
  }

  ngOnInit() {
    of(this.agency)
      .pipe(
        switchMap((agency: Agency) => {
          if (agency) return of(agency);

          return this.route.params.pipe(switchMap((params) => this.agencyService.get(params?.agencyIdentifier)));
        }),
      )
      .subscribe({
        next: (agency: Agency) => {
          this.agency = agency;
          this.breadcrumbData.push({ label: this.agency.identifier });
        },
      });
  }

  back() {
    this.router.navigate(['/agency/tenant/', this.tenantSelectionService.getSelectedTenant().identifier]);
  }
}
