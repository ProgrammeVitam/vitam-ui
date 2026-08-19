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
import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { DownloadSnackBarService } from 'projects/referential/src/app/core/service/download-snack-bar.service';
import { Subscription } from 'rxjs';
import { finalize } from 'rxjs/operators';
import {
  AdminUserProfile,
  AuthService,
  Customer,
  DEFAULT_PAGE_SIZE,
  Direction,
  GlobalEventService,
  Group,
  PageRequest,
  SidenavPage,
  User,
  SnackBarService,
} from 'vitamui-library';
import { CustomerService } from '../core/customer.service';
import { GroupService } from '../group/group.service';
import { UserCreateComponent } from './user-create/user-create.component';
import { UserListComponent } from './user-list/user-list.component';
import { UserService } from './user.service';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss'],
  standalone: false,
})
export class UserComponent extends SidenavPage<User> implements OnInit {
  dialog = inject(MatDialog);
  userService = inject(UserService);
  route: ActivatedRoute;
  customerService = inject(CustomerService);
  override globalEventService: GlobalEventService;
  groupService = inject(GroupService);
  private authService = inject(AuthService);
  private downloadSnackBarService = inject(DownloadSnackBarService);
  private snackBarService = inject(SnackBarService);

  public users: User[];
  public connectedUserInfo: AdminUserProfile;
  public customer: Customer;
  public search: string;
  public groups: Group[];
  public exportLoading = false;

  @ViewChild(UserListComponent, { static: true }) userListComponent: UserListComponent;

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);

    this.route = route;
    this.globalEventService = globalEventService;
  }

  ngOnInit() {
    this.customerService.getMyCustomer().subscribe((customer) => (this.customer = customer));
    this.groupService.getAll(true).subscribe((data: Group[]) => (this.groups = data));
    this.connectedUserInfo = this.userService.getUserProfileInfo(this.authService.user);
  }

  openCreateUserDialog(): void {
    const dialogRef = this.dialog.open(UserCreateComponent, {
      disableClose: true,
      data: { userInfo: this.connectedUserInfo, customer: this.customer, groups: this.groups },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.refreshList();
      }
    });
  }

  onSearchSubmit(search: string): void {
    this.search = search;
  }

  public export(): void {
    this.exportLoading = true;
    this.downloadSnackBarService.openDownloadBar();

    const request: Subscription = this.userService
      .prepareSignedExport()
      .pipe(
        finalize(() => {
          this.downloadSnackBarService.close();
          this.exportLoading = false;
        }),
      )
      .subscribe({
        next: (url) => this.snackBarService.startDownload(url),
      });

    this.downloadSnackBarService.cancelDownload.subscribe(() => request.unsubscribe());
  }

  private refreshList() {
    if (!this.userListComponent) {
      return;
    }
    this.userListComponent.search(new PageRequest(0, DEFAULT_PAGE_SIZE, 'lastname', Direction.ASCENDANT));
  }
}
