import { HttpResponse } from '@angular/common/http';

export class DownloadUtils {
  private static DOUBLE_QUOTES = '"';

  static loadFromBlob(resp: HttpResponse<Blob>, mimeType: string, newFilename?: string) {
    // It is necessary to create a new blob object with mime-type explicitly set
    // otherwise only Chrome works like it should
    const newBlob = new Blob([resp.body], { type: mimeType });

    // IE doesn't allow using a blob object directly as link href
    // instead it is necessary to use msSaveOrOpenBlob
    if (window.navigator && window.navigator.msSaveOrOpenBlob) {
      window.navigator.msSaveOrOpenBlob(newBlob);
      return;
    }

    // For other browsers:
    // Create a link pointing to the ObjectURL containing the blob.
    const data = window.URL.createObjectURL(newBlob);

    const link = document.createElement('a');
    link.href = data;
    const fileName = newFilename
      ? newFilename
      : decodeURIComponent(DownloadUtils.getFilenameFromContentDisposition(resp.headers.get('content-disposition')));
    // If the filename is surrounded by double quotes we remove them to have the correct filename
    link.download = DownloadUtils.isSurroundedByDoubleQuotes(fileName) ? DownloadUtils.removeSurroundingDoubleQuotes(fileName) : fileName;
    // This is necessary as link.click() does not work on the latest firefox
    link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));

    setTimeout(() => {
      // For Firefox it is necessary to delay revoking the ObjectURL
      window.URL.revokeObjectURL(data);
      link.remove();
    }, 100);
  }

  static getFilenameFromContentDisposition(contentDisposition: string): string {
    const regex = /filename[^;\n=]*=((['"]).*?\2|[^;\n]*)/g;
    const match = regex.exec(contentDisposition);
    const filename = match[1];
    return filename;
  }

  static isSurroundedByDoubleQuotes(charSequence: string): boolean {
    return charSequence.startsWith(DownloadUtils.DOUBLE_QUOTES) && charSequence.endsWith(DownloadUtils.DOUBLE_QUOTES);
  }

  static removeSurroundingDoubleQuotes(charSequence: string): string {
    return charSequence.substr(1, charSequence.length - 2);
  }
}
