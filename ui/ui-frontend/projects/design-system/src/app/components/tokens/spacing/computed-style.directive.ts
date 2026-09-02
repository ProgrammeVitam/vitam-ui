import { AfterViewInit, Directive, ElementRef, inject, input } from '@angular/core';

@Directive({
  selector: '[designSystemComputedStyle]',
  standalone: true,
})
export class ComputedStyleDirective implements AfterViewInit {
  readonly property = input.required<'padding' | 'margin' | 'gap'>({ alias: 'designSystemComputedStyle' });
  readonly target = input<HTMLElement | undefined>(undefined, { alias: 'designSystemComputedStyleTarget' });

  private readonly host = inject(ElementRef<HTMLElement>);

  ngAfterViewInit(): void {
    const target = this.target() ?? this.host.nativeElement;
    const value = getComputedStyle(target)[this.property()];
    this.host.nativeElement.textContent = value;
  }
}
