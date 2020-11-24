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
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {FILE_FORMAT_EXTERNAL_PREFIX, FileFormat} from 'projects/vitamui-library/src/public-api';
import {Observable, of} from 'rxjs';
import {catchError, filter, map, switchMap} from 'rxjs/operators';
import {diff} from 'ui-frontend-common';
import {extend, isEmpty} from 'underscore';

import {FileFormatService} from '../../file-format.service';


@Component({
  selector: 'app-file-format-information-tab',
  templateUrl: './file-format-information-tab.component.html',
  styleUrls: ['./file-format-information-tab.component.scss']
})
export class FileFormatInformationTabComponent {

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  form: FormGroup;

  submited = false;

  ruleFilter = new FormControl();

  // tslint:disable-next-line:variable-name
  private _fileFormat: FileFormat;

  previousValue = (): FileFormat => {
    return this._fileFormat;
  }

  @Input()
  set fileFormat(fileFormat: FileFormat) {
    this._fileFormat = fileFormat;
    this.resetForm(this.fileFormat);
    this.updated.emit(false);
  }

  get fileFormat(): FileFormat {
    return this._fileFormat;
  }

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly && this.form.enabled) {
      this.form.disable({emitEvent: false});
    } else if (this.form.disabled) {
      this.form.enable({emitEvent: false});
      this.form.get('identifier').disable({emitEvent: false});
    }
  }

  constructor(
    private formBuilder: FormBuilder,
    private fileFormatService: FileFormatService
  ) {
    this.form = this.formBuilder.group({
      puid: [null, Validators.required],
      name: [null, Validators.required],
      mimeType: [null],
      version: [null, Validators.required],
      versionPronom: [null],
      extensions: [null]
    });
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(diff(this.form.getRawValue(), this.previousValue())) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  isInvalid(): boolean {
    return this.form.get('name').invalid || this.form.get('name').pending ||
      this.form.get('mimeType').invalid || this.form.get('mimeType').pending ||
      this.form.get('version').invalid || this.form.get('version').pending ||
      this.form.get('extensions').invalid || this.form.get('extensions').pending;
  }

  isInternal(): boolean {
    return !this.form.getRawValue().puid.startsWith(FILE_FORMAT_EXTERNAL_PREFIX);
  }

  prepareSubmit(): Observable<FileFormat> {
    return of(diff(this.form.getRawValue(), this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({id: this.previousValue().id, puid: this.previousValue().puid}, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => this.fileFormatService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    if (this.isInvalid()) {
      return;
    }

    this.prepareSubmit().subscribe(() => {
      this.fileFormatService.get(this._fileFormat.puid).subscribe(
        response => {
          this.submited = false;
          this.fileFormat = response;
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  resetForm(fileFormat: FileFormat) {
    this.form.reset(fileFormat, {emitEvent: false});
  }
}
