export function download(blob: Blob, filename?: string) {
  const anchor = document.createElement('a');
  anchor.download = (filename) ? filename : 'download.zip';
  anchor.href = window.URL.createObjectURL(blob);
  anchor.click();
}
