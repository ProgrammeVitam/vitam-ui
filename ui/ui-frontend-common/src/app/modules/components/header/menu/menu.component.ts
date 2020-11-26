import { animate, keyframes, query, stagger, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { MatSelectionList, MatTabChangeEvent } from '@angular/material';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ApplicationService } from '../../../application.service';
import { Category } from '../../../models';
import { Application } from '../../../models/application/application.interface';
import { TenantSelectionService } from '../../../tenant-selection.service';
import { Tenant } from './../../../models/customer/tenant.interface';
import { StartupService } from './../../../startup.service';
import { MenuOverlayRef } from './menu-overlay-ref';

@Component({
  selector: 'vitamui-common-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
  animations: [
    trigger('opacityAnimation', [
      state('close', style({})),
      transition(':enter', [
        animate('500ms cubic-bezier(0, 0, 0.2, 1)', keyframes([
          style({ opacity: 0 }),
          style({ opacity: 1 }),
        ])),
      ]),
      transition('* => close', [
        animate('500ms cubic-bezier(0, 0, 0.2, 1)', keyframes([
          style({ opacity: 1 }),
          style({ opacity: 0 }),
        ])),
      ]),
    ]),
    trigger('slideLeftRight', [
      transition(':enter', [
        query('*', [
          style({ opacity: 0, transform: 'translateX(-20px)' }),
          stagger(50, [
            animate(
              '50ms',
              style({ opacity: 1, transform: 'none' })
            )
          ])
        ])
      ]),
      transition(':leave', [
        animate(
          '250ms',
          style({ opacity: 0, transform: 'translateX(+100px)' })
        )
      ])
    ])
  ]
})
export class MenuComponent implements OnInit, AfterViewInit, OnDestroy {

  public state = '';

  public appMap: Map<Category, Application[]>;

  public filteredApplications: Application[] = null;

  public criteria: string;

  public tabSelectedIndex = 0;

  public selectedCategory: Category;

  private firstResult: any;

  private firstResultFocused = false;

  public selectedTenant: Tenant;

  public tenants: Tenant[];

  private destroyer$ = new Subject();

  @ViewChildren(MatSelectionList) selectedList: QueryList<MatSelectionList>;
  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
      if (event.key === 'ArrowRight') {
        if (this.tabSelectedIndex < this.selectedList.length - 1) {
          this.tabSelectedIndex++;
        }
      } else if (event.key === 'ArrowLeft') {
        if (this.tabSelectedIndex > 0) {
          this.tabSelectedIndex--;
        }
      } else if (event.key === 'ArrowDown') {
        if (this.firstResult && !this.firstResultFocused) {
          this.firstResult.focus();
          this.firstResultFocused = true;
        }
      }
  }

  constructor(
    private dialogRef: MenuOverlayRef,
    private applicationService: ApplicationService,
    private cdrRef: ChangeDetectorRef,
    private router: Router,
    private tenantService: TenantSelectionService,
    private startupService: StartupService) { }

  ngOnInit() {
    this.dialogRef.overlay.backdropClick().subscribe(() => this.onClose());
    this.tenants = this.tenantService.getTenants();

    // Display application list depending on the current active tenant.
    // If no active tenant is set, then use the last tenant identifier.
    this.selectedTenant = this.tenantService.getSelectedTenant();
    if (this.selectedTenant) {
      this.updateApps(this.selectedTenant);
    } else {
      this.tenantService.getLastTenantIdentifier$().pipe(takeUntil(this.destroyer$)).subscribe((identifier: number) => {
        this.updateApps(this.tenants.find(value => value.identifier === identifier));
      });
    }
  }

  ngAfterViewInit(): void {
    this.changeTabFocus();
  }

  ngOnDestroy() {
    this.destroyer$.next();
  }

  public onSearch(value: string): void {
    if (value) {
      this.criteria = value;
      this.firstResultFocused = false;
      const flattenApps: Application[] = Array.from(new Set([].concat.apply([], Array.from(this.appMap.values()))));

      this.filteredApplications = flattenApps.filter((application: Application) => {
        return application.name.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
          .includes(value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase());
      });
      this.cdrRef.detectChanges();

      if (this.filteredApplications.length > 0) {
        this.firstResult = document.getElementById('searchResults').firstElementChild as any;
      }
    } else {
      this.resetSearch();
    }
  }

  public resetSearch(): void {
    this.filteredApplications = null;
    this.criteria = '';
    this.changeTabFocus();
  }

  public onClose(): void {
    this.state = 'close';
    setTimeout(() => this.dialogRef.close(), 500);
  }

  public changeTabFocus(value?: MatTabChangeEvent): void {
    if (value && value.index !== this.tabSelectedIndex) {
      this.tabSelectedIndex = value.index; // when clicking
    }
    setTimeout(() => {
      // tslint:disable-next-line: variable-name
      const firstElem = this.selectedList.find((_select, index) => index === this.tabSelectedIndex);
      if (firstElem && firstElem.options && firstElem.options.first) {
        firstElem.options.first.focus();
      }
    }, 300);
  }

  public openApplication(app: Application) {
    this.onClose();
    this.applicationService.
      openApplication(app, this.router, this.startupService.getConfigStringValue('UI_URL'), this.selectedTenant.identifier);
  }

  public updateApps(tenant: Tenant) {
    if (tenant) {
      this.selectedTenant = tenant;
      this.appMap = this.applicationService.getTenantAppMap(tenant);
    }
  }

}
