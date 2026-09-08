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
import { DatePipe } from '@angular/common';
import { Component, computed, HostBinding, HostListener, inject, input, output } from '@angular/core';
import { AuthService } from '../../../../../app/modules/auth.service';
import { VitamUICommonModule } from '../../../../../app/modules/vitamui-common.module';
import { Discussion, DiscussionService, Message } from '../../discussion.service';
import { CommonTooltipModule } from '../../../../../app/modules/components/common-tooltip/common-tooltip.module';
import { InputComponent } from '../../../input/input.component';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../../dialog/confirm-dialog/confirm-dialog.component';
import { ConfirmDialogData } from '../../../../models/confirm-dialog-data.interface';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'vitamui-message',
  imports: [DatePipe, CommonTooltipModule, InputComponent, ReactiveFormsModule, TranslatePipe, VitamUICommonModule],
  templateUrl: './message.component.html',
  styleUrl: './message.component.scss',
})
export class MessageComponent {
  me = inject(AuthService).user;
  private discussionService = inject(DiscussionService);
  private dialog = inject(MatDialog);

  discussion = input.required<Discussion>();
  message = input.required<Message>();
  lastReadAt = input<Date>();
  markUnread = output<Message>();

  updateMode = false;

  control = computed(() => new FormControl(this.message().text));

  isControlValid() {
    return !!this.control().value && this.control().value !== this.message().text && !this.control().pending;
  }

  @HostBinding('class.closed')
  get isClosed(): boolean {
    return this.discussion().status === 'RESOLVED';
  }

  @HostListener('keydown.control.enter')
  @HostListener('keydown.meta.enter')
  async updateMessage() {
    if (!this.isControlValid()) return;
    this.control().markAsPending();
    await this.discussionService.updateMessage(this.discussion(), { ...this.message(), text: this.control().value });
    this.updateMode = false;
  }

  async deleteMessage() {
    const confirmation = !!(await firstValueFrom(
      this.dialog
        .open<ConfirmDialogComponent, ConfirmDialogData>(ConfirmDialogComponent, {
          panelClass: 'small',
          disableClose: false,
          data: {
            title: 'DISCUSSION.MESSAGE.DELETE_CONFIRM_TITLE',
            confirmLabel: 'COMMON.CONFIRM',
            cancelLabel: 'COMMON.CANCEL',
          },
        })
        .afterClosed(),
    ));
    if (confirmation) this.discussionService.deleteMessage(this.discussion(), this.message());
  }
}
