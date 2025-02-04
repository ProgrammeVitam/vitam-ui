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
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';
import { CustomFile } from '../../../../lib/models/custom-file';

describe('FileSelectorComponent', () => {
  let component: FileSelectorComponent;
  let fixture: ComponentFixture<FileSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FileSelectorComponent, TranslateModule.forRoot(), PipesModule, LoggerModule.forRoot(), MatSnackBarModule],
    }).compileComponents();

    fixture = TestBed.createComponent(FileSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should reset the input after file selection', () => {
    component['inputFiles'] = { nativeElement: { value: 'test' } };
    const mockFiles = [new File(['content'], 'test.json')];
    component.handleFilesSelection(mockFiles);
    expect(component['inputFiles'].nativeElement.value).toBe('');
  });

  it('should filter files based on allowed extensions', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    const file2 = new File([''], 'file2.txt', { type: 'text/plain' });
    component.extensions = ['.json'];
    component.handleFilesSelection([file1, file2]);
    expect(component.files.length).toBe(1);
    expect(component.files[0].name).toBe('file1.json');
  });

  it('should limit files to one if multipleFiles is false', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    const file2 = new File([''], 'file2.json', { type: 'application/json' });
    component.multipleFiles = false;
    component.handleFilesSelection([file1, file2]);
    expect(component.files.length).toBe(1);
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
    component.multipleFiles = true;
    component.files = [file1];

    spyOn(component.filesChanged, 'emit');
    component.handleFilesSelection([file2]);

    expect(component.files).toEqual([file1, file2]);
    expect(component.filesChanged.emit).toHaveBeenCalledWith([file1, file2]);
  });

  it('should remove a file from files and displayFiles arrays', () => {
    const file1 = new File([''], 'file1.json', { type: 'application/json' });
    component.files = [file1];
    component.displayFiles = [{ name: 'file1.json', size: 0, directory: false }];
    component.removeFile(component.displayFiles[0]);
    expect(component.files.length).toBe(0);
    expect(component.displayFiles.length).toBe(0);
  });

  it('should remove all files within a directory', () => {
    const file1 = new CustomFile([''], 'file1.json', { type: 'application/json' });
    file1.relativePath = 'dir1';
    component.files = [file1];
    component.displayFiles = [{ name: 'dir1', size: 0, directory: true }];
    component.removeFile(component.displayFiles[0]);
    expect(component.displayFiles.length).toBe(0);
    expect(component.files.length).toBe(0);
  });

  it('should remove only the specified directory and keep others', () => {
    const file1 = new CustomFile([''], 'file1.json', { type: 'application/json' });
    const file2 = new CustomFile([''], 'file2.json', { type: 'application/json' });
    file1.relativePath = 'dir1';
    file2.relativePath = 'dir2';

    component.files = [file1, file2];
    component.displayFiles = [
      { name: 'dir1', size: 0, directory: true },
      { name: 'dir2', size: 0, directory: true },
    ];

    component.removeFile(component.displayFiles[0]);

    expect(component.displayFiles.length).toBe(1);
    expect(component.displayFiles[0].name).toBe('dir2');

    expect(component.files.length).toBe(1);
    expect(component.files[0]).toEqual(file2);
  });

  it('should skip adding files if directory already exists', () => {
    const mockFiles = [
      new CustomFile(['content'], 'file12.json', { type: 'application/json' }),
      new CustomFile(['content'], 'file2.json', { type: 'application/json' }),
    ];
    mockFiles.forEach((file) => (file.relativePath = 'folder1'));

    const mockDisplayFile = { name: 'folder1', size: 1000, directory: true };

    component.displayFiles = [mockDisplayFile];

    spyOn(component.snackBar, 'open');

    component.handleFilesSelection(mockFiles);

    mockFiles.forEach((file) => {
      expect(component.files).not.toContain(file);
    });

    expect(component.snackBar.open).toHaveBeenCalledWith(jasmine.any(String), null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
  });
});
