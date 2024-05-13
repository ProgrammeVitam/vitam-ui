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
