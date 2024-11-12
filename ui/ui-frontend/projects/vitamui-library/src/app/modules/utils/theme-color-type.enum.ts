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
export enum ThemeColorType {
  VITAMUI_PRIMARY = 'vitamui-primary',
  VITAMUI_SECONDARY = 'vitamui-secondary',
  VITAMUI_TERTIARY = 'vitamui-tertiary',
  VITAMUI_HEADER_FOOTER = 'vitamui-header-footer',
  VITAMUI_BACKGROUND = 'vitamui-background',
  VITAMUI_GREY = 'vitamui-grey',
  VITAMUI_ADDITIONAL = 'vitamui-additional',
  /* DEPRECATED colors : Use color chart with declinations var(--vitamui-primary-XXX) and var(--vitamui-secondary-XXX) */
  VITAMUI_PRIMARY_LIGHT = 'vitamui-primary-light',
  VITAMUI_PRIMARY_LIGHT_20 = 'vitamui-primary-light-20',
  VITAMUI_PRIMARY_DARK = 'vitamui-primary-dark',
  VITAMUI_SECONDARY_LIGHT = 'vitamui-secondary-light',
  VITAMUI_SECONDARY_LIGHT_8 = 'vitamui-secondary-light-8',
  VITAMUI_SECONDARY_DARK_5 = 'vitamui-secondary-dark-5',
}
