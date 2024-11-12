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
import { animate, keyframes, query, stagger, state, style, transition, trigger } from '@angular/animations';

export const collapseAnimation = trigger('collapseAnimation', [
  state('collapsed', style({ height: '0px', visibility: 'hidden', opacity: '0' })),
  state('expanded', style({ height: '*', visibility: 'visible', opacity: '1' })),
  transition('expanded <=> collapsed', animate('150ms cubic-bezier(0.4,0.0,0.2,1)')),
]);

export const rotateAnimation = trigger('rotateAnimation', [
  state('collapsed', style({ transform: 'rotate(180deg)' })),
  state('expanded', style({ transform: 'none' })),
  transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
]);

export const rotateUpAnimation = trigger('rotateUpAnimation', [
  state('true', style({ transform: 'none' })),
  state('false', style({ transform: 'rotate(180deg)' })),
  transition('true <=> false', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
]);

export const rotate90Animation = trigger('rotate90Animation', [
  state('collapsed', style({ transform: 'rotate(-90deg)' })),
  state('expanded', style({ transform: 'rotate(0deg)' })),
  transition('expanded <=> collapsed', animate('200ms ease-out')),
]);

export const fadeInOutAnimation = trigger('fadeInOutAnimation', [
  transition(':enter', [style({ opacity: 0 }), animate('200ms', style({ opacity: 1 }))]),
  transition(':leave', [style({ opacity: 1 }), animate('200ms', style({ opacity: 0 }))]),
]);

export const slideDownAnimation = trigger('slideDownAnimation', [
  state('*', style({ opacity: 1, transform: 'translateX(0)' })),
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'translateY(-100%)',
    }),
    animate('100ms ease-out'),
  ]),
]);

export const tooltipAnimation = trigger('tooltipAnimation', [
  state('*', style({ opacity: 1, transform: 'translateX(0)' })),
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'translateY(-100%)',
    }),
    animate('100ms ease-out'),
  ]),
]);

export const opacityAnimation = trigger('opacityAnimation', [
  state('close', style({})),
  transition(':enter', [animate('500ms cubic-bezier(0, 0, 0.2, 1)', keyframes([style({ opacity: 0 }), style({ opacity: 1 })]))]),
  transition('* => close', [animate('500ms cubic-bezier(0, 0, 0.2, 1)', keyframes([style({ opacity: 1 }), style({ opacity: 0 })]))]),
]);

export const transitionAnimation = trigger('transitionAnimation', [
  state('previous', style({ height: '0px', visibility: 'hidden' })),
  state('next', style({ height: '0px', visibility: 'hidden' })),
  state('current', style({ height: '*', visibility: 'visible' })),
  transition('* <=> current', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
]);

export const slideAnimation = trigger('slideAnimation', [
  transition(':enter', [
    query('*', [
      style({ opacity: 0, transform: 'translateX(-20px)' }),
      stagger(50, [animate('50ms', style({ opacity: 1, transform: 'none' }))]),
    ]),
  ]),
  transition(':leave', [animate('250ms', style({ opacity: 0, transform: 'translateX(+100px)' }))]),
]);
