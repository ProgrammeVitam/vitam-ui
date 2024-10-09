import { Component } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { EllipsisDirectiveModule } from 'vitamui-library';
import { NgClass } from '@angular/common';

@Component({
  selector: 'design-system-typography',
  templateUrl: './typography.component.html',
  styleUrls: ['./typography.component.scss'],
  standalone: true,
  imports: [TranslateModule, EllipsisDirectiveModule, NgClass],
})
export class TypographyComponent {
  textButtonFlavors = ['', 'link'];
  textButtonSizes = ['large', 'medium', 'small'];
  textFlavors = ['', 'bold', 'link'];
  textSizes = ['large', 'medium', 'normal', 'caption', 'subcaption'];
  textColors = ['primary', 'secondary', 'tertiary', 'success', 'warning', 'danger', 'light']; // FIXME: do not keep all?

  info(element: HTMLElement) {
    const style = getComputedStyle(element);
    return `(${this.fontFamily(style)} | ${this.fontSize(style)} | w: ${this.fontWeight(style)} | lh: ${this.lineHeight(style)} | ls: ${this.letterSpacing(style)})`;
  }

  private fontSize(style: CSSStyleDeclaration) {
    return style.fontSize.replace('px', '');
  }

  private fontFamily(style: CSSStyleDeclaration) {
    return style.fontFamily.split(',')[0];
  }

  private fontWeight(style: CSSStyleDeclaration) {
    return style.fontWeight;
  }

  private lineHeight(style: CSSStyleDeclaration) {
    return style.lineHeight.replace('px', '');
  }

  private letterSpacing(style: CSSStyleDeclaration) {
    return style.letterSpacing;
  }

  color(element: HTMLElement) {
    return getComputedStyle(element).color;
  }
}
