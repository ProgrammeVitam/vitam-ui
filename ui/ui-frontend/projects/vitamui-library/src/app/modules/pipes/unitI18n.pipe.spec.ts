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
/* eslint-disable no-magic-numbers */

import { Unit } from '../models/units/unit.interface';
import { UnitI18nPipe } from './unitI18n.pipe';

describe('UnitI18nPipe', () => {
  let unit: Partial<Unit>;

  beforeEach(() => {
    unit = {
      Title: 'Title',
      Title_: {
        fr: 'Title fr',
        en: 'Title en',
        FR: 'Title FR',
        EN: 'Title EN',
        es: 'Title es',
      },
      Description: 'Description',
      Description_: {
        fr: 'Description fr',
        en: 'Description en',
        FR: 'Description FR',
        EN: 'Description EN',
        es: 'Description es',
      },
    };
  });

  it('create an instance', () => {
    const pipe = new UnitI18nPipe();
    expect(pipe).toBeTruthy();
  });

  it('should extract Title or Description attributes by default', () => {
    const pipe = new UnitI18nPipe();

    expect(pipe.transform(unit as Unit, 'Title')).toBe(unit.Title);
    expect(pipe.transform(unit as Unit, 'Description')).toBe(unit.Description);
  });

  it('should extract Title_.fr or Description_.fr if Title and Description are not set', () => {
    const pipe = new UnitI18nPipe();

    delete unit.Title;
    delete unit.Description;

    expect(pipe.transform(unit as Unit, 'Title')).toBe(unit.Title_.fr);
    expect(pipe.transform(unit as Unit, 'Description')).toBe(unit.Description_.fr);
  });

  it('should extract Title_.FR or Description_.FR if Title and Description are not set and no lower-case fr', () => {
    const pipe = new UnitI18nPipe();

    delete unit.Title;
    delete unit.Title_.fr;
    delete unit.Description;
    delete unit.Description_.fr;

    expect(pipe.transform(unit as Unit, 'Title')).toBe(unit.Title_.FR);
    expect(pipe.transform(unit as Unit, 'Description')).toBe(unit.Description_.FR);
  });

  it('should extract Title_.en or Description_.en if Title and Description are not set and no french', () => {
    const pipe = new UnitI18nPipe();

    delete unit.Title;
    delete unit.Title_.fr;
    delete unit.Title_.FR;
    delete unit.Description;
    delete unit.Description_.fr;
    delete unit.Description_.FR;

    expect(pipe.transform(unit as Unit, 'Title')).toBe(unit.Title_.en);
    expect(pipe.transform(unit as Unit, 'Description')).toBe(unit.Description_.en);
  });

  it('should extract Title_.EN or Description_.EN if Title and Description are not set, no french and no lower-case en', () => {
    const pipe = new UnitI18nPipe();

    delete unit.Title;
    delete unit.Title_.fr;
    delete unit.Title_.FR;
    delete unit.Title_.en;
    delete unit.Description;
    delete unit.Description_.fr;
    delete unit.Description_.FR;
    delete unit.Description_.en;

    expect(pipe.transform(unit as Unit, 'Title')).toBe(unit.Title_.EN);
    expect(pipe.transform(unit as Unit, 'Description')).toBe(unit.Description_.EN);
  });

  it('should extract Title_.es or Description_.es if Title and Description are not set, no french and no english', () => {
    const pipe = new UnitI18nPipe();

    delete unit.Title;
    delete unit.Title_.fr;
    delete unit.Title_.FR;
    delete unit.Title_.en;
    delete unit.Title_.EN;
    delete unit.Description;
    delete unit.Description_.fr;
    delete unit.Description_.FR;
    delete unit.Description_.en;
    delete unit.Description_.EN;

    expect(pipe.transform(unit as Unit, 'Title')).toBe(unit.Title_.es);
    expect(pipe.transform(unit as Unit, 'Description')).toBe(unit.Description_.es);
  });

  it('should return null if no Title/Description nor translated version of Title/Description', () => {
    const pipe = new UnitI18nPipe();

    delete unit.Title;
    delete unit.Title_.fr;
    delete unit.Title_.FR;
    delete unit.Title_.en;
    delete unit.Title_.EN;
    delete unit.Description;
    delete unit.Description_.fr;
    delete unit.Description_.FR;
    delete unit.Description_.en;
    delete unit.Description_.EN;

    expect(pipe.transform({} as Unit, 'Title')).toBe('');
    expect(pipe.transform({} as Unit, 'Description')).toBe('');
  });
});
