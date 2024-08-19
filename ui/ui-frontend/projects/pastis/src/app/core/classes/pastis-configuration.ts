import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PastisApiService } from '../api/api.pastis.service';

@Injectable()
export class PastisConfiguration {
  // routes pastis
  pastisEditPage: string;
  sedaUrl: string;
  pastisNewProfile: string;

  // pastis base href
  pastisPathPrefix: string;

  // commons api path
  createProfileByTypeUrl: string;
  getAllProfilesUrl: string;
  editProfileUrl: string;

  // api's path

  pastisApiPath: string;
  archivalProfileUnitApiPath: string;
  archiveProfileApiPath: string;

  downloadProfile: string;
  uploadProfileUrl: string;
  savePAasFileUrl: string;
  savePUAasFileUrl: string;
  getFileUrl: string;
  updateFileUrl: string;
  getArchivalProfileUnitUrl: string;
  getAllArchivalProfileUrl: string;
  getProfilePaginatedUrl: string;
  getArchivalProfileUnitPaginatedUrl: string;
  updateProfileById: string;
  updateArchivalProfileUnitById: string;
  importProfileInExistingNotice: string;
  metaModelUrl: string;

  constructor(private pastisApi: PastisApiService) {}

  public initConfiguration(): Promise<any> {
    if (environment.apiServerUrl !== undefined && environment.standalone) {
      return new Promise((r, e) => {
        this.pastisApi.getLocally('./assets/config/config-standalone.json').subscribe(
          (content: PastisConfiguration) => {
            Object.assign(this, content);
            r(this);
          },
          (reason) => e(reason),
        );
      });
    } else {
      return new Promise((r, e) => {
        this.pastisApi.getLocally('./assets/config/config-vitam-ui.json').subscribe(
          (content: PastisConfiguration) => {
            Object.assign(this, content);
            r(this);
          },
          (reason) => e(reason),
        );
      });
    }
  }
}
