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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FileSelectorComponent } from './file-selector.component';
import { TranslateModule } from '@ngx-translate/core';
import { PipesModule } from '../../pipes/pipes.module';
import { LoggerModule } from '../../logger';
import { CustomFile } from '../../../../lib/models/custom-file';
import { SnackBarService } from '../snack-bar/snack-bar.service';

describe('FileSelectorComponent', () => {
  let component: FileSelectorComponent;
  let fixture: ComponentFixture<FileSelectorComponent>;
  const snackBarServiceSpy = jasmine.createSpyObj('SnackBarService', ['open']);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileSelectorComponent, TranslateModule.forRoot(), PipesModule, LoggerModule.forRoot()],
      providers: [{ provide: SnackBarService, useValue: snackBarServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(FileSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should reset the input after file selection', () => {
    component['fileSelector'] = { nativeElement: { value: 'test' } };
    const mockFiles = [new File(['content'], 'test.json')];
    component.handleFilesSelection(mockFiles);
    expect(component['fileSelector'].nativeElement.value).toBe('');
  });

  it('should reset the input after directory selection', () => {
    component['directorySelector'] = { nativeElement: { value: 'test' } };
    const mockFiles = [new File(['content'], 'test')];
    component.handleFilesSelection(mockFiles);
    expect(component['directorySelector'].nativeElement.value).toBe('');
  });

  it('should filter files based on allowed extensions', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    const file2 = new File([''], 'file2.txt', { type: 'text/plain' });
    component.multiple = true;
    component.extensions = ['.json'];
    component.handleFilesSelection([file1, file2]);
    expect((component as any).control.value.length).toBe(1);
    expect((component as any).control.value[0].name).toBe('file1.json');
  });

  it('should prevent dropping multiple files if multipleFiles is false', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    const file2 = new File([''], 'file2.json', { type: 'application/json' });
    component.multiple = false;
    component.handleFilesSelection([file1, file2]);
    expect((component as any).control.value?.length).toBeFalsy();
  });

  it('should emit filesChanged event with updated files', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    spyOn(component.filesChanged, 'emit');
    component.handleFilesSelection([file1]);
    expect(component.filesChanged.emit).toHaveBeenCalledWith([file1]);
  });

  it('should emit filesChanged event with updated files when adding to an existing list', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    const file2 = new File([''], 'file2.json', { type: 'application/json' });
    component.multiple = true;
    (component as any).control.setValue([file1]);

    spyOn(component.filesChanged, 'emit');
    component.handleFilesSelection([file2]);

    expect((component as any).control.value).toEqual([file1, file2]);
    expect(component.filesChanged.emit).toHaveBeenCalledWith([file1, file2]);
  });

  it('should remove a file from files and displayFiles arrays', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    (component as any).control.setValue([file1]);
    component.displayFiles = [{ name: 'file1.json', size: 0, directory: false }];
    component.removeFile(component.displayFiles[0]);
    expect((component as any).control.value.length).toBe(0);
    expect(component.displayFiles.length).toBe(0);
  });

  it('should remove all files within a directory', () => {
    const file1 = new CustomFile([''], 'file1.json', { type: 'application/json' });
    file1.relativePath = 'dir1';
    (component as any).control.setValue([file1]);
    component.displayFiles = [{ name: 'dir1', size: 0, directory: true }];
    component.removeFile(component.displayFiles[0]);
    expect(component.displayFiles.length).toBe(0);
    expect((component as any).control.value.length).toBe(0);
  });

  it('should remove only the specified directory and keep others', () => {
    const file1 = new CustomFile([''], 'file1.json', { type: 'application/json' });
    const file2 = new CustomFile([''], 'file2.json', { type: 'application/json' });
    file1.relativePath = 'dir1';
    file2.relativePath = 'dir2';

    component.directoryMode = true;
    (component as any).control.setValue([file1, file2]);
    component.displayFiles = [
      { name: 'dir1', size: 0, directory: true },
      { name: 'dir2', size: 0, directory: true },
    ];

    component.removeFile(component.displayFiles[0]);

    expect(component.displayFiles.length).toBe(1);
    expect(component.displayFiles[0].name).toBe('dir2');

    expect((component as any).control.value.length).toBe(1);
    expect((component as any).control.value[0]).toEqual(file2);
  });

  it('should skip adding files if directory already exists', () => {
    const mockFiles = [
      new CustomFile(['content'], 'file12.json', { type: 'application/json' }),
      new CustomFile(['content'], 'file2.json', { type: 'application/json' }),
    ];
    mockFiles.forEach((file) => (file.relativePath = 'folder1'));

    const mockDisplayFile = { name: 'folder1', size: 1000, directory: true };

    component.multiple = true;
    component.directoryMode = true;
    component.displayFiles = [mockDisplayFile];

    component.handleFilesSelection(mockFiles);

    mockFiles.forEach((file) => {
      expect((component as any).control.value).not.toContain(file);
    });

<<<<<<< HEAD
    expect(snackBarServiceSpy.open).toHaveBeenCalledWith({ message: 'FILE_SELECTOR.UPLOAD_FILE_ALREADY_IMPORTED' });
=======
    it('should have an error if cumulative file size is greater than max allowed size', async () => {
      fixture.componentInstance.multiple = true;
      fixture.componentInstance.maxSizeInBytes = 20;
      fixture.detectChanges();

      await component.handleFilesSelection([
        file('dir1/file1.json', 'X'.repeat(component.maxSizeInBytes() / 4)),
        file('dir1/file2.json', 'X'.repeat(component.maxSizeInBytes() / 4)),
        file('dir2/file.json', 'X'.repeat(component.maxSizeInBytes() / 4)),
        file('file.json', 'X'.repeat(component.maxSizeInBytes() / 4 + 1)),
      ]);

      expect(control.valid).toBe(false);
      expect(control.hasError('maxSizeInBytes')).toBe(true);
      expect(control.getError('maxSizeInBytes')).toEqual({
        size: `${component.maxSizeInBytes() + 1} octets`,
        maxSize: `${component.maxSizeInBytes()} octets`,
      });
    });
  });

  describe('Individual file validation', () => {
    it('should show an error for invalid extension', async () => {
      fixture.componentInstance.extensions = ['.json'];
      fixture.detectChanges();

      await component.handleFilesSelection([file('file1.txt')]);
      await waitForValidation();

      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ invalidExtension: { extensions: '.json' } }));
      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
    });

    it('should keep the error when the native file picker is reopened and cancelled (empty FileList)', async () => {
      fixture.componentInstance.extensions = ['.json'];
      fixture.detectChanges();

      await component.handleFilesSelection([file('file1.txt')]);
      await waitForValidation();

      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);

      // Simulates the browser firing a (change) event with no files when the OS file dialog is cancelled
      await component.handleFilesSelection([]);
      await waitForValidation();

      expect(component.displayFiles.length).toBe(1);
      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ invalidExtension: { extensions: '.json' } }));
      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
    });

    describe('Duplication', () => {
      beforeEach(() => {
        fixture.componentInstance.multiple = true;
        fixture.detectChanges();
      });

      it('should show an error for duplicated file', async () => {
        await component.handleFilesSelection([file('file.txt')]);
        await component.handleFilesSelection([file('FILE.txt')]);
        await waitForValidation();

        expect(control.valid).toBe(false);
        expect(control.hasError('invalidFiles')).toBe(true);
        expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ duplicatedFile: expect.anything() }));
        expect(component.displayFiles[1].errors).toEqual(expect.objectContaining({ duplicatedFile: expect.anything() }));
      });

      it('should show an error for duplicated directory', async () => {
        await component.handleFilesSelection([file('dir/file1.txt')]);
        await component.handleFilesSelection([file('DIR/file2.txt')]);
        await waitForValidation();

        expect(control.valid).toBe(false);
        expect(control.hasError('invalidFiles')).toBe(true);
        expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ duplicatedDirectory: expect.anything() }));
        expect(component.displayFiles[1].errors).toEqual(expect.objectContaining({ duplicatedDirectory: expect.anything() }));
      });

      it('should show an error for file and directory having the same name', async () => {
        await component.handleFilesSelection([file('DIR_OR_FILE/file1.txt')]);
        await component.handleFilesSelection([file('dir_or_file')]);
        await waitForValidation();

        expect(control.valid).toBe(false);
        expect(control.hasError('invalidFiles')).toBe(true);
        expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ duplicatedDirectory: expect.anything() }));
        expect(component.displayFiles[1].errors).toEqual(expect.objectContaining({ duplicatedFile: expect.anything() }));
      });
    });

    it('should show an error for a directory when only files are allowed', async () => {
      fixture.componentInstance.directoryMode = false;
      fixture.detectChanges();

      await component.handleFilesSelection([file('dir/file.txt')]);
      await waitForValidation();

      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ directoryForbidden: expect.anything() }));
    });

    it('should show custom validation error', async () => {
      fixture.componentInstance.fileValidators = (file: File) =>
        Promise.resolve(
          file.name === 'file1.json' ? { fileErrors: { 'CUSTOM.ERROR': true }, controlErrors: { invalidFiles: true } } : null,
        );
      fixture.detectChanges();

      await component.handleFilesSelection([file('file1.json')]);
      await waitForValidation();

      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ 'CUSTOM.ERROR': expect.anything() }));
      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
    });

    it('should accumulate errors', async () => {
      fixture.componentInstance.multiple = true;
      fixture.componentInstance.extensions = ['.png'];
      fixture.componentInstance.fileValidators = (file: File) =>
        Promise.resolve(file.name === 'file2.json' ? { fileErrors: { 'CUSTOM.ERROR': true }, controlErrors: { toto: true } } : null);
      fixture.detectChanges();

      await component.handleFilesSelection([file('file1.txt'), file('file2.json')]);
      await waitForValidation();

      expect(component.displayFiles.length).toBe(2);

      expect(component.displayFiles[0].name).toBe('file1.txt');
      expect(component.displayFiles[0].errors).toEqual(
        expect.objectContaining({
          invalidExtension: expect.anything(),
        }),
      );

      expect(component.displayFiles[1].name).toBe('file2.json');
      expect(component.displayFiles[1].errors).toEqual(
        expect.objectContaining({
          invalidExtension: expect.anything(),
          'CUSTOM.ERROR': expect.anything(),
        }),
      );

      expect(control.hasError('invalidFiles')).toBe(true);
      expect(control.getError('invalidFiles')).toEqual(expect.objectContaining({ files: expect.anything() }));
      expect(control.getError('invalidFiles').files).toContain(`file1.txt`);
      expect(control.getError('invalidFiles').files).toContain(`file2.json`);
    });
>>>>>>> e63d8925b (Bug #15770: [Search] Display error: non-XML file incorrectly marked as valid after cancellation.)
  });
});
