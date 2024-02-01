export enum IngestUploadStatus {
  WIP,
  FINISHED,
  ERROR,
}

export class IngestInfo {
  constructor(
    public name: string,
    public size: number,
    public sizeUploaded: number,
    public status: IngestUploadStatus,
  ) {}
}

export class IngestList {
  ingests: { [key: string]: IngestInfo } = {};
  wipNumber = 0;

  add(requestId: string, info: IngestInfo) {
    this.ingests[requestId] = info;
    this.wipNumber++;
  }

  update(requestId: string, sizeUploaded: number, status?: IngestUploadStatus) {
    if (!this.ingests[requestId]) {
      return;
    }
    if (status) {
      // status defined and value > 0 ( FINISHED or ERROR )
      this.ingests[requestId].status = status;
      if (status === IngestUploadStatus.FINISHED) {
        this.finishTask(requestId);
      }
    }
    if (!status) {
      // status undefined or status = WIP (index = 0)
      this.ingests[requestId].sizeUploaded = sizeUploaded;
      if (this.ingests[requestId].sizeUploaded === this.ingests[requestId].size) {
        this.finishTask(requestId);
      }
    }
  }

  finishTask(requestId: string) {
    delete this.ingests[requestId];
    this.wipNumber--;
  }
}
