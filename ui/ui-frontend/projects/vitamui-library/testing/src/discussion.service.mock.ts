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
import { Discussion, DiscussionDto, DiscussionEntity, Message } from 'vitamui-library';
import { BehaviorSubject, Observable } from 'rxjs';

export class DiscussionServiceMock {
  #discussionId = 0;
  #messageId = 0;

  #discussion({ title, read, closed, me }: { title: string; read: boolean; closed: boolean; me: boolean }): DiscussionDto {
    this.#discussionId++;
    const now = new Date();
    return {
      discussion: {
        id: `${this.#discussionId}`,
        title,
        entities: [{ entityType: 'mockType', entityId: 'mockId' }],
        createdAt: now,
        lastMessageAt: now,
        messages: [
          {
            id: `${this.#messageId++}`,
            userId: 'me',
            text: 'Ancien message',
            createdAt: new Date(now.getTime() - 3_600_000),
            updatedAt: undefined,
            deletedAt: undefined,
          },
          {
            id: `${this.#messageId++}`,
            userId: me ? 'me' : 'mockUser',
            text: 'Extrait du dernier message, une ligne maximum',
            createdAt: now,
            updatedAt: undefined,
            deletedAt: undefined,
          },
        ],
        status: closed ? 'RESOLVED' : 'IN_PROGRESS',
      },
      lastReadAt: now,
      unread: !read,
    };
  }

  #discussions$: BehaviorSubject<DiscussionDto[]> = new BehaviorSubject<DiscussionDto[]>([
    this.#discussion({
      title: `Sujet de la discussion pouvant faire 2 lignes maximum. Si c'est trop long, ça coupe`,
      read: false,
      closed: false,
      me: false,
    }),
    this.#discussion({ title: 'Sujet de la discussion', read: true, closed: false, me: true }),
    this.#discussion({ title: 'Sujet de la discussion fermée', read: true, closed: true, me: true }),
    this.#discussion({ title: 'Sujet de la discussion fermée 2', read: true, closed: true, me: true }),
    this.#discussion({ title: 'Sujet de la discussion fermée 3', read: true, closed: true, me: true }),
    this.#discussion({ title: 'Sujet de la discussion fermée 4', read: true, closed: true, me: true }),
  ]);

  findDiscussions(_entity: DiscussionEntity): Observable<DiscussionDto[]> {
    return this.#discussions$.asObservable();
  }

  createDiscussion({ type: entityType, id: entityId }: DiscussionEntity, title: string, message: string): Promise<Discussion> {
    this.#discussionId++;
    const date = new Date();
    const newDiscussion: DiscussionDto = {
      discussion: {
        id: `${this.#discussionId}`,
        title: title,
        entities: [{ entityType, entityId }],
        createdAt: date,
        lastMessageAt: date,
        messages: [
          {
            id: `${this.#messageId++}`,
            userId: 'me',
            text: message,
            createdAt: date,
            updatedAt: undefined,
            deletedAt: undefined,
          },
        ],
        status: 'IN_PROGRESS',
      },
      lastReadAt: date,
      unread: false,
    };
    this.#discussions$.next([...this.#discussions$.getValue(), newDiscussion]);
    return Promise.resolve(newDiscussion.discussion);
  }

  renameDiscussion(discussion: Discussion, title: string): Promise<void> {
    return new Promise((resolve) =>
      setTimeout(() => {
        const discussions = this.#discussions$.getValue();
        const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.id).discussion;
        discussionToUpdate.title = title;

        this.#discussions$.next([...discussions]);
        resolve();
      }, 250),
    );
  }

  addMessage(discussion: Discussion, message: string): Promise<void> {
    return new Promise((resolve) =>
      setTimeout(() => {
        const discussions = this.#discussions$.getValue();
        const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.id).discussion;
        const date = new Date();
        discussionToUpdate.messages.push({
          id: `${this.#messageId++}`,
          userId: 'me',
          text: message,
          createdAt: date,
          updatedAt: undefined,
          deletedAt: undefined,
        });
        discussionToUpdate.lastMessageAt = date;
        discussionToUpdate.status = 'IN_PROGRESS';
        this.#discussions$.next([...discussions]);
        resolve();
      }, 250),
    );
  }

  updateMessage(discussion: Discussion, message: Message): Promise<void> {
    return new Promise((resolve) =>
      setTimeout(() => {
        const discussions = this.#discussions$.getValue();
        const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.id).discussion;

        const messageToUpdate = discussionToUpdate.messages.find((m) => m.id === message.id);
        messageToUpdate.text = message.text;
        messageToUpdate.updatedAt = new Date();

        this.#discussions$.next([...discussions]);
        resolve();
      }, 250),
    );
  }

  deleteMessage(discussion: Discussion, message: Message) {
    const discussions = this.#discussions$.getValue();
    const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.id).discussion;

    const messageToUpdate = discussionToUpdate.messages.find((m) => m.id === message.id);
    messageToUpdate.text = '';
    messageToUpdate.deletedAt = new Date();

    this.#discussions$.next([...discussions]);
  }

  resolveDiscussion(discussion: Discussion) {
    const discussions = this.#discussions$.getValue();
    const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.id).discussion;

    discussionToUpdate.status = 'RESOLVED';
    this.#discussions$.next([...discussions]);
  }

  markAsRead(discussion: DiscussionDto) {
    const discussions = this.#discussions$.getValue();
    const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.discussion.id);

    discussionToUpdate.lastReadAt = new Date();
    discussionToUpdate.unread = false;
    this.#discussions$.next([...discussions]);
  }

  markAsUnread(discussion: DiscussionDto, message: Message) {
    const discussions = this.#discussions$.getValue();
    const discussionToUpdate = discussions.find((d) => d.discussion.id === discussion.discussion.id);

    discussionToUpdate.lastReadAt = new Date(message.createdAt.getTime() - 1);
    discussionToUpdate.unread = true;
    this.#discussions$.next([...discussions]);
  }
}
