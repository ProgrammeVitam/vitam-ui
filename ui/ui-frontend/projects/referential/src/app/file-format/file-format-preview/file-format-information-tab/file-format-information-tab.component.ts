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
import { formatDate, NgIf, AsyncPipe } from '@angular/common';
import { Component, EventEmitter, Inject, Input, LOCALE_ID, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, Observable, Subscription, combineLatest, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { extend, isEmpty, omit } from 'underscore';
import {
  ApplicationId,
  FILE_FORMAT_EXTERNAL_PREFIX,
  FileFormat,
  Role,
  SecurityService,
  VitamuiAutocompleteMultiselectOptions,
  diff,
  VitamUICommonInputComponent,
  VitamUIAutocompleteMultiSelectModule,
} from 'vitamui-library';
import { FileFormatService } from '../../file-format.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-file-format-information-tab',
  templateUrl: './file-format-information-tab.component.html',
  styleUrls: ['./file-format-information-tab.component.scss'],
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    VitamUICommonInputComponent,
    NgIf,
    VitamUIAutocompleteMultiSelectModule,
    AsyncPipe,
    TranslateModule,
  ],
})
export class FileFormatInformationTabComponent {
  private _dateFormat = 'dd/MM/yyyy';
  private _fileFormat: FileFormat;
  private _subscriptions = new Subscription();

  private isInternal = new BehaviorSubject(true);
  private canUpdateFileFormat = new BehaviorSubject<boolean>(false);
  private submitting = new BehaviorSubject<boolean>(false);
  private fileFormats = new BehaviorSubject<FileFormat[]>([]);
  private fileFormats$ = this.fileFormats.asObservable();
  private tenantId = new BehaviorSubject<string>(null);
  private tenantId$ = this.tenantId.asObservable();

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  isInternal$ = this.isInternal.asObservable();
  canUpdateFileFormat$ = this.canUpdateFileFormat.asObservable();
  submitting$ = this.submitting.asObservable();
  canUpdate$: Observable<boolean>;
  fileFormatOptions$: Observable<VitamuiAutocompleteMultiselectOptions>;

  form: FormGroup;

  puidPlaceholder: string;
  previousValue = (): FileFormat => {
    const fileFormat: FileFormat = { ...this._fileFormat };

    fileFormat.mimeType = fileFormat.mimeType || null;
    fileFormat.extensions = fileFormat.extensions || null;
    fileFormat.hasPriorityOverFileFormatIDs = fileFormat.hasPriorityOverFileFormatIDs || null;

    return fileFormat;
  };

  @Input()
  set fileFormat(fileFormat: FileFormat) {
    this._subscriptions.unsubscribe();
    this._subscriptions = new Subscription();
    this._subscriptions.add(
      this.form
        .get('puid')
        .valueChanges.pipe(
          map((puid: string) => !puid.startsWith(FILE_FORMAT_EXTERNAL_PREFIX)),
          tap((isInternal) => (this.puidPlaceholder = `FILE_FORMATS.TAB.INFORMATION.PUID${isInternal ? '' : '_EXTERNAL'}`)),
        )
        .subscribe((isInternal) => this.isInternal.next(isInternal)),
    );
    this.canUpdate$ = combineLatest([this.isInternal$, this.canUpdateFileFormat$]).pipe(
      map(([isInternal, canUpdateFileFormat]: boolean[]) => isInternal || !canUpdateFileFormat),
      tap((disabled) => {
        if (disabled) {
          this.form.controls.name.disable({ onlySelf: true });
          this.form.controls.mimeType.disable({ onlySelf: true });
          this.form.controls.version.disable({ onlySelf: true });
          this.form.controls.extensions.disable({ onlySelf: true });
          this.form.controls.hasPriorityOverFileFormatIDs.disable({ onlySelf: true });
        } else {
          this.form.controls.name.enable({ onlySelf: true });
          this.form.controls.mimeType.enable({ onlySelf: true });
          this.form.controls.version.enable({ onlySelf: true });
          this.form.controls.extensions.enable({ onlySelf: true });
          this.form.controls.hasPriorityOverFileFormatIDs.enable({ onlySelf: true });
        }
      }),
    );
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
      this.form.disable({ emitEvent: false });
    } else if (this.form.disabled) {
      this.form.enable({ emitEvent: false });
      this.form.get('identifier').disable({ emitEvent: false });
    }
  }

  constructor(
    @Inject(LOCALE_ID) private locale: string,
    private formBuilder: FormBuilder,
    private fileFormatService: FileFormatService,
    private route: ActivatedRoute,
    private securityService: SecurityService,
  ) {
    this.form = this.formBuilder.group({
      puid: [{ value: null, disabled: true }, Validators.required],
      name: [{ value: null, disabled: true }, Validators.required],
      mimeType: [{ value: null, disabled: true }],
      version: [{ value: null, disabled: true }, Validators.required],
      extensions: [{ value: null, disabled: true }],
      hasPriorityOverFileFormatIDs: [{ value: null, disabled: true }],
      createdDate: [{ value: null, disabled: true }],
      updateDate: [{ value: null, disabled: true }],
      versionPronom: [{ value: null, disabled: true }],
    });
    this.route.params
      .pipe(
        filter((params: any) => params.tenantIdentifier),
        tap((params: { tenantIdentifier: string }) => this.tenantId.next(params.tenantIdentifier)),
        switchMap((params: { tenantIdentifier: string }) =>
          this.securityService.hasRole(ApplicationId.FILE_FORMATS_APP, +params.tenantIdentifier, Role.ROLE_UPDATE_FILE_FORMATS),
        ),
      )
      .subscribe((canUpdateFileFormat) => this.canUpdateFileFormat.next(canUpdateFileFormat));
    this.tenantId$
      .pipe(switchMap((tenantId) => this.fileFormatService.getAllForTenant(tenantId)))
      .subscribe((fileFormats) => this.fileFormats.next(fileFormats));
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(omit(diff(this.form.getRawValue(), this.previousValue()), 'createdDate', 'updateDate')) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<FileFormat> {
    const previousValue = this.previousValue();
    const { id, puid } = previousValue;
    const patchData = diff(this.form.getRawValue(), previousValue);

    // The extensions property must be an array of string, not a string
    if (patchData.extensions) patchData.extensions = patchData.extensions.replace(/\s/g, '').split(',');

    return of(patchData).pipe(
      filter((data) => !isEmpty(data)),
      map((data) => extend({ id, puid }, data)),
      map((data) => omit(data, 'createdDate', 'updateDate')),
      switchMap((formData) => this.fileFormatService.patch(formData).pipe(catchError(() => of(null)))),
    );
  }

  onSubmit() {
    if (this.form.pending || this.form.invalid) return;

    this.submitting.next(true);
    this.prepareSubmit()
      .pipe(
        switchMap(() => this.fileFormatService.get(this._fileFormat.puid)),
        tap(
          () => this.submitting.next(false),
          () => this.submitting.next(false),
        ),
      )
      .subscribe((response) => {
        this.fileFormat = response;
        this.fileFormatService.updated.next(this.fileFormat);
      });
  }

  resetForm(fileFormat: FileFormat) {
    this.form.reset({
      ...fileFormat,
      createdDate: fileFormat.createdDate ? formatDate(fileFormat.createdDate, this._dateFormat, this.locale) : undefined,
      updateDate: fileFormat.updateDate ? formatDate(fileFormat.updateDate, this._dateFormat, this.locale) : undefined,
    });

    if (this.isInternal.value) return;

    this.fileFormatOptions$ = this.fileFormats$.pipe(
      map((ffs) => ffs.filter((ff) => ff.puid !== fileFormat.puid)),
      map((fileFormats: FileFormat[]) => ({
        options: fileFormats.map((ff) => ({
          label: `${ff.puid}-${ff.name}`,
          key: ff.puid,
        })),
      })),
    );
  }
}
