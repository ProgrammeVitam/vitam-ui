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
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Component, ViewChild } from '@angular/core';

@Component({
  template: ` <vitamui-file-selector [formControl]="control" /> `,
  imports: [FileSelectorComponent, ReactiveFormsModule],
})
class TestHostComponent {
  control: FormControl = new FormControl();
  @ViewChild(FileSelectorComponent, { static: false })
  component: FileSelectorComponent;
}

describe('FileSelectorComponent', () => {
  let control: FormControl;
  let component: FileSelectorComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent, FileSelectorComponent, TranslateModule.forRoot(), PipesModule, LoggerModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(TestHostComponent);
    control = fixture.componentInstance.control;
    fixture.detectChanges();
    component = fixture.componentInstance.component;
  });

  async function waitForValidation() {
    await fixture.whenStable();
  }

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should reset the input after file selection', async () => {
    component['fileSelector'] = { nativeElement: { value: 'test' } };
    const mockFiles = [file('test.json', 'content')];
    await component.handleFilesSelection(mockFiles);
    expect(component['fileSelector'].nativeElement.value).toBe('');
  });

  it('should reset the input after directory selection', async () => {
    component['directorySelector'] = { nativeElement: { value: 'test' } };
    const mockFiles = [file('test', 'content')];
    await component.handleFilesSelection(mockFiles);
    expect(component['directorySelector'].nativeElement.value).toBe('');
  });

  it('should emit filesChanged event with updated files', async () => {
    const file1 = file('file1.json');
    vi.spyOn(component.filesChanged, 'emit');
    await component.handleFilesSelection([file1]);
    expect(component.filesChanged.emit).toHaveBeenCalledWith([file1]);
  });

  it('should emit filesChanged event with updated files when adding to an existing list', async () => {
    const file1 = file('file1.json');
    const file2 = file('file2.json');
    component.multiple = true;
    control.setValue([file1]);

    vi.spyOn(component.filesChanged, 'emit');
    await component.handleFilesSelection([file2]);

    expect(control.value).toEqual([file1, file2]);
    expect(component.filesChanged.emit).toHaveBeenCalledWith([file1, file2]);
  });

  it('should remove a file from files and displayFiles arrays', async () => {
    await component.handleFilesSelection([file('file1.json')]);
    expect(control.value.length).toBe(1);
    expect(component.displayFiles.length).toBe(1);

    await component.removeFile(component.displayFiles[0]);

    expect(control.value.length).toBe(0);
    expect(component.displayFiles.length).toBe(0);
  });

  it('should remove all files within a directory', async () => {
    component.directoryMode = true;

    await component.handleFilesSelection([file('dir1/file1.json'), file('dir1/file2.json')]);
    expect(control.value.length).toBe(2);
    expect(component.displayFiles.length).toBe(1);

    await component.removeFile(component.displayFiles[0]);
    expect(component.displayFiles.length).toBe(0);
    expect(control.value.length).toBe(0);
  });

  it('should remove only the specified directory and keep others', async () => {
    component.directoryMode = true;

    await component.handleFilesSelection([file('dir1/file1.json'), file('dir2/file2.json')]);

    await component.removeFile(component.displayFiles[0]);

    expect(component.displayFiles.length).toBe(1);
    expect(component.displayFiles[0].name).toBe('dir2');

    expect(control.value.length).toBe(1);
    expect((control.value[0] as File).name).toEqual('file2.json');
  });

  it('should allow multiple files when multiple=true', async () => {
    component.multiple = true;

    await component.handleFilesSelection([file('file1.json'), file('file2.json')]);
    await waitForValidation();

    expect(control.value.length).toBe(2);
    expect(component.displayFiles.length).toBe(2);
    expect(control.valid).toBe(true);
  });

  it('should have an error if multiples files are added when multiple=false', async () => {
    component.multiple = false;

    await component.handleFilesSelection([file('file1.json'), file('file2.json')]);

    expect(control.value.length).toBe(2);
    expect(component.displayFiles.length).toBe(2);
    expect(control.valid).toBe(false);
    expect(control.hasError('maxFiles')).toBe(true);
  });

  it('should sort correctly', async () => {
    component.multiple = true;
    component.directoryMode = true;

    await component.handleFilesSelection([
      file('yyyy.json'),
      file('bbb.json'),
      file('yyyy_duplicate.json'),
      file('bbb_duplicate.json'),
      file('zzz/file.json'),
      file('aaa/file.json'),
      file('zzz_dir_duplicate/file.json'),
      file('aaa_dir_duplicate/file.json'),
    ]);
    await component.handleFilesSelection([
      file('yyyy_duplicate.json'),
      file('bbb_duplicate.json'),
      file('zzz_dir_duplicate/file.json'),
      file('aaa_dir_duplicate/file.json'),
    ]);

    expect(control.value.length).toBe(12);
    expect(component.displayFiles.length).toBe(12);
    expect(component.displayFiles.map((df) => df.name)).toEqual([
      // First, errors (duplicates), starting with directories then files, alphabetically
      'aaa_dir_duplicate',
      'aaa_dir_duplicate',
      'zzz_dir_duplicate',
      'zzz_dir_duplicate',
      'bbb_duplicate.json',
      'bbb_duplicate.json',
      'yyyy_duplicate.json',
      'yyyy_duplicate.json',
      // Then, non-errors, starting with directories then files, alphabetically
      'aaa',
      'zzz',
      'bbb.json',
      'yyyy.json',
    ]);
  });

  describe('Global validations', () => {
    it('should have an error if file size is greater than max allowed size', async () => {
      component.multiple = false;
      component.maxSizeInBytes = 10;

      await component.handleFilesSelection([file('file1.json', 'X'.repeat(component.maxSizeInBytes + 1))]);

      expect(control.valid).toBe(false);
      expect(control.hasError('maxSizeInBytes')).toBe(true);
      expect(control.getError('maxSizeInBytes')).toEqual({
        size: `${component.maxSizeInBytes + 1} octets`,
        maxSize: `${component.maxSizeInBytes} octets`,
      });
    });

    it('should have an error if cumulative file size is greater than max allowed size', async () => {
      component.multiple = true;
      component.maxSizeInBytes = 20;

      await component.handleFilesSelection([
        file('dir1/file1.json', 'X'.repeat(component.maxSizeInBytes / 4)),
        file('dir1/file2.json', 'X'.repeat(component.maxSizeInBytes / 4)),
        file('dir2/file.json', 'X'.repeat(component.maxSizeInBytes / 4)),
        file('file.json', 'X'.repeat(component.maxSizeInBytes / 4 + 1)),
      ]);

      expect(control.valid).toBe(false);
      expect(control.hasError('maxSizeInBytes')).toBe(true);
      expect(control.getError('maxSizeInBytes')).toEqual({
        size: `${component.maxSizeInBytes + 1} octets`,
        maxSize: `${component.maxSizeInBytes} octets`,
      });
    });
  });

  describe('Individual file validation', () => {
    it('should show an error for invalid extension', async () => {
      component.extensions = ['.json'];

      await component.handleFilesSelection([file('file1.txt')]);
      await waitForValidation();

      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ invalidExtension: { extensions: '.json' } }));
      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
    });

    describe('Duplication', () => {
      beforeEach(() => {
        component.multiple = true;
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
      component.directoryMode = false;

      await component.handleFilesSelection([file('dir/file.txt')]);
      await waitForValidation();

      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ directoryForbidden: expect.anything() }));
    });

    it('should show custom validation error', async () => {
      component.fileValidators = (file: File) =>
        Promise.resolve(
          file.name === 'file1.json' ? { fileErrors: { 'CUSTOM.ERROR': true }, controlErrors: { invalidFiles: true } } : null,
        );

      await component.handleFilesSelection([file('file1.json')]);
      await waitForValidation();

      expect(component.displayFiles[0].errors).toEqual(expect.objectContaining({ 'CUSTOM.ERROR': expect.anything() }));
      expect(control.valid).toBe(false);
      expect(control.hasError('invalidFiles')).toBe(true);
    });

    it('should accumulate errors', async () => {
      component.multiple = true;
      component.extensions = ['.png'];
      component.fileValidators = (file: File) =>
        Promise.resolve(file.name === 'file2.json' ? { fileErrors: { 'CUSTOM.ERROR': true }, controlErrors: { toto: true } } : null);

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
  });
});

function file(filePath: string, content = ''): CustomFile {
  const fileName = filePath.split('/').pop();
  const extension = fileName.split('.').pop();
  const customFile = new CustomFile([content], fileName, { type: { json: 'application/json', txt: 'text/plain' }[extension] });
  customFile.isDirectory = filePath.includes('/');
  if (customFile.isDirectory) {
    customFile.relativePath = filePath.split('/').slice(0, -1).join('/');
  }
  return customFile;
}
