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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import { AccessContract, diff, Option, AccessContractService } from 'vitamui-library';

@Component({
  selector: 'app-access-contract-write-access-tab',
  templateUrl: './access-contract-write-access-tab.component.html',
  styleUrls: ['./access-contract-write-access-tab.component.scss'],
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

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  public form: FormGroup;
  public submited = false;

  private _accessContract: AccessContract;

  previousValue = (): AccessContract => {
    return this._accessContract;
  };

  constructor(
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
  ) {
    this.form = this.formBuilder.group(
      {
        writingPermission: [false],
        downloadChoose: ['ALL'],
        everyDataObjectVersion: [true],
        dataObjectVersion: [new Array<string>()],
        writingAuthorizedDesc: [false],
      },
      {
        validators: [this.validator()],
      },
    );
  }

  ngOnInit() {
    this.form.get('downloadChoose').valueChanges.subscribe((val) => {
      this.form.get('everyDataObjectVersion').setValue(val === 'ALL', { emitEvent: false });

      if (val !== 'SELECTION') {
        this.form.get('dataObjectVersion').setValue([], { emitEvent: false });
      }
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

  private validator(): ValidatorFn {
    return (form: FormGroup): ValidationErrors | null => {
      const downloadChoose = form.get('downloadChoose').value;
      const dataObjectVersion = form.get('dataObjectVersion').value;
      if (downloadChoose === 'SELECTION' && dataObjectVersion.length === 0) {
        return { dataObjectVersion: true };
      }
      return null;
    };
  }

  public unChanged(): boolean {
    const unchanged = JSON.stringify(diff(this.formDataValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);

    return unchanged;
  }

  public onSubmit(): void {
    this.submited = true;

    this.prepareSubmit().subscribe(
      () => {
        this.accessContractService.get(this._accessContract.identifier).subscribe((response) => {
          this.submited = false;
          this.accessContract = response;
        });
      },
      () => {
        this.submited = false;
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

  private prepareSubmit(): Observable<AccessContract> {
    return of(diff(this.formDataValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string; [key: string]: any }) =>
        this.accessContractService.patch(formData).pipe(catchError(() => of(null))),
      ),
    );
  }

  private resetForm(accessContract: AccessContract): void {
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
