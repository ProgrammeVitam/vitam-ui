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
import { AfterViewInit, Component, ElementRef, inject, input, OnInit, signal, Signal, viewChild } from '@angular/core';
import { DiscussionDto, DiscussionService, Message } from '../discussion.service';
import { MessageComponent } from './message/message.component';
import { ContentDividerComponent } from '../content-divider/content-divider.component';
import { MessageNewComponent } from './message-new/message-new.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'vitamui-discussion',
  imports: [MessageComponent, ContentDividerComponent, MessageNewComponent, TranslatePipe],
  templateUrl: './discussion.component.html',
  styleUrl: './discussion.component.scss',
})
export class DiscussionComponent implements OnInit, AfterViewInit {
  private discussionService = inject(DiscussionService);

  discussion = input.required<DiscussionDto>();

  lastReadAt = signal<Date>(undefined);

  scrollWrapper: Signal<ElementRef> = viewChild('scrollWrapper');
  contentDivider: Signal<ElementRef> = viewChild(ContentDividerComponent, { read: ElementRef });

  ngOnInit() {
    this.lastReadAt.set(this.discussion().lastReadAt);
    this.discussionService.markAsRead(this.discussion());
  }

  ngAfterViewInit() {
    const scrollWrapper = this.scrollWrapper().nativeElement;
    const contentDivider = this.contentDivider()?.nativeElement;
    scrollWrapper.scrollTo({
      top: contentDivider ? contentDivider.offsetTop - scrollWrapper.offsetTop : scrollWrapper.scrollHeight,
      behavior: 'smooth',
    });
  }

  scrollToBottom() {
    setTimeout(() => {
      const scrollWrapper = this.scrollWrapper().nativeElement;
      scrollWrapper.scrollTo({
        top: scrollWrapper.scrollHeight,
        behavior: 'smooth',
      });
    }, 100);
  }

  displayNewMessageSeparator(message: Message): boolean {
    const messageDate = new Date(message.createdAt).getTime();
    const lastReadDate = new Date(this.lastReadAt() || 0).getTime();
    const messages = this.discussion().discussion.messages;
    const messageIndex = messages.indexOf(message);
    const first = messageIndex === 0;
    return !first && messageDate > lastReadDate && (first || new Date(messages[messageIndex - 1].createdAt).getTime() < lastReadDate);
  }

  markUnread(message: Message) {
    this.lastReadAt.set(new Date(new Date(message.createdAt).getTime() - 1));
    this.discussionService.markAsUnread(this.discussion(), message);
  }
}
