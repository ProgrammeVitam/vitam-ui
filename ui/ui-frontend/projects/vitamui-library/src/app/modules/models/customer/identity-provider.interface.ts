/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { AuthnRequestBindingEnum } from '../../utils';
import { Id } from '../id.interface';

export interface IdentityProvider extends Id {
  identifier?: string;
  customerId: string;
  name: string;
  technicalName: string;
  internal: boolean;
  keystorePassword: string;
  keystore: File;
  idpMetadata: File;
  patterns: string[];
  enabled: boolean;
  idpMetadataUrl?: string;
  spMetadataUrl?: string;
  readonly: boolean;
  mailAttribute?: string;
  identifierAttribute?: string;
  authnRequestBinding: AuthnRequestBindingEnum;
  maximumAuthenticationLifetime: number;
  wantsAssertionsSigned: boolean;
  authnRequestSigned: boolean;
  propagateLogout: boolean;
  autoProvisioningEnabled: boolean;
  clientId?: string;
  clientSecret?: string;
  discoveryUrl?: string;
  scope?: string;
  preferredJwsAlgorithm?: string;
  customParams?: Map<string, string>;
  useState?: boolean;
  useNonce?: boolean;
  usePkce?: boolean;
  protocoleType?: string;
}

/**
 * Function created to bypass IE issues with 'new File(fileBits: BlobPart[], fileName: string)' which is not supported.
 */
export function newFile(fileBits: BlobPart[], fileName: string) {
  const blob: any = new Blob(fileBits);
  blob.lastModifiedDate = new Date();
  blob.name = fileName;
  blob.fileName = fileName;

  return blob as File;
}