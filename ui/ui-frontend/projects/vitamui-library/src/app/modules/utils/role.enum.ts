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
export enum Role {
  ROLE_CREATE_USERS = 'ROLE_CREATE_USERS',
  ROLE_ANONYMIZATION_USERS = 'ROLE_ANONYMIZATION_USERS',
  ROLE_GENERIC_USERS = 'ROLE_GENERIC_USERS',
  ROLE_UPDATE_USERS = 'ROLE_UPDATE_USERS',
  ROLE_MFA_USERS = 'ROLE_MFA_USERS',
  ROLE_UPDATE_STANDARD_USERS = 'ROLE_UPDATE_STANDARD_USERS',
  ROLE_GET_USERS = 'ROLE_GET_USERS',
  ROLE_GET_GROUPS = 'ROLE_GET_GROUPS',

  ROLE_SEARCH = 'ROLE_SEARCH',
  ROLE_EXPORT_DIP = 'ROLE_EXPORT_DIP',
  ROLE_LOGBOOKS = 'ROLE_LOGBOOKS',
  ROLE_GET_LOGBOOKS_OPERATIONS = 'ROLE_GET_LOGBOOKS_OPERATIONS',
  ROLE_GET_BASKETS = 'ROLE_GET_BASKETS',
  ROLE_CREATE_BASKETS = 'ROLE_CREATE_BASKETS',
  ROLE_UPDATE_BASKETS = 'ROLE_UPDATE_BASKETS',
  ROLE_DELETE_BASKETS = 'ROLE_DELETE_BASKETS',
  ROLE_GET_ARCHIVE_PARAM = 'ROLE_GET_ARCHIVE_PARAM',
  ROLE_SEARCH_CYCLE = 'ROLE_SEARCH_CYCLE',
  ROLE_CREATE_WITHDRAWAL_TRANSFER_ITEMS = 'ROLE_CREATE_WITHDRAWAL_TRANSFER_ITEMS',
  ROLE_CREATE_DISPOSAL_TRANSFER_ITEMS = 'ROLE_CREATE_DISPOSAL_TRANSFER_ITEMS',
  ROLE_INGEST_ITEM = 'ROLE_INGEST_ITEM',
  ROLE_INGEST_FILE = 'ROLE_INGEST_FILE',
  ROLE_GET_ARCHIVE_LIFECYCLE = 'ROLE_GET_ARCHIVE_LIFECYCLE',
  ROLE_DOWNLOAD_PROFILE = 'ROLE_DOWNLOAD_PROFILE',
  ROLE_GET_INGEST_CONTRACTS = 'ROLE_GET_INGEST_CONTRACTS',
  ROLE_GET_OPERATIONS = 'ROLE_GET_OPERATIONS',
  ROLE_GET_MY_ARCHIVE_TREE = 'ROLE_GET_MY_ARCHIVE_TREE',

  ROLE_GET_USER_INFOS = 'ROLE_GET_USER_INFOS',
  ROLE_CREATE_USER_INFOS = 'ROLE_CREATE_USER_INFOS',
  ROLE_UPDATE_USER_INFOS = 'ROLE_UPDATE_USER_INFOS',

  ROLE_CREATE_AGENCIES = 'ROLE_CREATE_AGENCIES',
  ROLE_IMPORT_AGENCIES = 'ROLE_IMPORT_AGENCIES',
  ROLE_EXPORT_AGENCIES = 'ROLE_EXPORT_AGENCIES',
  ROLE_UPDATE_AGENCIES = 'ROLE_UPDATE_AGENCIES',

  ROLE_GET_RULES = 'ROLE_GET_RULES',
  ROLE_CREATE_RULES = 'ROLE_CREATE_RULES',
  ROLE_UPDATE_RULES = 'ROLE_UPDATE_RULES',
  ROLE_DELETE_RULES = 'ROLE_DELETE_RULES',
  ROLE_IMPORT_RULES = 'ROLE_IMPORT_RULES',
  ROLE_EXPORT_RULES = 'ROLE_EXPORT_RULES',

  ROLE_UPDATE_FILE_FORMATS = 'ROLE_UPDATE_FILE_FORMATS',
  ROLE_CREATE_FILE_FORMATS = 'ROLE_CREATE_FILE_FORMATS',
  ROLE_IMPORT_FILE_FORMATS = 'ROLE_IMPORT_FILE_FORMATS',
}