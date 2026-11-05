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
import { Provider } from '@angular/core';
import { ArchiveProfileApiService } from './core/services/archive-profile-api.service';
import { ProfileService } from './core/services/profile.service';
import { PastisApiService } from './core/api/api.pastis.service';
import { ArchivalProfileUnitApiService } from './core/services/archival-profile-unit-api.service';
import { FileService } from './core/services/file.service';
import { NoticeService } from './core/services/notice.service';
import { ArchiveProfileSaverService } from './user-actions/save-profile/archive-profile-saver.service';
import { ArchiveUnitProfileSaverService } from './user-actions/save-profile/archive-unit-profile-saver.service';
import { IdentifierExistsValidator } from './validators/IdentifierExistsValidator';

/**
 * Services of the pastis feature whose HTTP calls depend on BASE_URL, directly or through another service.
 * They are not provided in root: they must be provided with the feature's BASE_URL (see provideVitamuiFeature).
 */
export function providePastisServices(): Provider[] {
  return [
    ArchiveProfileApiService,
    ProfileService,
    ArchivalProfileUnitApiService,
    ArchiveProfileSaverService,
    ArchiveUnitProfileSaverService,
    FileService,
    IdentifierExistsValidator,
    NoticeService,
    PastisApiService,
  ];
}
