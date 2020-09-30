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
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { ApplicationId, ApplicationInfo, ApplicationService, ConfirmDialogService } from 'ui-frontend-common';

const INFINITE_SCROLL_FAKE_DELAY_MS = 1500;

@Component({
  selector: 'demo-components',
  templateUrl: './components.component.html',
  styleUrls: ['./components.component.scss']
})
export class ComponentsComponent implements OnInit {

  @ViewChild('confirmDialogTemplate', { static: true }) confirmDialogTemplate: TemplateRef<ComponentsComponent>;

  appId = ApplicationId.ACCOUNTS_APP; // FIXME: Why this app id, can we replace easy ?

  applications: ApplicationInfo;

  vitamuiInputValue: string;
  vitamUIInputPositiveNumberValue: number;
  vitamuiEmailValue = 'toto@vitamui.com';
  vitamuiTextValue = 'Some text value';
  vitamuiTextareaValue = 'Some text value';
  vitamuiSelectValue = 'Value 1';
  vitamuiToggleGroupValue = 'Value 2';
  customers = [
    { value: '0001', label: 'Customer 1' },
    { value: '0002', label: 'Customer 2' },
    { value: '0003', label: 'Customer 3' },
    { value: '0004', label: 'Customer 4' },
    { value: '0005', label: 'Customer 5' },
    { value: '0006', label: 'Customer 6' },
  ];
  infiniteValues: number[] = [];
  infiniteScrollDisabled = false;
  confirmResult: boolean;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private appService: ApplicationService,
    private confirmDialogService: ConfirmDialogService
  ) { }

  ngOnInit() {
    this.appService.list().subscribe((apps) => this.applications = apps);
  }

  onTenantSelect(tenantIdentifier: number) {
    if (this.route.snapshot.paramMap.has('tenantIdentifier')) {
      this.router.navigate(['../', tenantIdentifier], { relativeTo: this.route });
    } else {
      this.router.navigate(['components-demo', 'angular', 'tenant', tenantIdentifier]);
    }
  }

  onCustomerSelect(customerCode: string) {
    if (this.route.snapshot.paramMap.has('customerCode')) {
      this.router.navigate(['../', customerCode], { relativeTo: this.route });
    } else {
      this.router.navigate(['components-demo', 'angular', 'customer', customerCode]);
    }
  }

  onScroll() {
    this.infiniteScrollDisabled = true;
    setTimeout(
      () => {
        this.infiniteScrollDisabled = false;
        this.infiniteValues = this.infiniteValues.concat([1, 1, 1, 1, 1]);
      },
      INFINITE_SCROLL_FAKE_DELAY_MS
    );
  }

  openConfirmDialog() {
    this.confirmDialogService.confirm(this.confirmDialogTemplate).subscribe(() => this.confirmResult = true);
  }

  onKeyPress(event: KeyboardEvent): boolean {
    // tslint:disable-next-line: deprecation
    const charCode = (event.which) ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
      return false;
    }
    return true;
  }
}
