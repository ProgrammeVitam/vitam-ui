import { NgModule } from '@angular/core';
import { EventTypeBadgeClassPipe } from './event-type-badge-class.pipe';
import { EventTypeColorClassPipe } from './event-type-color-class.pipe';
import { LastEventPipe } from './last-event.pipe';

@NgModule({
    declarations: [LastEventPipe, EventTypeBadgeClassPipe, EventTypeColorClassPipe],
    imports: [],
    exports: [LastEventPipe, EventTypeBadgeClassPipe, EventTypeColorClassPipe],
})
export class PipesModule { }
