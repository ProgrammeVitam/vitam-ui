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
import { Component, effect, input, signal } from '@angular/core';
import { disabled, email, form, FormField, pattern, required } from '@angular/forms/signals';

import type { Account } from '../../models/account/account.interface';
import { SlideToggleComponent } from '../../../../lib/components/slide-toggle/slide-toggle.component';
import { TranslatePipe } from '@ngx-translate/core';

interface AccountInformationForm {
  id: Account['id'] | null;
  firstname: string | null;
  lastname: string | null;
  email: string | null;
  level: string | null;
  otp: boolean | null;
  mobile: string | null;
  phone: string | null;
  address: Account['address'] | null;
  type: string | null;
  profileGroup: Account['profileGroup'] | null;
}

const PHONE_PATTERN = /^[+]{1}[0-9]{11,12}$/;

function toFormModel(account?: Partial<Account>): AccountInformationForm {
  return {
    id: account?.id ?? null,
    firstname: account?.firstname ?? null,
    lastname: account?.lastname ?? null,
    email: account?.email ?? null,
    level: account?.level ?? null,
    otp: account?.otp ?? null,
    mobile: account?.mobile ?? null,
    phone: account?.phone ?? null,
    address: account?.address ?? null,
    type: account?.type ?? null,
    profileGroup: account?.profileGroup ?? null,
  };
}

@Component({
  selector: 'vitamui-common-account-information-tab',
  templateUrl: './account-information-tab.component.html',
  styleUrls: ['./account-information-tab.component.scss'],
  imports: [FormField, SlideToggleComponent, TranslatePipe],
})
export class AccountInformationTabComponent {
  private readonly model = signal<AccountInformationForm>(toFormModel());

  public readonly form = form(this.model, (path) => {
    // Read-only form: every field but the id is disabled
    disabled(path.firstname);
    disabled(path.lastname);
    disabled(path.email);
    disabled(path.level);
    disabled(path.otp);
    disabled(path.mobile);
    disabled(path.phone);
    disabled(path.address);
    disabled(path.type);
    disabled(path.profileGroup);
    required(path.firstname);
    required(path.lastname);
    required(path.email);
    email(path.email);
    pattern(path.mobile, PHONE_PATTERN);
    pattern(path.phone, PHONE_PATTERN);
    required(path.address);
  });

  public language: string;

  public account = input<Account | null>(null);

  constructor() {
    effect(() => {
      const acc = this.account();
      if (acc?.userInfo) {
        this.language = acc.userInfo.language;
      }
      if (acc) {
        this.resetForm(acc);
      }
    });
  }

  private resetForm(account: Account) {
    this.form().reset(toFormModel(account));
  }
}
