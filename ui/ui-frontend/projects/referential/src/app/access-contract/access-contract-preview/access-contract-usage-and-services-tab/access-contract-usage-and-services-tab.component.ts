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
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { AccessContract, Agency, Option, VitamuiAutocompleteMultiselectOptions, diff } from 'vitamui-library';
import { AgencyService } from '../../../agency/agency.service';
import { AccessContractService } from '../../access-contract.service';

@Component({
  selector: 'app-access-contract-usage-and-services-tab',
  templateUrl: './access-contract-usage-and-services-tab.component.html',
  styleUrls: ['./access-contract-usage-and-services-tab.component.scss'],
})
export class AccessContractUsageAndServicesTabComponent {
  @Input() set accessContract(accessContract: AccessContract) {
    this.setAccessContract(accessContract);
  }

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  @Input() set isTabActive(isActive: boolean) {
    if (isActive) {
      this.resetForm(this.accessContract);
    }
  }

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() isFormValid: EventEmitter<boolean> = new EventEmitter<boolean>();

  public form: FormGroup;
  public submitted = false;
  public originatingAgenciesOptions: VitamuiAutocompleteMultiselectOptions;
  public usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];

  // tslint:disable-next-line:variable-name
  private _accessContract: AccessContract;

  previousValue = (): AccessContract => {
    return this._accessContract;
  };

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
    private agencyService: AgencyService,
  ) {
    this.initForm();
  }

  public unChanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);

    return unchanged;
  }

  public isInvalid(): boolean {
    const isInvalid = this.form.invalid;
    this.isFormValid.emit(!isInvalid);
    return isInvalid;
  }

  public onSubmit() {
    this.submitted = true;
    this.prepareSubmit().subscribe(
      () => {
        this.accessContractService.get(this._accessContract.identifier).subscribe((response) => {
          this.submitted = false;
          this.accessContract = response;
        });
      },
      () => {
        this.submitted = false;
      },
    );
  }

  private prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.accessContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  private setAccessContract(accessContract: AccessContract): void {
    if (!accessContract.originatingAgencies) {
      accessContract.originatingAgencies = [];
    }

    if (!accessContract.dataObjectVersion) {
      accessContract.dataObjectVersion = [];
    }

    this._accessContract = { ...accessContract, originatingAgencies: accessContract.originatingAgencies?.sort() };

    this.agencyService
      .getAll()
      .pipe(
        map((agencies: Agency[]) => {
          const options: Option[] = agencies.map((agency) => ({ label: agency.identifier + ' - ' + agency.name, key: agency.identifier }));
          return { options };
        }),
        tap((options: VitamuiAutocompleteMultiselectOptions) => (this.originatingAgenciesOptions = options)),
      )
      .subscribe(() => {
        this.form.controls.originatingAgencies.setValue(accessContract.originatingAgencies);
        this.form.controls.dataObjectVersion.setValue(accessContract.dataObjectVersion);
      });

    this.resetForm(this._accessContract);
  }

  private initForm(): void {
    this.form = this.formBuilder.group({
      everyOriginatingAgency: [true, Validators.required],
      originatingAgencies: [[]],
      everyDataObjectVersion: [true, Validators.required],
      dataObjectVersion: [[]],
    });

    this.form.controls.everyOriginatingAgency.valueChanges.subscribe((allAgencies) => {
      if (allAgencies) {
        // remove required validator on originatingAgencySelect
        this.form.controls.originatingAgencies.setValidators([]);
        this.form.controls.originatingAgencies.setValue([]);
        this.form.controls.originatingAgencies.updateValueAndValidity();
      } else {
        // add required validator on originatingAgencySelect
        this.form.controls.originatingAgencies.setValidators(Validators.required);
        this.form.controls.originatingAgencies.markAllAsTouched();
        this.form.controls.originatingAgencies.updateValueAndValidity();
      }
    });

    this.form.controls.everyDataObjectVersion.valueChanges.subscribe((allUsage) => {
      if (allUsage) {
        // remove required validator on usageSelect
        this.form.controls.dataObjectVersion.setValidators([]);
        this.form.controls.dataObjectVersion.setValue([]);
        this.form.controls.dataObjectVersion.updateValueAndValidity();
      } else {
        // add required validator on usageSelect
        this.form.controls.dataObjectVersion.setValidators(Validators.required);
        this.form.controls.dataObjectVersion.markAllAsTouched();
        this.form.controls.dataObjectVersion.updateValueAndValidity();
      }
    });
  }

  private resetForm(accessContract: AccessContract): void {
    if (!this.form) {
      this.initForm();
    }

    if (accessContract.everyOriginatingAgency) {
      // remove required validator on originatingAgencySelect
      this.form.controls.originatingAgencies.setValidators([]);
    } else {
      // add required validator on originatingAgencySelect
      this.form.controls.originatingAgencies.setValidators(Validators.required);
    }

    if (accessContract.everyDataObjectVersion) {
      // remove required validator on usageSelect
      this.form.controls.dataObjectVersion.setValidators([]);
    } else {
      // add required validator on usageSelect
      this.form.controls.dataObjectVersion.setValidators(Validators.required);
    }

    this.form.reset(accessContract, { emitEvent: false });
  }
}
