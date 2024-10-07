/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Component, Input } from '@angular/core';
import { FileUploader } from 'ng2-file-upload';
import { FileService } from '../../core/services/file.service';
import { ProfileService } from '../../core/services/profile.service';
import { filter, finalize, mergeMap } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { SedaService } from '../../core/services/seda.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-user-action-upload',
  templateUrl: './upload-profile.component.html',
  styleUrls: ['./upload-profile.component.scss'],
})
export class UserActionUploadProfileComponent {
  @Input()
  uploader: FileUploader = new FileUploader({ url: '' });
  fileToUpload: File = null;

  constructor(
    private profileService: ProfileService,
    private fileService: FileService,
    private sedaService: SedaService,
    private loaderService: NgxUiLoaderService,
  ) {}

  handleFileInput(files: FileList) {
    this.fileToUpload = files.item(0);
  }

  uploadAndReload(event: any) {
    const fileList: FileList = event.target.files;
    this.handleFileInput(fileList);
    if (this.fileToUpload) {
      const formData = new FormData();
      formData.append('file', this.fileToUpload, this.fileToUpload.name);
      this.loaderService.start();
      this.profileService
        .uploadProfile(formData)
        .pipe(
          mergeMap((profile) =>
            this.profileService.getMetaModel(profile.sedaVersion).pipe(
              map((metaModel) => ({
                profile,
                metaModel,
              })),
            ),
          ),
          filter(({ profile, metaModel }) => Boolean(profile) && Boolean(metaModel)),
          tap(({ profile, metaModel }) => {
            this.sedaService.setMetaModel(metaModel);
            this.fileService.linkFileNodeToSedaData(null, [profile.profile]);
            this.fileService.updateTreeWithProfile(profile);
          }),
          finalize(() => this.loaderService.stop()),
        )
        .subscribe();
    }
  }
}
