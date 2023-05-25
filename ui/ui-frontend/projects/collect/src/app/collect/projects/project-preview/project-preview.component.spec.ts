import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { ProjectsService } from '../projects.service';

import { ProjectPreviewComponent } from './project-preview.component';

describe('ProjectPreviewComponent', () => {
  let component: ProjectPreviewComponent;
  let fixture: ComponentFixture<ProjectPreviewComponent>;

  beforeEach(async () => {
    const projectServiceMock = {
      getBaseUrl: () => '/fake-api',
      getProjectById: () => of({ selectedProject: '' }),
    };

    await TestBed.configureTestingModule({
      declarations: [ProjectPreviewComponent],
      imports: [],
      providers: [{ provide: ProjectsService, useValue: projectServiceMock }],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
