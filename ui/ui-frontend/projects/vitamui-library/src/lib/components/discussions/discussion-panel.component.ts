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
import { Component, computed, ElementRef, inject, input, signal, Signal, viewChild } from '@angular/core';
import { OverlayModule } from '@angular/cdk/overlay';
import { DiscussionDto, DiscussionEntity, DiscussionService } from './discussion.service';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';
import { DiscussionComponent } from './discussion/discussion.component';
import { CdkDragMove, DragDropModule } from '@angular/cdk/drag-drop';
import { DiscussionListComponent } from './discussion-list/discussion-list.component';
import { VitamUICommonModule } from '../../../app/modules/vitamui-common.module';
import { DiscussionTitleComponent } from './discussion-title/discussion-title.component';
import { DiscussionPanelService } from './discussion-panel.service';
import { DiscussionNewComponent } from './discussion-new/discussion-new.component';
import { DiscussionRenameComponent } from './discussion-rename/discussion-rename.component';

const MIN_WIDTH = 300;
const MIN_HEIGHT = 500;

@Component({
  selector: 'vitamui-discussion-panel',
  imports: [
    FormsModule,
    DiscussionComponent,
    DragDropModule,
    OverlayModule,
    DiscussionListComponent,
    VitamUICommonModule,
    DiscussionTitleComponent,
    DiscussionNewComponent,
    DiscussionRenameComponent,
  ],
  providers: [DiscussionPanelService],
  templateUrl: './discussion-panel.component.html',
  styleUrl: './discussion-panel.component.scss',
})
export class DiscussionPanelComponent {
  private discussionService = inject(DiscussionService);
  private discussionPanelService = inject(DiscussionPanelService);

  width = 400;
  height = 600;
  left = 400;
  expanded = this.discussionPanelService.expanded;
  opened = this.discussionPanelService.opened;
  componentState = this.discussionPanelService.componentState;
  resizeStartPosition?: { width: number; height: number };

  titleEl = viewChild<DiscussionTitleComponent, ElementRef<HTMLElement>>(DiscussionTitleComponent, {
    read: ElementRef<HTMLElement>,
  });

  private draggingLeftStart: number = undefined;

  onResizeStart() {
    this.resizeStartPosition = {
      width: this.width,
      height: this.height,
    };
  }

  onResize(event: CdkDragMove) {
    this.width = Math.min(window.innerWidth - this.left, Math.max(MIN_WIDTH, this.resizeStartPosition.width + event.distance.x));
    this.height = Math.min(window.innerHeight, Math.max(MIN_HEIGHT, this.resizeStartPosition.height - event.distance.y));
    event.source._dragRef.reset();
  }

  onResizeEnd() {
    setTimeout(() => (this.resizeStartPosition = undefined), 50); // Add some delay to make sur the "toggle" knows a resize was in progress
  }

  onTitleDragStart() {
    this.draggingLeftStart = this.left;
  }

  onTitleMove(event: CdkDragMove) {
    this.left = Math.min(window.innerWidth - this.width, Math.max(0, this.draggingLeftStart + event.distance.x));
    event.source._dragRef.reset();
  }

  onTitleDragEnd() {
    setTimeout(() => (this.draggingLeftStart = undefined), 50); // Add some delay to make sur the "toggle" knows a dragging was in progress
  }

  toggle() {
    if (this.draggingLeftStart === undefined && this.resizeStartPosition === undefined) {
      this.expanded.set(!this.expanded());
    }
  }

  discussionEntities = input.required<DiscussionEntity[]>();
  mainDiscussionEntity: Signal<DiscussionEntity> = computed(() =>
    this.discussionEntities().length > 1 ? this.discussionEntities().find((de) => de.main) : this.discussionEntities()[0],
  );
  discussions: Signal<DiscussionDto[]> = toSignal(
    toObservable(this.mainDiscussionEntity).pipe(
      filter((entity) => !!entity),
      switchMap((entity) => this.discussionService.findDiscussions(entity)),
      map((discussions) =>
        discussions.sort((d1, d2) => {
          if (d1.discussion.status !== d2.discussion.status) return d2.discussion.status === 'RESOLVED' ? -1 : 1;
          if (d1.unread !== d2.unread) return d1.unread ? -1 : 1;
          return new Date(d2.discussion.lastMessageAt).getTime() - new Date(d1.discussion.lastMessageAt).getTime();
        }),
      ),
    ),
    { initialValue: [] as DiscussionDto[] },
  );
  private readonly selectedDiscussionId = signal<string | null>(null);
  readonly currentDiscussion: Signal<DiscussionDto | undefined> = computed(() => {
    const id = this.selectedDiscussionId();
    const list = this.discussions();

    if (!id || !list.length) return null;
    return list.find((d) => d.discussion.id === id);
  });

  async createDiscussion({ title, text }: { title: string; text: string }) {
    const discussion = await this.discussionService.createDiscussion(this.discussionEntities(), title, text);
    this.openDiscussion(discussion.id);
  }

  protected openDiscussion(discussionId: string) {
    this.componentState.set('DISCUSSION');
    this.selectedDiscussionId.set(discussionId);
  }

  protected backToDiscussions() {
    this.componentState.set('LIST_DISCUSSIONS');
    this.closeDiscussion();
  }

  protected closeDiscussion() {
    this.selectedDiscussionId.set(null);
  }
}
