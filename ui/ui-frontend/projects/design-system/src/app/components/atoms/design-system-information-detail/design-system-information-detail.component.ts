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
import { Component } from '@angular/core';
import { ValidationError } from 'vitamui-library';
import { InformationDetailComponent } from 'vitamui-library';

@Component({
  imports: [InformationDetailComponent],
  templateUrl: './design-system-information-detail.component.html',
  styleUrl: './design-system-information-detail.component.scss',
})
export class DesignSystemInformationDetailComponent {
  errorWithoutEvDetData: ValidationError = {
    evId: 'aeaaaaaaaaec255qad23sam3yx4rtvyaaaaq',
    obId: 'aebqaaaaagec255qad23sam3yx4rsfiaaaba',
    evTypeProc: 'COLLECT_SIP_INGEST',
    outDetail: 'LFC.CHECK_DIGEST.CALC_CHECK.INVALID.KO',
    outMessg: "Échec de la vérification de l'empreinte du fichier",
    evIdProc: 'aeeaaaaaagec255qad37gam3yx4roaqaaaaq',
    evDateTime: '2026-01-16T08:43:15.287',
  };
  errorWithEvDetData: ValidationError = {
    evId: 'aeaaaaaaaaec255qad23sam3yx4rtvyaaaaq',
    obId: 'aebqaaaaagec255qad23sam3yx4rsfiaaaba',
    evTypeProc: 'COLLECT_SIP_INGEST',
    outDetail: 'LFC.CHECK_DIGEST.CALC_CHECK.INVALID.KO',
    outMessg: "Échec de la vérification de l'empreinte du fichier",
    evIdProc: 'aeeaaaaaagec255qad37gam3yx4roaqaaaaq',
    evDateTime: '2026-01-16T08:43:15.287',
    evDetData:
      '{"evDetTechData":"metadata contains fields declared in ontology with a wrong format : Error \'Invalid date format: bad_date\' on field \'LastModified\'."}',
  };
  colors = ['primary', 'secondary', 'danger', 'success', 'warning', 'neutral'] as const;
}
