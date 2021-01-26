import { animate, keyframes, query, stagger, state, style, transition, trigger } from '@angular/animations';

export const collapseAnimation = trigger('collapseAnimation', [
  state('collapsed', style({ height: 0, visibility: 'hidden' })),
  state('expanded', style({ height: '*' })),
  transition('expanded <=> collapsed', animate('300ms ease-out')),
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
      transform: 'translateY(-100%)'
    }),
    animate('100ms ease-out'),
  ]),
]);

export const tooltipAnimation = trigger('tooltipAnimation', [
  state('*', style({ opacity: 1, transform: 'translateX(0)' })),
  transition(':enter', [
    style({
      opacity: 0,
      transform: 'translateY(-100%)'
    }),
    animate('100ms ease-out'),
  ]),
]);

export const opacityAnimation = trigger('opacityAnimation', [
  state('close', style({})),
  transition(':enter', [
    animate('500ms cubic-bezier(0, 0, 0.2, 1)', keyframes([
      style({ opacity: 0 }),
      style({ opacity: 1 }),
    ])),
  ]),
  transition('* => close', [
    animate('500ms cubic-bezier(0, 0, 0.2, 1)', keyframes([
      style({ opacity: 1 }),
      style({ opacity: 0 }),
    ])),
  ]),
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
      stagger(50, [
        animate(
          '50ms',
          style({ opacity: 1, transform: 'none' })
        )
      ])
    ])
  ]),
  transition(':leave', [
    animate(
      '250ms',
      style({ opacity: 0, transform: 'translateX(+100px)' })
    )
  ])
]);
