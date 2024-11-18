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
import { TestBed } from '@angular/core/testing';
import { PathService } from './path.service';

describe('PathService', () => {
  let service: PathService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PathService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should convert to dot notation a path', () => {
    expect(service.dot('a[0]')).toEqual('a.0');
    expect(service.dot('a.b.c[0]')).toEqual('a.b.c.0');
    expect(service.dot('a[0].b[1].c[2]')).toEqual('a.0.b.1.c.2');
  });

  describe('Children', () => {
    it('should find path children', () => {
      const paths = ['A', 'B', 'B.B', 'C', 'A.A', 'A.A.A', 'A.A.B', 'A.A.C'];

      expect(service.children('A.A', paths)).toEqual(['A.A.A', 'A.A.B', 'A.A.C']);
      expect(service.children('', paths)).toContain('A');
      expect(service.children('', paths)).toContain('B');
      expect(service.children('', paths)).toContain('C');
    });

    it('should find path children from an object', () => {
      const data = {
        A: {
          B: {
            C: {},
          },
        },
        D: {
          E: {},
          F: {},
        },
      };
      const paths = service.paths(data);

      expect(service.children('', paths)).toEqual(['A', 'D']);
      expect(service.children('A', paths)).toEqual(['A.B']);
      expect(service.children('A.B', paths)).toEqual(['A.B.C']);
      expect(service.children('D', paths)).toEqual(['D.E', 'D.F']);
      expect(service.children('D.E', paths)).toEqual([]);
      expect(service.children('Z', paths)).toEqual([]);
    });

    it('should find path children from an object with arrays', () => {
      const data = {
        A: {
          B: {
            C: {},
          },
        },
        D: [{ E: {} }, { F: {} }],
      };
      const paths = service.paths(data);

      expect(service.children('', paths)).toEqual(['A', 'D[0]', 'D[1]']);
      expect(service.children('A', paths)).toEqual(['A.B']);
      expect(service.children('A.B', paths)).toEqual(['A.B.C']);
      expect(service.children('D', paths)).toEqual(['D[0]', 'D[1]']);
      expect(service.children('D[0]', paths)).toEqual(['D[0].E']);
      expect(service.children('Z', paths)).toEqual([]);
    });

    it('should find path children from an object with arrays less array notation', () => {
      const data = {
        A: {
          B: {
            C: {},
          },
        },
        D: [{ E: {} }, { F: {} }],
      };
      const paths = service.paths(data, { arrayNotation: false });

      expect(service.children('', paths)).toEqual(['A', 'D']);
      expect(service.children('A', paths)).toEqual(['A.B']);
      expect(service.children('A.B', paths)).toEqual(['A.B.C']);
      expect(service.children('D', paths)).toEqual(['D.0', 'D.1']);
      expect(service.children('D.0', paths)).toEqual(['D.0.E']);
      expect(service.children('Z', paths)).toEqual([]);
    });
  });

  describe('Paths', () => {
    it('should give simple object paths', () => {
      const data = { title: 'hello' };
      const paths = service.paths(data);
      const expected = ['title'];

      expect(paths).toEqual(expected);
    });

    it('should give nested object paths', () => {
      const data = {
        A: {
          B: {
            C: {},
          },
        },
        D: {
          E: {},
          F: {},
        },
      };
      const paths = service.paths(data);
      const expected = ['A', 'A.B', 'A.B.C', 'D', 'D.E', 'D.F'];

      expect(paths).toEqual(expected);
    });
  });

  describe('Entries', () => {
    it('should give nested object paths', () => {
      const data = {
        A: {
          B: {
            C: {},
          },
        },
        D: [{ E: {} }, { F: {} }],
      };
      const entries = service.entries(data);

      expect(entries).toContain({
        key: 'A',
        value: {
          B: {
            C: {},
          },
        },
      });
      expect(entries).toContain({
        key: 'A.B',
        value: {
          C: {},
        },
      });
      expect(entries).toContain({
        key: 'A.B.C',
        value: {},
      });
      expect(entries).toContain({
        key: 'D[0]',
        value: { E: {} },
      });
      expect(entries).toContain({
        key: 'D[1]',
        value: { F: {} },
      });
      expect(entries).toContain({
        key: 'D[0].E',
        value: {},
      });
      expect(entries).toContain({
        key: 'D[1].F',
        value: {},
      });
    });
  });

  describe('Value', () => {
    it('should retrieve simple value', () => {
      expect(service.value({ name: 'john' }, 'name')).toEqual('john');
    });

    it('should retrieve nested value', () => {
      expect(service.value({ user: { name: 'john' } }, 'user.name')).toEqual('john');
    });

    it('should retrieve array value', () => {
      expect(service.value(['apple', 'cherry', 'strawberry'], '[1]')).toEqual('cherry');
    });

    it('should retrieve nested array value', () => {
      expect(service.value([[[0], [1], [2]]], '[0][1][0]')).toEqual(1);
    });

    it('should retrieve complex value', () => {
      const clouds = [
        {
          name: 'cloud',
          devices: [
            {
              type: 'computer',
              cpu: {
                name: 'intel xeon',
                cores: 12,
              },
            },
            {
              type: 'computer',
              cpu: {
                name: 'intel itanium',
                cores: 8,
              },
            },
            {
              type: 'computer',
              cpu: {
                name: 'intel 2 quad',
                cores: 4,
              },
            },
          ],
        },
      ];

      expect(service.value(clouds, '[0].devices[1].cpu.cores')).toEqual(8);
      expect(service.value(clouds, '[0].devices')).toEqual([
        {
          type: 'computer',
          cpu: {
            name: 'intel xeon',
            cores: 12,
          },
        },
        {
          type: 'computer',
          cpu: {
            name: 'intel itanium',
            cores: 8,
          },
        },
        {
          type: 'computer',
          cpu: {
            name: 'intel 2 quad',
            cores: 4,
          },
        },
      ]);
    });
  });
});
