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
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { AccessContract, AccessContractService, diff, Option } from 'vitamui-library';

@Component({
  selector: 'app-access-contract-write-access-tab',
  templateUrl: './access-contract-write-access-tab.component.html',
  styleUrls: ['./access-contract-write-access-tab.component.scss'],
  standalone: false,
})
export class AccessContractWriteAccessTabComponent implements OnInit {
  public usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];

  @Input() set accessContract(accessContract: AccessContract) {
    accessContract.dataObjectVersion = accessContract.dataObjectVersion || [];
    this._accessContract = accessContract;

    this.resetForm(this.accessContract);
  }

  get accessContract(): AccessContract {
    return this._accessContract;
  }

  public form: FormGroup;
  public submitted = false;

  private _accessContract: AccessContract;

  previousValue = (): any => {
    return {
      writingPermission: this._accessContract.writingPermission,
      writingRestrictedDesc: this._accessContract.writingRestrictedDesc,
      everyDataObjectVersion: this._accessContract.everyDataObjectVersion,
      dataObjectVersion: this._accessContract.dataObjectVersion.sort(),
    };
  };

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
  ) {
    this.form = this.formBuilder.group({
      writingPermission: [false],
      writingAuthorizedDesc: [false],
      downloadChoose: ['ALL'],
      everyDataObjectVersion: [true],
      dataObjectVersion: [[], Validators.required],
    });
  }

  ngOnInit() {
    this.form.get('downloadChoose').valueChanges.subscribe((val) => {
      this.form.get('everyDataObjectVersion').setValue(val === 'ALL', { emitEvent: false });

      if (val === 'SELECTION') {
        this.form.controls.dataObjectVersion.setValidators(Validators.required);
      } else {
        this.form.controls.dataObjectVersion.setValidators([]);
        this.form.controls.dataObjectVersion.setValue([]);
      }
      this.form.controls.dataObjectVersion.updateValueAndValidity();
    });

    this.onWritingRestrictedDescChanges();
  }

  onWritingRestrictedDescChanges(): void {
    this.form.get('writingAuthorizedDesc').valueChanges.subscribe((val) => {
      if (val) {
        this.form.get('writingPermission').setValue(true, { emitEvent: false });
      }
    });

    this.form.get('writingPermission').valueChanges.subscribe((val) => {
      if (!val) {
        this.form.get('writingAuthorizedDesc').setValue(false, { emitEvent: false });
      }
    });
  }

  public get unChanged(): boolean {
    return JSON.stringify(diff(this.formDataValue(), this.previousValue())) === '{}';
  }

  public onSubmit(): void {
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

  private formDataValue(): AccessContract {
    const accessContractValue = {
      ...this.form.getRawValue(),
      writingRestrictedDesc: !this.form.getRawValue().writingAuthorizedDesc,
    };
    delete accessContractValue.writingAuthorizedDesc;
    delete accessContractValue.downloadChoose;
    return accessContractValue;
  }

  prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.formDataValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this._accessContract.id, identifier: this._accessContract.identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.accessContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  resetForm(accessContract: AccessContract): void {
    const downloadChoose = accessContract.everyDataObjectVersion
      ? 'ALL'
      : accessContract.dataObjectVersion?.length > 0
        ? 'SELECTION'
        : 'NONE';

    const accessContractForm = {
      ...accessContract,
      downloadChoose: downloadChoose,
      writingAuthorizedDesc: !accessContract.writingRestrictedDesc,
    };
    delete accessContractForm.writingRestrictedDesc;

    this.form.reset(accessContractForm, { emitEvent: false });
  }
}
