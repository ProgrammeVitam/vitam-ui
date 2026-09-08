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
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { concat, firstValueFrom, map, merge, Observable, scan, Subject } from 'rxjs';
import { EventSource } from 'eventsource';
import { OAuthStorage } from 'angular-oauth2-oidc';
import { AuthService } from '../../../app/modules/auth.service';
import { BASE_URL } from '../../../app/modules/injection-tokens';
import { TenantSelectionService } from '../../../app/modules/tenant-selection.service';
import { VitamuiHttpHeaders } from '../../../app/modules/vitamui-http-headers.enum';
import { ConfigService } from '../../../app/modules/config.service';

export interface DiscussionDto {
  discussion: Discussion;
  lastReadAt: Date;
  unread: boolean;
}

export interface Discussion {
  title: string;
  entities: { entityId: string; entityType: string }[];
  id: string;
  createdAt: Date;
  lastMessageAt: Date;
  messages: Message[];
  status: 'IN_PROGRESS' | 'RESOLVED';
}

class DiscussionUpdate {
  id: string;
  lastReadAt: Date;
  unread: boolean;
}

export interface Message {
  id: string;
  userId: string;
  userName?: string;
  text: string;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date;
}

export interface DiscussionEntity {
  id: string;
  type: string;
  /**
   * The "main" entity is the one that'll be used to search the discussions related to the current "page", while all entities are used when discussions are created.
   * For example, in Collect, the main entity would be the Transaction but there would also be a Project entity in the list.
   */
  main?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class DiscussionService {
  private readonly configService = inject(ConfigService);
  private readonly httpClient = inject(HttpClient);
  private readonly authStorage = inject(OAuthStorage);
  private readonly tenantSelectionService = inject(TenantSelectionService);
  private readonly me = inject(AuthService).user;
  private readonly apiUrl = `${inject(BASE_URL)}/discussions`;

  #updateDiscussion = new Subject<DiscussionUpdate>();

  findDiscussions(entity: DiscussionEntity): Observable<DiscussionDto[]> {
    const initialData$ = this.httpClient.get<DiscussionDto[]>(`${this.apiUrl}?entityType=${entity.type}&entityId=${entity.id}`);

    const streamData$ = new Observable<Discussion>((observer) => {
      const eventSource = new EventSource(`${this.apiUrl}/stream?entityType=${entity.type}&entityId=${entity.id}`, {
        fetch: (input, init) =>
          fetch(input, {
            ...init,
            headers: {
              ...init.headers,
              [this.configService.config?.AUTHORIZATION_HEADER_NAME || 'Authorization']:
                `Bearer ${this.authStorage.getItem('access_token')}`,
              [VitamuiHttpHeaders.X_TENANT_ID]: String(this.tenantSelectionService.getLastTenantIdentifier()),
            },
          }),
      });

      eventSource.onmessage = (event) => {
        observer.next(JSON.parse(event.data));
      };

      eventSource.onerror = (err) => {
        console.error(`SSE connexion failed! Discussions won't update automatically`, err);
        eventSource.close();
      };

      // Cleanup: executed on unsubscribe
      return () => {
        eventSource.close();
      };
    });

    return concat(initialData$, merge(streamData$, this.#updateDiscussion.asObservable())).pipe(
      scan((previousDiscussions, payload) => {
        // If 'payload' is an array, this is the initial data
        if (Array.isArray(payload)) return payload;

        const id = payload.id;
        const existingIndex = previousDiscussions.findIndex((d) => d.discussion.id === id);
        const discussionAlreadyExists = existingIndex !== -1;

        // If the discussion already exists, we're going to update it
        if (discussionAlreadyExists) {
          const updatedDiscussions = [...previousDiscussions];
          const previousDiscussion = updatedDiscussions[existingIndex];

          const isDiscussionUpdate = this.isDiscussionUpdate(payload);

          const unread = isDiscussionUpdate
            ? payload.unread
            : (payload as Discussion).messages.some(
                (m) => new Date(m.createdAt) > new Date(previousDiscussion.lastReadAt) && m.userId !== this.me.id,
              );
          const lastReadAt = isDiscussionUpdate
            ? payload.lastReadAt
            : unread
              ? previousDiscussion.lastReadAt
              : (payload as Discussion).lastMessageAt;
          const discussion = isDiscussionUpdate ? previousDiscussion.discussion : payload;

          updatedDiscussions[existingIndex] = {
            ...previousDiscussion,
            discussion: discussion,
            unread: unread,
            lastReadAt: lastReadAt,
          };
          return updatedDiscussions;
        } else {
          // Otherwise, this is a new discussion
          if (this.isDiscussionUpdate(payload)) {
            console.error(`Couldn't update discussion #${id} as it does not exist (shouldn't happen`);
            return previousDiscussions;
          } else {
            return [...previousDiscussions, { discussion: payload, unread: true, lastReadAt: undefined }];
          }
        }
      }, [] as DiscussionDto[]),
      map((discussions) =>
        discussions.map((discussion) => ({
          ...discussion,
          discussion: {
            ...discussion.discussion,
            messages: discussion.discussion.messages.map((message) => ({
              ...message,
              createdAt: new Date(message.createdAt),
              updatedAt: message.updatedAt ? new Date(message.updatedAt) : undefined,
              deletedAt: message.deletedAt ? new Date(message.deletedAt) : undefined,
            })),
          },
        })),
      ),
    );
  }

  private isDiscussionUpdate(payload: any): payload is DiscussionUpdate {
    return payload instanceof DiscussionUpdate;
  }

  createDiscussion(discussionEntities: DiscussionEntity[], title: string, text: string): Promise<Discussion> {
    return firstValueFrom(
      this.httpClient.post<Discussion>(this.apiUrl, {
        entities: discussionEntities.map((de) => ({ entityType: de.type, entityId: de.id })),
        title,
        text,
      }),
    );
  }

  renameDiscussion(discussion: Discussion, title: string): Promise<void> {
    return firstValueFrom(
      this.httpClient.post<void>(`${this.apiUrl}/${discussion.id}`, {
        title,
      }),
    );
  }

  addMessage(discussion: Discussion, message: string): Promise<void> {
    return firstValueFrom(
      this.httpClient.post<void>(`${this.apiUrl}/${discussion.id}/messages`, {
        text: message,
      }),
    );
  }

  updateMessage(discussion: Discussion, message: Message): Promise<void> {
    return firstValueFrom(
      this.httpClient.post<void>(`${this.apiUrl}/${discussion.id}/messages/${message.id}`, {
        text: message.text,
      }),
    );
  }

  deleteMessage(discussion: Discussion, message: Message) {
    this.httpClient.delete(`${this.apiUrl}/${discussion.id}/messages/${message.id}`).subscribe();
  }

  resolveDiscussion(discussion: Discussion) {
    this.httpClient.put(`${this.apiUrl}/${discussion.id}/resolve`, {}).subscribe();
  }

  markAsRead(discussion: DiscussionDto) {
    this.#updateDiscussion.next(
      Object.assign(new DiscussionUpdate(), { id: discussion.discussion.id, lastReadAt: new Date(), unread: false }),
    );
    this.httpClient.put(`${this.apiUrl}/${discussion.discussion.id}/read`, {}).subscribe();
  }

  markAsUnread(discussion: DiscussionDto, message: Message) {
    this.#updateDiscussion.next(
      Object.assign(new DiscussionUpdate(), {
        id: discussion.discussion.id,
        lastReadAt: new Date(new Date(message.createdAt).getTime() - 1),
        unread: true,
      }),
    );
    this.httpClient.put(`${this.apiUrl}/${discussion.discussion.id}/unread/${message.id}`, {}).subscribe();
  }
}
