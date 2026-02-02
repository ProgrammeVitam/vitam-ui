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
import {
  Component,
  ContentChild,
  ElementRef,
  EventEmitter,
  forwardRef,
  Injector,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { DragAndDropDirective } from '../../directives/drag-and-drop/drag-and-drop.directive';
import { TranslatePipe } from '@ngx-translate/core';
import { I18nPluralPipe, NgTemplateOutlet } from '@angular/common';
import { PipesModule } from '../../pipes/pipes.module';
import { DisplayFile } from './display-file/display-file.interface';
import { CustomFile } from '../../../../lib/models/custom-file';
import { AbstractControl, FormsModule, NG_VALUE_ACCESSOR, ValidationErrors } from '@angular/forms';
import { FormErrorsComponent } from '../../../../lib/components/form-errors/form-errors.component';
import { BytesPipe } from '../../pipes';
import { AbstractFormInputDirective } from '../../../../lib/components/abstract-form-input.directive';
import { DisplayFileComponent } from './display-file/display-file.component';

export const FILE_SELECTOR_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => FileSelectorComponent),
  multi: true,
};

export function readFileContent(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = (event) => resolve(event.target?.result as string);
    reader.onerror = (error) => reject(error);
    reader.readAsText(file);
  });
}

export interface FileValidationErrors {
  /** Errors that will be displayed on the file */
  fileErrors: ValidationErrors;
  /** Errors that will be displayed on the control (global error) */
  controlErrors: ValidationErrors;
}

type FileValidatorFunction = (file: File) => Promise<FileValidationErrors | null>;

@Component({
  selector: 'vitamui-file-selector',
  templateUrl: './file-selector.component.html',
  styleUrl: './file-selector.component.scss',
  imports: [
    FormsModule,
    DragAndDropDirective,
    TranslatePipe,
    PipesModule,
    NgTemplateOutlet,
    I18nPluralPipe,
    FormErrorsComponent,
    DisplayFileComponent,
  ],
  providers: [FILE_SELECTOR_VALUE_ACCESSOR, BytesPipe],
})
export class FileSelectorComponent extends AbstractFormInputDirective implements OnInit {
  /**
   * Allowed extensions. Ex: ['.json', '.rng']
   */
  @Input() extensions?: string[];
  /** Allows to select multiple files. Automatically set to true in directoryMode **/
  @Input() set multiple(multiple: boolean) {
    this.#multiple = multiple;
  }
  get multiple() {
    return this.#multiple || this.directoryMode;
  }
  #multiple = false;
  @Input() directoryMode = false;
  @Input() maxSizeInBytes: number;
  /** Extra information to display between picker links and selected files */
  @Input({ transform: (v: string | string[]) => (v instanceof Array ? v : [v]) }) information: string[];
  @Input() fileValidators?: FileValidatorFunction | FileValidatorFunction[];

  @ContentChild('fileList') fileList: TemplateRef<any>;
  @ContentChild('content') content: TemplateRef<any>;

  @Output() filesChanged = new EventEmitter<File[]>();

  @ViewChild('fileSelector') fileSelector: ElementRef;
  @ViewChild('directorySelector') directorySelector: ElementRef;

  displayFiles: DisplayFile[] = [];

  format: { [k: string]: string } = {
    '=1': 'FILE_SELECTOR.ALLOWED_FORMATS_SINGULAR',
    other: 'FILE_SELECTOR.ALLOWED_FORMATS_PLURAL',
  };

  constructor(
    injector: Injector,
    private bytesPipe: BytesPipe,
  ) {
    super(injector);
  }

  ngOnInit() {
    super.ngOnInit();

    this.control.addValidators([this.maxSizeInBytesValidator, this.maxFilesValidator]);
    this.control.addAsyncValidators(async () => {
      // We're returning the promise that is created in computeErrors. We do NOT want to run computeErrors directly here as otherwise, it would only run if SYNC validators have no error (and we want to have all errors in that case)
      return await this.globalErrors;
    });
    this.control.valueChanges.subscribe((files) => this.filesChanged.emit(files));
  }

  // This is a promise that will resolve to the global errors after custom validation is run
  private globalErrors: Promise<ValidationErrors>;

  private isInitialization = true;

  writeValue(): void {
    // Only add files to init the component from the form control value, otherwise it would be triggered after calling control.setValue
    if (this.isInitialization) {
      this.isInitialization = false;
      this.addDisplayFiles(this.control.value);
      return;
    }
  }

  hasErrors(): boolean {
    return this.control.touched && this.control.invalid;
  }

  get displayMessageAndLinks() {
    return this.multiple || this.directoryMode || !this.displayFiles?.length;
  }

  async handleFilesSelection(files: FileList | File[]) {
    this.control.markAsTouched();

    await this.updateFiles(files);

    this.resetInput();
  }

  private async updateFiles(files: FileList | File[]) {
    // 1. We MUST first add display files
    await this.addDisplayFiles(files);
    // 2. Then add them to the control. Because computeErrors is run on displayFiles and must be run before asyncValidation is triggered
    this.control.setValue([...(this.control.value || []), ...Array.from(files)]);
  }

  private async addDisplayFiles(newFiles: FileList | File[]) {
    this.displayFiles = [...this.displayFiles, ...(await this.getRootElements(newFiles))];

    await this.computeErrors();
  }

  private async computeErrors(): Promise<ValidationErrors | null> {
    // We store a promise that will resolve to global errors in order to use it in async validation
    this.globalErrors = new Promise(async (resolve) => {
      const globalErrors = [];
      // Compute errors on root files
      for (const displayFile of this.displayFiles) {
        const errors = await this.computeDisplayFileErrors(displayFile);
        displayFile.errors = errors.fileErrors;
        globalErrors.push(...errors.globalErrors);
      }

      // Compute duplication errors
      globalErrors.push(...this.computeDuplicationError());

      this.sortDisplayFiles();

      resolve(this.resumeGlobalErrors(globalErrors));
    });

    return this.globalErrors;
  }

  /**
   * We sort to display errors first, then by type (directory, file), then by name ASC
   */
  private sortDisplayFiles() {
    this.displayFiles.sort((f1, f2) => {
      const f1HasErrors = !!f1.errors && !!Object.keys(f1.errors);
      const f2HasErrors = !!f2.errors && !!Object.keys(f2.errors);
      return f1HasErrors === f2HasErrors
        ? f1.directory === f2.directory
          ? f1.name.localeCompare(f2.name)
          : f1.directory
            ? -1
            : 1
        : f1HasErrors
          ? -1
          : 1;
    });
  }

  private computeDuplicationError(): any[] {
    const fileNameCounts = this.displayFiles
      .map((df) => df.name.toLowerCase())
      .reduce(
        (acc, name) => {
          acc[name] = (acc[name] || 0) + 1;
          return acc;
        },
        {} as Record<string, number>,
      );
    return this.displayFiles
      .filter((displayFile) => fileNameCounts[displayFile.name.toLowerCase()] > 1)
      .map((displayFile) => {
        displayFile.errors = {
          ...(displayFile.errors || {}),
          [displayFile.directory ? 'duplicatedDirectory' : 'duplicatedFile']: true,
        };
        return { invalidFiles: { files: `<li>${displayFile.name}</li>` } };
      });
  }

  private async computeDisplayFileErrors(displayFile: DisplayFile): Promise<{ fileErrors: ValidationErrors; globalErrors: any[] }> {
    if (displayFile.directory) {
      return this.computeDirectoryErrors(displayFile);
    } else {
      return this.computeFileErrors(displayFile.files[0]);
    }
  }

  private async computeDirectoryErrors(displayFile: DisplayFile): Promise<{ fileErrors: ValidationErrors; globalErrors: any[] }> {
    console.group(`Computing errors on directory "${displayFile.name}"`);

    const validators: ((displayFile: DisplayFile) => Promise<FileValidationErrors | undefined>)[] = [this.directoryForbiddenValidator];
    const errors = await this.runValidators(validators, displayFile);

    console.groupEnd();
    return errors;
  }

  private async computeFileErrors(file: File): Promise<{ fileErrors: ValidationErrors; globalErrors: any[] }> {
    console.group(`Computing errors on file "${file.name}"`);

    const customFileValidators = this.fileValidators
      ? this.fileValidators instanceof Array
        ? this.fileValidators
        : [this.fileValidators]
      : [];
    const validators: ((file: File) => Promise<FileValidationErrors | undefined>)[] = [
      this.fileExtensionValidator,
      ...customFileValidators,
    ];
    const errors = await this.runValidators(validators, file);

    console.groupEnd();
    return errors;
  }

  private async runValidators<T extends File | DisplayFile>(
    validators: ((element: T) => Promise<FileValidationErrors | undefined>)[],
    element: T,
  ): Promise<{ fileErrors: ValidationErrors; globalErrors: any[] }> {
    let errors: ValidationErrors = {};
    const globalErrors: any[] = [];
    for (const validator of validators) {
      const validationErrors = await validator.call(this, element);
      const fileErrors = validationErrors?.fileErrors || {};
      const controlErrors = validationErrors?.controlErrors || {};

      // We're adding errors to the file
      errors = { ...errors, ...fileErrors };

      // We're adding global errors
      if (controlErrors) {
        globalErrors.push(controlErrors);
      }
    }
    return { fileErrors: Object.keys(errors).length > 0 ? errors : undefined, globalErrors };
  }

  private resumeGlobalErrors(globalErrors: any[]): ValidationErrors {
    let errors: ValidationErrors = {};
    globalErrors.forEach((globalError) => {
      for (let entry of Object.entries(globalError)) {
        const errorKey = entry[0];
        let errorDetail = entry[1];

        const alreadyExistingErrorDetail = errors[errorKey];

        // We're combining errors if other files have generated the same error
        if (errorDetail instanceof Object && alreadyExistingErrorDetail instanceof Object) {
          errorDetail = Object.fromEntries(
            Object.entries(errorDetail).map(([key, value]) => {
              if (typeof value === 'string') {
                return [key, (alreadyExistingErrorDetail[key] || '') + value];
              }
              return [key, value];
            }),
          );
        }

        errors = {
          ...errors,
          [errorKey]: errorDetail,
        };
      }
    });
    return errors;
  }

  private async fileExtensionValidator(file: File): Promise<FileValidationErrors | undefined> {
    if (!this.extensions) return undefined;
    if (!this.extensions.some((ext) => file.name.toLowerCase().endsWith(ext.toLowerCase()))) {
      console.warn(`Invalid extension. Expected: ${this.extensions.join(', ')}`);
      return {
        fileErrors: { invalidExtension: { extensions: this.extensions.join(', ') } },
        controlErrors: {
          invalidFiles: { files: `<li>${file.name}</li>` },
        },
      };
    }
    console.log(`Valid extension`);
    return undefined;
  }

  private async directoryForbiddenValidator(displayFile: DisplayFile): Promise<FileValidationErrors | undefined> {
    if (this.directoryMode) return undefined;
    console.warn(`Directory forbidden.`);
    return {
      fileErrors: { directoryForbidden: true },
      controlErrors: {
        invalidFiles: { files: `<li>${displayFile.name}</li>` },
      },
    };
  }

  openFileSelectorOSDialog() {
    this.fileSelector.nativeElement.click();
  }

  openDirectorySelectorOSDialog() {
    this.directorySelector.nativeElement.click();
  }

  async removeFile(displayFile: DisplayFile) {
    // 1. We MUST first remove display files
    this.displayFiles = this.displayFiles.filter((df) => df !== displayFile);
    // 2. Then compute errors (because it's based on display files)
    await this.computeErrors();
    // 3. And finally add files to the control. Because computeErrors must be run before asyncValidation is triggered
    this.control.setValue(this.control.value.filter((file: CustomFile) => !displayFile.files.includes(file)));
  }

  /**
   * Reset the value to allow a new "change" event.
   */
  private resetInput(): void {
    if (this.fileSelector) this.fileSelector.nativeElement.value = '';
    if (this.directorySelector) this.directorySelector.nativeElement.value = '';
  }

  private async getRootElements(files: FileList | File[]): Promise<DisplayFile[]> {
    if (!files || files.length === 0) return [];

    const rootElementsMap = new Map<string, DisplayFile>();

    for (const file of Array.from(files) as CustomFile[]) {
      const path = this.getPath(file);
      const rootPath = path.split('/')[0];
      const existing = rootElementsMap.get(rootPath);

      rootElementsMap.set(rootPath, {
        name: rootPath,
        size: (existing?.size || 0) + (file?.size || 0),
        directory: existing?.directory || file?.isDirectory || rootPath !== path,
        files: [...(existing?.files || []), file],
      });
    }

    return Array.from(rootElementsMap.values());
  }

  /** We need the file path, so we use the webkitRelativePath attribute when loading a folder via the native HTML file selector,
   the relativePath attribute in the case of drag & drop, and the name attribute when uploading a file. */
  private getPath(file: CustomFile) {
    return file?.webkitRelativePath || file?.relativePath || file?.name;
  }

  maxSizeInBytesValidator = (control: AbstractControl<CustomFile[]>): ValidationErrors => {
    if (!this.maxSizeInBytes) return null;
    const size = (control.value || []).reduce((acc: number, file: CustomFile) => acc + (file.size || 0), 0);
    if (size > this.maxSizeInBytes) {
      console.warn(`Maximum size exceeded: ${size}/${this.maxSizeInBytes}`);
      return {
        maxSizeInBytes: {
          size: this.bytesPipe.transform(size),
          maxSize: this.bytesPipe.transform(this.maxSizeInBytes),
        },
      };
    }
    console.debug(`Maximum size respected: ${size}/${this.maxSizeInBytes}`);
    return null;
  };

  maxFilesValidator = (control: AbstractControl<CustomFile[]>): ValidationErrors => {
    const maxFiles = this.multiple ? undefined : 1;
    if (!maxFiles) return null;
    const nbFiles = (control.value || []).length;
    if (nbFiles > maxFiles) {
      console.warn(`Maximum number of files (${maxFiles}) exceeded`);
      return { maxFiles: true };
    }
    console.debug(`Maximum number of files respected`);
    return null;
  };
}
