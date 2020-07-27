/**
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
package fr.gouv.vitamui.commons.logbook.common;

/**
 *
 * Enum for represent Event
 *
 */
public enum EventType implements EventLogable {
    EXT_VITAMUI_AUTHENTICATION_USER,
    EXT_VITAMUI_BLOCK_USER,
    EXT_VITAMUI_PASSWORD_REVOCATION,
    EXT_VITAMUI_PASSWORD_INIT,
    EXT_VITAMUI_PASSWORD_CHANGE,
    EXT_VITAMUI_AUTHENTICATION_OTP_USER,

    EXT_VITAMUI_CREATE_CUSTOMER,
    EXT_VITAMUI_UPDATE_CUSTOMER,

    EXT_VITAMUI_CREATE_GROUP,
    EXT_VITAMUI_UPDATE_GROUP,

    EXT_VITAMUI_CREATE_IDP,
    EXT_VITAMUI_UPDATE_IDP,

    EXT_VITAMUI_CREATE_OWNER,
    EXT_VITAMUI_UPDATE_OWNER,

    EXT_VITAMUI_CREATE_PROFILE,
    EXT_VITAMUI_UPDATE_PROFILE,

    EXT_VITAMUI_START_SURROGATE_USER,
    EXT_VITAMUI_START_SURROGATE_GENERIC,
    EXT_VITAMUI_STOP_SURROGATE,
    EXT_VITAMUI_DECLINE_SURROGATE,
    EXT_VITAMUI_LOGOUT_SURROGATE,

    EXT_VITAMUI_CREATE_TENANT,
    EXT_VITAMUI_UPDATE_TENANT,

    EXT_VITAMUI_CREATE_USER,
    EXT_VITAMUI_UPDATE_USER

}
