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
import { Injectable } from '@angular/core';
import { ArchivalProfileUnit, ArchivalProfileUnitModel } from '../../models/archival-profile-unit';
import { Notice } from '../../models/notice.model';
import { Profile, ProfileModel } from '../../models/profile';
import { ProfileDescription, ProfileDescriptionModel } from '../../models/profile-description.model';
import { FileService } from './file.service';

@Injectable({
  providedIn: 'root'
})
export class NoticeService {

  constructor(private fileService: FileService) { }

  notice: Notice;

  getNotice() {
    this.fileService.notice.subscribe(
      (value: any) => {
        this.notice = value;
        return this.notice;
      },
      (error) => {
        console.log(error);
      }
    );
  }

  puaNotice(retour: any): ArchivalProfileUnit {
    const profileDescription = new ArchivalProfileUnitModel();
    profileDescription.identifier = retour.identifier;
    profileDescription.name = retour.intitule;
    profileDescription.description = retour.description;
    profileDescription.status = retour.selectedStatus;
    profileDescription.controlSchema = '{}';

    return profileDescription;
    }

  paNotice(retour: any, create: boolean): Profile {
    const profile = new ProfileModel();
    profile.identifier = retour.identifier;
    profile.name = retour.intitule;
    profile.description = retour.description;
    profile.status = retour.selectedStatus;
    profile.format = create ? 'RNG' : retour.format;
    return profile;

  }

  profileFromNotice(retour: any, edit: boolean, pua: boolean): ProfileDescription {
    const profile = new ProfileDescriptionModel();
    profile.identifier = retour.identifier;
    profile.name = retour.intitule;
    profile.description = retour.description;
    profile.status = retour.selectedStatus;
    if (!edit && !pua) {
      profile.format = 'RNG';
    }

    return profile;

  }

  profileDescriptionToPaProfile(profileDescription: ProfileDescription): Profile {
    let profile = new ProfileModel();
    profile = Object.assign(profile, profileDescription);
    return profile;
  }

  profileDescriptionToPuaProfile(profileDescription: ProfileDescription): ArchivalProfileUnit {
    let archivalProfileUnit = new ArchivalProfileUnitModel();
    archivalProfileUnit = Object.assign(archivalProfileUnit, profileDescription);
    return archivalProfileUnit;
  }


}
