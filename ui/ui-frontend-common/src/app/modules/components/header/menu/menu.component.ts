import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  HostListener,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { MatSelectionList, MatSelectionListChange } from '@angular/material/list';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { take, takeUntil } from 'rxjs/operators';
import { opacityAnimation, slideAnimation } from '../../../animations';
import { ApplicationService } from '../../../application.service';
import { Category } from '../../../models';
import { Application } from '../../../models/application/application.interface';
import { StartupService } from '../../../startup.service';
import { TenantSelectionService } from '../../../tenant-selection.service';
import { MenuOption } from '../../navbar';
import { SearchBarComponent } from '../../search-bar';
import { Tenant } from './../../../models/customer/tenant.interface';
import { MenuOverlayRef } from './menu-overlay-ref';

const APPLICATION_TRANSLATE_PATH = 'APPLICATION';

interface NgxTranslateApp {
  identifier: string;
  name: string;
}

@Component({
  selector: 'vitamui-common-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss'],
  animations: [opacityAnimation, slideAnimation],
})
export class MenuComponent implements OnInit, AfterViewInit, OnDestroy {
  public state = '';
  public appMap: Map<Category, Application[]>;
  public filteredApplications: Application[];
  public criteria: string;
  public tabSelectedIndex = 0;
  public selectedCategory: Category;
  public selectedTenant: MenuOption;
  public tenants: MenuOption[];

  private firstResult: any;
  private firstResultFocused = false;
  private destroyer$ = new Subject();
  private ngxAppArray: NgxTranslateApp[] = []; // Translated apps array from en / fr json files

  @ViewChild('searchBar', { static: true }) searchBar: SearchBarComponent;
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
    private tenantService: TenantSelectionService,
    private translateService: TranslateService,
    private router: Router,
    private startupService: StartupService,
  ) {}

  ngOnInit() {
    this.dialogRef.overlay
      .backdropClick()
      .pipe(takeUntil(this.destroyer$))
      .subscribe(() => this.onClose());
    this.tenants = this.tenantService.getTenants().map((tenant: Tenant) => {
      return { value: tenant, label: tenant.name };
    });

    // Display application list depending on the current active tenant.
    // If no active tenant is set, then use the last tenant identifier.
    this.tenantService
      .getSelectedTenant$()
      .pipe(take(1))
      .subscribe((tenant: Tenant) => {
        if (tenant) {
          this.selectedTenant = { value: tenant, label: tenant.name };
          this.updateApps(this.selectedTenant);
        } else {
          this.tenantService
            .getLastTenantIdentifier$()
            .pipe(takeUntil(this.destroyer$))
            .subscribe((identifier: number) => {
              this.updateApps(this.tenants.find((option) => option.value.identifier === identifier));
            });
        }
      });

    // Get the list of translated apps from en / fr json files for research
    this.translateService
      .get(APPLICATION_TRANSLATE_PATH)
      .pipe(take(1))
      .subscribe((translatedApps: any) => {
        for (const [key, value] of Object.entries(translatedApps)) {
          this.ngxAppArray.push({ identifier: key, name: (value as any).NAME });
        }
      });
  }

  ngAfterViewInit(): void {
    this.searchBar.onFocus();
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public onSearch(value: string): void {
    if (value) {
      this.criteria = value;
      this.firstResultFocused = false;

      // Search value in the translated apps array
      const filteredNgxTranslateApps = this.ngxAppArray.filter((app: NgxTranslateApp) => {
        return app.name
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .toLowerCase()
          .includes(
            value
              .normalize('NFD')
              .replace(/[\u0300-\u036f]/g, '')
              .toLowerCase(),
          );
      });

      // Avoid maping array by identifier inside filter
      const mappedArray = filteredNgxTranslateApps.map((ngxApp: NgxTranslateApp) => ngxApp.identifier);
      const flattenApps: Application[] = Array.from(new Set([].concat.apply([], Array.from(this.appMap.values()))));

      this.filteredApplications = flattenApps.filter((app: Application) => {
        return mappedArray.includes(app.identifier);
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
    this.searchBar.onFocus();
  }

  public onClose(event?: MatSelectionListChange): void {
    if (event) {
      this.openApplication(event.options[0].value);
    }

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

  public updateApps(tenant: MenuOption): void {
    if (tenant) {
      this.selectedTenant = tenant;
      this.appMap = this.applicationService.getTenantAppMap(tenant.value);
    }
  }

  public getApplicationUrl(application: Application): string {
    return this.applicationService.getApplicationUrl(application, this.selectedTenant.value.identifier);
  }

  public openApplication(application: Application): void {
    this.applicationService.openApplication(
      application,
      this.router,
      this.startupService.getConfigStringValue('UI_URL'),
      this.selectedTenant.value.identifier,
    );

    this.onClose();
  }
}
