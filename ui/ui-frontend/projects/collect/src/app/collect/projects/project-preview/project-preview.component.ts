import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Observable } from 'rxjs';
import { Project } from 'ui-frontend-common';
import { ProjectsService } from '../projects.service';

@Component({
  selector: 'app-project-preview',
  templateUrl: './project-preview.component.html',
  styleUrls: ['./project-preview.component.scss'],
})
export class ProjectPreviewComponent implements OnInit {
  @Output()
  backToNormalLateralPanel: EventEmitter<any> = new EventEmitter();
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Output()
  showExtendedLateralPanel: EventEmitter<any> = new EventEmitter();

  @Input()
  get projectId(): string {
    return this._projectId;
  }
  set projectId(value: string) {
    this.selectedProject = this.projectService.getProjectById(value);
    this.selectedTabIndex = 0;
  }
  private _projectId: string;

  updateStarted: false;
  isPanelextended = false;
  selectedTabIndex = 0;
  selectedProject = new Observable<Project>();

  constructor(private projectService: ProjectsService) {}

  ngOnInit(): void {}

  emitClose() {
    this.isPanelextended = false;
    this.previewClose.emit();
    this.backToNormalLateralPanel.emit();
    this.selectedTabIndex = 0;
  }

  showNormalPanel() {
    this.isPanelextended = false;
    this.backToNormalLateralPanel.emit();
  }

  showExtendedPanel() {
    this.isPanelextended = true;
    this.showExtendedLateralPanel.emit();
  }
}
