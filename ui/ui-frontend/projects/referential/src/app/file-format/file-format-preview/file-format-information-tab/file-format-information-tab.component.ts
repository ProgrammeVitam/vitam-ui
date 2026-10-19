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
import { AsyncPipe, formatDate } from '@angular/common';
import { Component, computed, inject, LOCALE_ID, output, input, model, OnChanges, signal, SimpleChanges } from '@angular/core';
import { disabled, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { extend, isEmpty, omit } from 'underscore';
import {
  ApplicationId,
  diff,
  FILE_FORMAT_EXTERNAL_PREFIX,
  FileFormat,
  InputComponent,
  Role,
  SecurityService,
  SelectComponent,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { FileFormatService } from '../../file-format.service';
import { TranslatePipe } from '@ngx-translate/core';

interface FileFormatInformationForm {
  puid: string;
  name: string;
  mimeType: string;
  version: string;
  extensions: string[];
  hasPriorityOverFileFormatIDs: string[];
  createdDate: string;
  updateDate: string;
  versionPronom: string;
}

@Component({
  selector: 'app-file-format-information-tab',
  templateUrl: './file-format-information-tab.component.html',
  styleUrls: ['./file-format-information-tab.component.scss'],
  imports: [FormRoot, FormField, InputComponent, SelectComponent, AsyncPipe, TranslatePipe],
})
export class FileFormatInformationTabComponent implements OnChanges {
  private readonly locale = inject(LOCALE_ID);
  private readonly fileFormatService = inject(FileFormatService);
  private readonly route = inject(ActivatedRoute);
  private readonly securityService = inject(SecurityService);

  private readonly _dateFormat = 'dd/MM/yyyy';

  private readonly canUpdateFileFormat = signal(false);
  private readonly fileFormats$ = new BehaviorSubject<FileFormat[]>([]);
  private readonly tenantId$ = new BehaviorSubject<string>(null);

  readonly updated = output<boolean>();

  readonly submitting = signal(false);
  fileFormatOptions$: Observable<VitamuiSelectOptions>;

  private readonly formModel = signal<FileFormatInformationForm>({
    puid: null,
    name: null,
    mimeType: null,
    version: null,
    extensions: null,
    hasPriorityOverFileFormatIDs: null,
    createdDate: null,
    updateDate: null,
    versionPronom: null,
  });
  /** A file format is internal when its PUID does not start with the external prefix */
  readonly isInternal = computed(() => {
    const puid = this.formModel().puid;
    return puid === null || !puid.startsWith(FILE_FORMAT_EXTERNAL_PREFIX);
  });
  readonly puidPlaceholder = computed(() => `FILE_FORMATS.TAB.INFORMATION.PUID${this.isInternal() ? '' : '_EXTERNAL'}`);
  /** Internal file formats cannot be updated */
  readonly updateDisabled = computed(() => this.isInternal() || !this.canUpdateFileFormat());

  readonly form = form(
    this.formModel,
    (path) => {
      disabled(path, () => !!this.readOnly());
      disabled(path.puid);
      disabled(path.createdDate);
      disabled(path.updateDate);
      disabled(path.versionPronom);
      disabled(path.name, () => this.updateDisabled());
      disabled(path.mimeType, () => this.updateDisabled());
      disabled(path.version, () => this.updateDisabled());
      disabled(path.extensions, () => this.updateDisabled());
      disabled(path.hasPriorityOverFileFormatIDs, () => this.updateDisabled());
      required(path.name);
      required(path.version);
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );

  previousValue = (): FileFormat => {
    const fileFormat: FileFormat = { ...this.fileFormat() };

    fileFormat.mimeType = fileFormat.mimeType || null;
    fileFormat.extensions = fileFormat.extensions || null;
    fileFormat.hasPriorityOverFileFormatIDs = fileFormat.hasPriorityOverFileFormatIDs || null;

    return fileFormat;
  };

  readonly fileFormat = model<FileFormat>(undefined);

  readonly readOnly = input<boolean>(undefined);

  constructor() {
    this.route.params
      .pipe(
        filter((params: any) => params.tenantIdentifier),
        tap((params: { tenantIdentifier: string }) => this.tenantId$.next(params.tenantIdentifier)),
        switchMap((params: { tenantIdentifier: string }) =>
          this.securityService.hasRole$(ApplicationId.FILE_FORMATS_APP, Role.ROLE_UPDATE_FILE_FORMATS, +params.tenantIdentifier),
        ),
      )
      .subscribe((canUpdateFileFormat) => this.canUpdateFileFormat.set(canUpdateFileFormat));
    this.tenantId$
      .pipe(switchMap((tenantId) => this.fileFormatService.getAllForTenant(tenantId)))
      .subscribe((fileFormats) => this.fileFormats$.next(fileFormats));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['fileFormat']) {
      this.setFileFormat(this.fileFormat());
    }
  }

  unchanged(): boolean {
    const unchanged = JSON.stringify(omit(diff(this.formModel(), this.previousValue()), 'createdDate', 'updateDate')) === '{}';
    this.updated.emit(!unchanged);
    return unchanged;
  }

  prepareSubmit(): Observable<FileFormat> {
    const previousValue = this.previousValue();
    const { id, puid } = previousValue;
    const patchData = diff(this.formModel(), previousValue);

    return of(patchData).pipe(
      filter((data) => !isEmpty(data)),
      map((data) => extend({ id, puid }, data)),
      map((data) => omit(data, 'createdDate', 'updateDate')),
      switchMap((formData) => this.fileFormatService.patch(formData).pipe(catchError(() => of(null)))),
    );
  }

  onSubmit() {
    if (this.form().pending() || this.form().invalid()) return;

    this.submitting.set(true);
    this.prepareSubmit()
      .pipe(
        switchMap(() => this.fileFormatService.get(this.fileFormat().puid)),
        tap({
          next: () => this.submitting.set(false),
          error: () => this.submitting.set(false),
        }),
      )
      .subscribe((response) => {
        this.setFileFormat(response);
        this.fileFormatService.updated.next(this.fileFormat());
      });
  }

  private setFileFormat(fileFormat: FileFormat) {
    this.fileFormat.set(fileFormat);
    this.resetForm(this.fileFormat());
    this.updated.emit(false);
  }

  resetForm(fileFormat: FileFormat) {
    // Like reactive forms reset, the missing values are reset to null
    this.form().reset({
      puid: fileFormat.puid ?? null,
      name: fileFormat.name ?? null,
      mimeType: fileFormat.mimeType ?? null,
      version: fileFormat.version ?? null,
      extensions: fileFormat.extensions ?? null,
      hasPriorityOverFileFormatIDs: fileFormat.hasPriorityOverFileFormatIDs ?? null,
      createdDate: fileFormat.createdDate ? formatDate(fileFormat.createdDate, this._dateFormat, this.locale) : null,
      updateDate: fileFormat.updateDate ? formatDate(fileFormat.updateDate, this._dateFormat, this.locale) : null,
      versionPronom: fileFormat.versionPronom ?? null,
    });

    if (this.isInternal()) return;

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
