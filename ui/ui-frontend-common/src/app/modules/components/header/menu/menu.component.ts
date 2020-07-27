import { animate, keyframes, query, stagger, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit, QueryList, ViewChildren } from '@angular/core';
import { MatSelectionList, MatTabChangeEvent } from '@angular/material';
import { Router } from '@angular/router';
import * as _ from 'lodash';
import { Subscription } from 'rxjs';
import { ApplicationService } from '../../../application.service';
import { Category } from '../../../models';
import { Application } from '../../../models/application/application.interface';
import { StartupService } from '../../../startup.service';
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

  private unSub: Subscription;

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
      }
  }

  constructor(
    private dialogRef: MenuOverlayRef,
    private applicationService: ApplicationService,
    private startupService: StartupService,
    private cdrRef: ChangeDetectorRef,
    private router: Router) { }

  ngAfterViewInit(): void {
    this.changeTabFocus();
  }

  ngOnInit() {
    this.unSub = this.applicationService.getAppsGroupByCategories()
      .subscribe((map: Map<Category, Application[]>) => {
        this.appMap = map;
      });
    this.dialogRef.overlay.backdropClick().subscribe(() => this.onClose());
  }

  ngOnDestroy() {
    this.unSub.unsubscribe();
  }

  public onSearch(value: string): void {
    if (value) {
      this.criteria = value;
      this.filteredApplications = this.appMap.get(Array.from(this.appMap.keys())[this.tabSelectedIndex])
        .filter((application: Application) =>
        application.name.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
        .includes(value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()));
      this.cdrRef.detectChanges();

      if (this.filteredApplications.length > 0) {
        const firstChildElem = document.getElementById('searchResults').firstElementChild as any;
        if (firstChildElem) {
          firstChildElem.focus();
        }
      }
    } else {
      this.filteredApplications = null;
      this.criteria = '';
      this.changeTabFocus();
    }
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
      const firstElem = this.selectedList.find((_select, index) => index === this.tabSelectedIndex).options.first;
      if (firstElem) {
        firstElem.focus();
      }
    }, 300);
  }

  public openApplication(app: Application) {
    this.onClose();
    this.applicationService.openApplication(app);
  }
}
