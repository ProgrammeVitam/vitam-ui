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
import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { coerceBooleanProperty } from '@angular/cdk/coercion';

@Component({
  selector: 'vitamui-menu-button',
  templateUrl: './vitamui-menu-button.component.html',
  styleUrls: ['./vitamui-menu-button.component.scss'],
  imports: [CommonModule, MatButtonModule, MatMenuModule],
})
export class VitamuiMenuButtonComponent {
  /** Set the menu overlay display (by defaut at start) */
  overlayPos = input<'start' | 'end'>('start');

  /**
   * Set the icon to display on the button.
   * If there is only an icon, the button will be rounded.
   * WARNING : ONLY USE VITAMUI ICONS, MATERIAL ICONS WONT WORK
   */
  icon = input<string>();

  /**
   * Set the label that will be displayed on the button.
   * Can be combined with an icon.
   */
  label = input<string>();

  disabled = input(false, { transform: coerceBooleanProperty });
  /**
   * Color of the menu button (and overlay)
   */
  menuColor = input<'primary' | 'secondary'>('secondary');
  /**
   * Whether the menu button is in outline style or plain
   */
  menuButtonOutline = input(false, { transform: coerceBooleanProperty });
  /**
   * Size of the menu button
   */
  menuButtonSize = input<'large' | 'medium' | 'small' | 'xsmall tonal link'>('large');
  /**
   * When true, do not show an "X" button in place of the trigger button when opened and removes menu borders
   */
  simpleOverlay = input(false, { transform: coerceBooleanProperty });
}
