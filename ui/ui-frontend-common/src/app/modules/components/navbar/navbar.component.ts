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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { rotateUpAnimation } from '../../animations/vitamui-common-animations';

import { ApplicationId } from '../../application-id.enum';
import { AuthService } from '../../auth.service';
import { AuthUser } from '../../models';
import { StartupService } from '../../startup.service';
import { SubrogationService } from '../../subrogation/subrogation.service';
import { MenuOption } from './customer-menu/menu-option.interface';

@Component({
  selector: 'vitamui-common-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  animations: [
    rotateUpAnimation,
  ]
})
export class NavbarComponent {

  @Input() appId: string;
  @Input() hideTenantMenu = false;
  @Input() hideCustomerMenu = false;
  @Input() customers: MenuOption[];

  @Output() tenantSelect = new EventEmitter<number>();
  @Output() customerSelect = new EventEmitter<string>();

  portalUrl: string;
  base64Logo: string;
  currentUser: AuthUser;
  hasAccountProfile = false;
  trustedInlineLogoUrl: SafeUrl;
  trustedAppLogoUrl: SafeUrl;

  constructor(
    public authService: AuthService,
    startupService: StartupService,
    private subrogationService: SubrogationService,
    private domSanitizer: DomSanitizer) {
    this.portalUrl = startupService.getPortalUrl();
    this.base64Logo = startupService.getLogo();

    this.trustedAppLogoUrl = startupService.getAppLogoURL() ?
    this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + startupService.getAppLogoURL()) : null;

    if (this.authService.user) {
      this.currentUser = this.authService.user;
      this.trustedInlineLogoUrl = startupService.getCustomerLogoURL() ?
      this.domSanitizer.bypassSecurityTrustUrl('data:image/*;base64,' + startupService.getCustomerLogoURL()) : null;
      this.hasAccountProfile = this.authService.user.profileGroup.profiles.find((profile) =>
                                                                  profile.applicationName === ApplicationId.ACCOUNTS_APP) !== undefined;
    }
  }

  emitTenantSelect(tenantIdentifier: number) {
    this.tenantSelect.emit(tenantIdentifier);
  }

  emitCustomerSelect(customerId: string) {
    this.customerSelect.emit(customerId);
  }

  enabledSubrogation() {
    this.subrogationService.checkSubrogation();
  }

}
