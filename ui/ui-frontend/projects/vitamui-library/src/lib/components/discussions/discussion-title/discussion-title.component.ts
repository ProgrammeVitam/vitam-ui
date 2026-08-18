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
import { Component, inject, input, output } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { VitamUICommonModule } from '../../../../app/modules/vitamui-common.module';
import { TranslatePipe } from '@ngx-translate/core';
import { CdkDragMove, DragDropModule } from '@angular/cdk/drag-drop';
import { MatMenuItem, MatMenuModule } from '@angular/material/menu';
import { DiscussionPanelService } from '../discussion-panel.service';
import { Discussion, DiscussionDto, DiscussionService } from '../discussion.service';
import { VitamuiMenuButtonComponent } from '../../../../app/modules/components/vitamui-menu-button/vitamui-menu-button.component';
import { DiscussionIconComponent } from '../discussion-icon/discussion-icon.component';

@Component({
  selector: 'vitamui-discussion-title',
  imports: [
    VitamuiMenuButtonComponent,
    TranslatePipe,
    NgTemplateOutlet,
    VitamUICommonModule,
    DragDropModule,
    MatMenuModule,
    MatMenuItem,
    DiscussionIconComponent,
  ],
  templateUrl: './discussion-title.component.html',
  styleUrl: './discussion-title.component.scss',
})
export class DiscussionTitleComponent {
  private discussionService = inject(DiscussionService);
  private discussionPanelService = inject(DiscussionPanelService);
  componentState = this.discussionPanelService.componentState;
  opened = this.discussionPanelService.opened;
  expanded = this.discussionPanelService.expanded;

  discussions = input.required<DiscussionDto[]>();
  currentDiscussion = input.required<Discussion | undefined>();
  panelResizeStart = output<void>();
  panelResize = output<CdkDragMove>();
  panelResizeEnd = output<void>();
  backToDiscussions = output<void>();

  back($event: MouseEvent) {
    $event.stopPropagation();
    if (this.componentState() === 'RENAME_DISCUSSION') {
      this.discussionPanelService.componentState.set('DISCUSSION');
    } else {
      this.backToDiscussions.emit();
    }
  }

  closeDiscussion() {
    this.discussionService.resolveDiscussion(this.currentDiscussion());
    this.backToDiscussions.emit();
  }

  renameDiscussion() {
    this.componentState.set('RENAME_DISCUSSION');
  }
}
