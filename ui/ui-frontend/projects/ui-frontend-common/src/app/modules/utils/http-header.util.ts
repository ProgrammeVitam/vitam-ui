import { HttpHeaders } from '@angular/common/http';

export const SKIP_ERROR_NOTIFICATION = 'SkipErrorNotification';

export function addSkipErrorNotificationHeader(httpHeaders: HttpHeaders): HttpHeaders {
  const headers = httpHeaders.append(SKIP_ERROR_NOTIFICATION, SKIP_ERROR_NOTIFICATION);
  return headers;
}
