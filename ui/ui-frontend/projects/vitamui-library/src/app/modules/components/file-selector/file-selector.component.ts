import { Component, ContentChild, ElementRef, Input, TemplateRef, ViewChild } from '@angular/core';
import { DragAndDropDirective } from '../../directives/drag-and-drop/drag-and-drop.directive';
import { TranslateModule } from '@ngx-translate/core';
import { NgForOf, NgIf, NgTemplateOutlet } from '@angular/common';
import { PipesModule } from '../../pipes/pipes.module';

@Component({
  selector: 'vitamui-file-selector',
  templateUrl: './file-selector.component.html',
  styleUrl: './file-selector.component.scss',
  standalone: true,
  imports: [DragAndDropDirective, TranslateModule, NgIf, NgForOf, PipesModule, NgTemplateOutlet],
})
export class FileSelectorComponent {
  /**
   * Allowed extensions. Ex: ['.json', '.rng']
   */
  @Input() extensions?: string[];
  @Input() multipleFiles = false;
  @Input() maxSizeInBytes: number; // TODO: do some control on the file size?

  @ViewChild('inputFiles') inputFiles: ElementRef;

  @ContentChild('fileList') fileList: TemplateRef<any>;
  @ContentChild('content') content: TemplateRef<any>;

  protected files: File[] = [];

  protected handleFiles(files: FileList | File[]) {
    if (!this.multipleFiles && this.files.length > 0) return;

    // Filter to keep only the ones matching extension list (useful for drag & drop and to make sure no other type has been selected)
    this.files.push(
      ...Array.from(files).filter(
        (file) => !this.extensions?.length || this.extensions.some((ext) => file.name.toLowerCase().endsWith(ext.toLowerCase())),
      ),
    );
  }

  openFileSelectorOSDialog() {
    this.inputFiles.nativeElement.click();
  }

  removeFile(file: File) {
    this.files.splice(this.files.indexOf(file), 1);
  }
}
