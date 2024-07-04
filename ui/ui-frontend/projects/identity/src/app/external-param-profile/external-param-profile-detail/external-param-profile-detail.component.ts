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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { Event, ExternalParamProfile, VitamuiSidenavHeaderComponent, HistoryModule } from 'vitamui-library';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { SharedService } from '../shared.service';
import { TranslateModule } from '@ngx-translate/core';
import { ThresholdsTabComponent } from './thresholds-tab/thresholds-tab.component';
import { InformationTabComponent } from './information-tab/information-tab.component';
import { MatLegacyTabsModule } from '@angular/material/legacy-tabs';

@Component({
  selector: 'app-external-param-profile-detail',
  templateUrl: './external-param-profile-detail.component.html',
  styleUrls: ['./external-param-profile-detail.component.css'],
  standalone: true,
  imports: [
    VitamuiSidenavHeaderComponent,
    MatLegacyTabsModule,
    InformationTabComponent,
    ThresholdsTabComponent,
    HistoryModule,
    TranslateModule,
  ],
})
export class ExternalParamProfileDetailComponent implements OnInit, OnDestroy {
  @Input() externalParamProfile: ExternalParamProfile;
  @Input() tenantIdentifier: string;
  @Input() isPopup: boolean;
  @Output() externalParamProfileClose = new EventEmitter();
  readOnly: boolean;
  externalParamProfileUpdateSub: Subscription;

  constructor(
    private sharedService: SharedService,
    private externalParamProfileServiceService: ExternalParamProfileService,
  ) {
    this.sharedService.getReadOnly().subscribe((readOnly) => {
      this.readOnly = readOnly;
    });
  }

  ngOnInit(): void {
    this.externalParamProfileUpdateSub = this.externalParamProfileServiceService.updated.subscribe((externalParamProfile) => {
      if (externalParamProfile) {
        this.externalParamProfileServiceService.getOne(externalParamProfile.idProfile).subscribe((newExternalParamProfile) => {
          this.externalParamProfile = newExternalParamProfile;
        });
      }
    });
  }

  emitClose() {
    this.externalParamProfileClose.emit();
  }

  ngOnDestroy(): void {
    this.externalParamProfileUpdateSub.unsubscribe();
  }

  filterEvents(event: Event): boolean {
    return (
      event.outDetail &&
      (event.outDetail.includes('EXT_VITAMUI_CREATE_EXTERNAL_PARAM_PROFILE') ||
        event.outDetail.includes('EXT_VITAMUI_UPDATE_EXTERNAL_PARAM_PROFILE'))
    );
  }
}
