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
import { Observable, Subject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  AdminUserProfile,
  AuthUser,
  Criterion,
  Operators,
  Profile,
  SearchQuery,
  SearchService,
  User,
  VitamUISnackBarService,
} from 'ui-frontend-common';

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { UserApiService } from '../core/api/user-api.service';
import { ProfileService } from '../profile/profile.service';

@Injectable()
export class UserService extends SearchService<User> {
  userUpdated = new Subject<User>();

  constructor(
    private userApi: UserApiService,
    private snackBarService: VitamUISnackBarService,
    private rngProfileService: ProfileService,
    http: HttpClient,
  ) {
    super(http, userApi, '');
  }

  create(user: User) {
    user.email = user.email;

    return this.userApi.create(user).pipe(
      tap(
        (response: User) => {
          this.snackBarService.open({
            message: 'SHARED.SNACKBAR.USER_CREATE',
            icon: 'vitamui-icon-key',
            translateParams: {
              param1: response.firstname,
              param2: response.lastname,
            },
          });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  get(id: string): Observable<User> {
    return this.userApi.getOne(id);
  }

  exists(email: string): Observable<any> {
    const criterionArray: Criterion[] = [];
    criterionArray.push({ key: 'email', value: email, operator: Operators.equalsIgnoreCase });
    const query: SearchQuery = { criteria: criterionArray };

    const params = [{ key: 'criteria', value: JSON.stringify(query) }];

    return this.userApi.checkExistsByParam(params);
  }

  patch(partialUser: { id: string; [key: string]: any }): Observable<User> {
    return this.userApi.patch(partialUser).pipe(
      tap((response) => this.userUpdated.next(response)),
      tap(
        (updatedUser: User) => {
          this.snackBarService.open({
            message: 'SHARED.SNACKBAR.USER_UPDATE',
            icon: 'vitamui-icon-key',
            translateParams: {
              param1: updatedUser.firstname,
              param2: updatedUser.lastname,
            },
          });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  deleteUser(partialUser: { id: string; [key: string]: any }): Observable<User> {
    return this.userApi.patch(partialUser).pipe(
      tap((response) => this.userUpdated.next(response)),
      tap(
        (user: User) => {
          this.snackBarService.open({
            message: 'SHARED.SNACKBAR.USER_DELETE',
            icon: 'vitamui-icon-key',
            translateParams: { param1: user.firstname, param2: user.lastname },
          });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  getUserProfileInfo(connectedUser: AuthUser): AdminUserProfile | null {
    let userInfo = null;
    connectedUser.profileGroup.profiles.forEach((profile: Profile) => {
      if (profile.applicationName === 'USERS_APP') {
        userInfo = this.rngProfileService.convertToAdminUserProfile(profile.roles);
      }
    });

    return userInfo;
  }

  getLevelsNoEmpty(query?: SearchQuery): Observable<string[]> {
    return this.userApi.getLevels(query).pipe(map((levels) => levels.filter((l) => !!l)));
  }
}
