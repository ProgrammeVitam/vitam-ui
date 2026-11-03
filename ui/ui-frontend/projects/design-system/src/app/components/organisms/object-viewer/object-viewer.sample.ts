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
import { Collection, DisplayRule, ProfiledSchemaElement } from 'vitamui-library';

type ComponentType = DisplayRule['ui']['component'];

const LABEL = 'OBJECT_VIEWER.LABEL';

/** Original data, as returned by an API. */
export const sampleData = {
  identifier: 'FRAD075',
  name: 'Archives de Paris',
  otherNames: ['Archives départementales de Paris', 'AD75'],
  languages: ['fr', 'en'],
  description: 'Service chargé de la collecte, de la conservation et de la communication des archives de Paris.',
  fromDate: '1860-01-01',
  toDate: '2024-12-31',
};

/** A virtual group (Path: null) only exists in the projected structure, to organize fields. */
const group = (uiPath: string, label: string): DisplayRule => ({
  Path: uiPath === '' ? '' : null,
  ui: {
    Path: uiPath,
    component: 'group',
    open: true,
    display: true,
    layout: { columns: 2, size: 'medium' },
    label: label && `${LABEL}.${label}`,
  },
});

/** Maps a data path (Path) to its projected path (ui.Path) and the component used to display / edit it. */
const field = (path: string, uiPath: string, component: ComponentType, label: string): DisplayRule => ({
  Path: path,
  ui: {
    Path: uiPath,
    component,
    open: true,
    display: true,
    layout: { columns: 1, size: 'medium' },
    label: `${LABEL}.${label}`,
  },
});

export const sampleTemplate: DisplayRule[] = [
  group('', ''),
  group('Identification', 'IDENTIFICATION'),
  field('identifier', 'Identification.Identifier', 'textfield', 'IDENTIFIER'),
  field('name', 'Identification.Name', 'textfield', 'NAME'),
  field('otherNames', 'Identification.OtherNames', 'textfield', 'OTHER_NAMES'),
  field('languages', 'Identification.Languages', 'select', 'LANGUAGES'),
  group('Description', 'DESCRIPTION'),
  field('description', 'Description.Description', 'textarea', 'DESCRIPTION'),
  field('fromDate', 'Description.FromDate', 'datepicker', 'FROM_DATE'),
  field('toDate', 'Description.ToDate', 'datepicker', 'TO_DATE'),
];

const schemaElement = (
  apiPath: string,
  fieldName: string,
  element: Pick<ProfiledSchemaElement, 'Type' | 'Cardinality'> & Partial<ProfiledSchemaElement>,
): ProfiledSchemaElement => ({
  Path: fieldName,
  FieldName: fieldName,
  SedaField: fieldName,
  ApiField: apiPath,
  ApiPath: apiPath,
  Origin: 'INTERNAL',
  Collection: Collection.AGENCY,
  SedaVersions: [],
  Category: 'DESCRIPTION',
  DataType: 'STRING',
  StringSize: 'SHORT',
  ...element,
});

/** Schema used by the editor to compute cardinalities, required fields, validators, hints and select options. */
export const sampleSchema: ProfiledSchemaElement[] = [
  schemaElement('identifier', 'Identifier', {
    Type: 'KEYWORD',
    Cardinality: 'ONE_REQUIRED',
    Control: { Type: 'REGEX', Value: '.{1,10}', Comment: `${LABEL}.MAX_LENGTH_10` },
  }),
  schemaElement('name', 'Name', { Type: 'TEXT', Cardinality: 'ONE_REQUIRED' }),
  schemaElement('otherNames', 'OtherNames', { Type: 'TEXT', Cardinality: 'MANY' }),
  schemaElement('languages', 'Languages', {
    Type: 'KEYWORD',
    Cardinality: 'MANY',
    Control: { Type: 'SELECT', Values: ['fr', 'en', 'de', 'es'] },
  }),
  schemaElement('description', 'Description', { Type: 'TEXT', Cardinality: 'ONE', StringSize: 'LARGE' }),
  schemaElement('fromDate', 'FromDate', { Type: 'DATE', Cardinality: 'ONE', DataType: 'DATETIME' }),
  schemaElement('toDate', 'ToDate', { Type: 'DATE', Cardinality: 'ONE', DataType: 'DATETIME' }),
];
