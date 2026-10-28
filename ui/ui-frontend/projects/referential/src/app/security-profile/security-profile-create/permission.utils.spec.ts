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

import { Permission, PermissionStructure, PermissionUtils } from './permission.utils';

const perm = (activated: boolean, extra: Partial<Permission> = {}): Permission => ({
  activated,
  description: 'desc',
  ...extra,
});

describe('PermissionUtils', () => {
  let utils: PermissionUtils;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    utils = TestBed.inject(PermissionUtils);
  });

  describe('getPermissionString', () => {
    it('should use service and default permission when there is no override', () => {
      expect(utils.getPermissionString('units', perm(true), 'read')).toBe('units:read');
    });

    it('should use service and overridePermission when set', () => {
      expect(utils.getPermissionString('units:id', perm(true, { overridePermission: 'read:json' }), 'read')).toBe('units:id:read:json');
    });

    it('should return overrideApiPermission as is, taking precedence over overridePermission', () => {
      const permission = perm(true, { overridePermission: 'read:json', overrideApiPermission: 'removeforcepause:check' });
      expect(utils.getPermissionString('forcepause', permission, 'delete')).toBe('removeforcepause:check');
    });

    it('should ignore empty/null overrides', () => {
      expect(utils.getPermissionString('agencies', perm(true, { overridePermission: null, overrideApiPermission: '' }), 'update')).toBe(
        'agencies:update',
      );
    });
  });

  describe('getInitPermissions', () => {
    it.each([true, false])('should set every defined permission to activated=%s', (defaultPermission) => {
      const structure = utils.getInitPermissions(defaultPermission);
      const all = structure.apiPermissions.flatMap((p) => [p.readPermission, p.writePermission, p.deletePermission]).filter(Boolean);

      expect(all.length).toBeGreaterThan(0);
      expect(all.every((p) => p.activated === defaultPermission)).toBe(true);
    });

    it('should return a fresh structure on each call', () => {
      const first = utils.getInitPermissions(false);
      first.apiPermissions[0].readPermission.activated = true;

      expect(utils.getInitPermissions(false).apiPermissions[0].readPermission.activated).toBe(false);
    });
  });

  describe('getPermissionsAsList', () => {
    it('should list only activated permissions, skipping null ones, with read/update/delete defaults', () => {
      const structure: PermissionStructure = {
        apiPermissions: [
          {
            service: 'units',
            type: 'units',
            name: 'Units',
            readPermission: perm(true),
            writePermission: perm(false),
            deletePermission: null,
          },
          {
            service: 'objects',
            type: 'objects',
            name: 'Objects',
            readPermission: null,
            writePermission: perm(true, { overridePermission: 'create' }),
            deletePermission: perm(true),
          },
          {
            service: 'forcepause',
            type: 'forcepause',
            name: 'Pause',
            readPermission: undefined,
            writePermission: undefined,
            deletePermission: perm(true, { overrideApiPermission: 'removeforcepause:check' }),
          },
        ],
      };

      expect(utils.getPermissionsAsList(structure)).toEqual(['units:read', 'objects:create', 'objects:delete', 'removeforcepause:check']);
    });

    it('should return an empty list when nothing is activated', () => {
      expect(utils.getPermissionsAsList(utils.getInitPermissions(false))).toEqual([]);
    });

    it('should include known overridden permissions when everything is activated', () => {
      const list = utils.getPermissionsAsList(utils.getInitPermissions(true));

      expect(list).toContain('managementcontracts:read');
      expect(list).toContain('managementcontracts:create:json');
      expect(list).toContain('dipexport:id:dip:read');
      expect(list).toContain('transfers:reply');
      expect(list).toContain('units:id:objects:read:binary');
      expect(list).toContain('removeforcepause:check');
    });
  });

  describe('getFormConfigFromPermission', () => {
    it('should map each non-null permission string to its activation flag', () => {
      const structure: PermissionStructure = {
        apiPermissions: [
          {
            service: 'units',
            type: 'units',
            name: 'Units',
            readPermission: perm(true),
            writePermission: perm(false, { overridePermission: 'rules' }),
            deletePermission: null,
          },
          {
            service: 'x',
            type: 'x',
            name: 'X',
            readPermission: null,
            writePermission: null,
            deletePermission: perm(false, { overrideApiPermission: 'api:custom' }),
          },
        ],
      };

      expect(utils.getFormConfigFromPermission(structure)).toEqual({
        'units:read': true,
        'units:rules': false,
        'api:custom': false,
      });
    });

    it('should contain one key per permission of the init structure', () => {
      const structure = utils.getInitPermissions(false);
      const config = utils.getFormConfigFromPermission(structure);
      const definedPermissions = structure.apiPermissions
        .flatMap((p) => [p.readPermission, p.writePermission, p.deletePermission])
        .filter(Boolean);

      expect(Object.keys(config).length).toBe(definedPermissions.length);
      expect(Object.values(config).every((v) => v === false)).toBe(true);
    });
  });

  describe('getPermissionsFromList', () => {
    it('should activate only the permissions present in the list', () => {
      const structure = utils.getPermissionsFromList(['units:read', 'transfers:reply', 'removeforcepause:check', 'unknown:perm']);

      const activated = utils.getPermissionsAsList(structure);
      expect(activated.sort()).toEqual(['removeforcepause:check', 'transfers:reply', 'units:read']);

      const transfersReply = structure.apiPermissions.find((p) => p.name === 'Transfert de SIP (Reply)');
      expect(transfersReply.writePermission.activated).toBe(true);
      const transfers = structure.apiPermissions.find((p) => p.name === 'Transfert de SIP');
      expect(transfers.writePermission.activated).toBe(false);
      expect(transfers.readPermission.activated).toBe(false);
    });

    it('should return a fully deactivated structure for an empty list', () => {
      expect(utils.getPermissionsFromList([])).toEqual(utils.getInitPermissions(false));
    });

    it('should round-trip the full set of permissions', () => {
      const all = utils.getInitPermissions(true);
      expect(utils.getPermissionsFromList(utils.getPermissionsAsList(all))).toEqual(all);
    });

    it('should preserve activation of every single permission through a round-trip', () => {
      const definedCount = (s: PermissionStructure) =>
        s.apiPermissions.flatMap((p) => [p.readPermission, p.writePermission, p.deletePermission]).filter(Boolean).length;
      const init = utils.getInitPermissions(false);

      for (let i = 0; i < init.apiPermissions.length; i++) {
        for (const key of ['readPermission', 'writePermission', 'deletePermission'] as const) {
          const source = utils.getInitPermissions(false);
          if (!source.apiPermissions[i][key]) {
            continue;
          }
          source.apiPermissions[i][key].activated = true;

          const roundTripped = utils.getPermissionsFromList(utils.getPermissionsAsList(source));

          expect(roundTripped, `${source.apiPermissions[i].name} ${key}`).toEqual(source);
        }
      }
      expect(definedCount(init)).toBeGreaterThan(0);
    });
  });
});
