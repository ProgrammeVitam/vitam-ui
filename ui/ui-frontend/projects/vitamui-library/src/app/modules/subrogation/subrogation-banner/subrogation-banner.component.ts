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
import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, switchMap } from 'rxjs/operators';

import { AuthService } from '../../auth.service';
import { Subrogation } from '../../models/subrogation/subrogation.interface';
import { AuthUser } from '../../models/user/auth-user.interface';
import { SubrogationService } from '../subrogation.service';
import { DatePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'vitamui-common-subrogation-banner',
  templateUrl: './subrogation-banner.component.html',
  imports: [DatePipe, TranslatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SubrogationBannerComponent {
  authService = inject(AuthService);
  private subrogationService = inject(SubrogationService);

  hidden = signal(false);
  stopped = signal(false);

  private user = toSignal(this.authService.user$.pipe(filter((user: AuthUser) => !!user?.superUser)));
  private currentSubrogation = toSignal(
    this.authService.user$.pipe(
      filter((user: AuthUser) => !!user?.superUser),
      switchMap(() => this.subrogationService.getCurrent().pipe(filter((data) => !!data))),
    ),
  );

  subrogation = computed<Subrogation | undefined>(() => this.currentSubrogation());
  show = computed(() => !!this.subrogation() && !this.stopped());
  endDate = computed(() => (this.subrogation() ? new Date(this.subrogation().date) : undefined));
  surrogateCustomerCode = computed(() => this.subrogation()?.surrogateCustomerCode);
  surrogateCustomerName = computed(() => this.subrogation()?.surrogateCustomerName);
  userEmail = computed(() => this.user()?.email ?? this.authService.user?.email);

  private logoutTimer: ReturnType<typeof setTimeout>;

  constructor() {
    effect(() => {
      const subrogation = this.subrogation();
      if (subrogation && !this.logoutTimer) {
        const ttl = new Date(subrogation.date).getTime() - Date.now();
        this.logoutTimer = setTimeout(
          () => this.authService.logoutAndRedirectToUiForUser(this.authService.user.superUser),
          Math.max(ttl, 0),
        );
      }
    });
  }

  onStopSubrogation() {
    this.subrogationService.decline(this.subrogation().id).subscribe(() => {
      this.stopped.set(true);
      this.authService.logoutAndRedirectToUiForUser(this.authService.user.superUser);
    });
  }
}
