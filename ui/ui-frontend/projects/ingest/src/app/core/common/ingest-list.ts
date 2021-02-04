export enum IngestStatus {
  WIP, FINISHED, ERROR
}

export class IngestInfo {
  constructor(public name: string, public size: number, public nbChunks: number, public actualChunk: number, public status: IngestStatus) { }
}

export class IngestList {
  ingests: {[key: string]: IngestInfo} = {};
  wipNumber = 0;

  add(requestId: string, info: IngestInfo) {
    this.ingests[requestId] = info;
    this.wipNumber++;
  }

  update(requestId: string, status?: IngestStatus) {
    if (!this.ingests[requestId]) { return; }

    if (status) { // status defined and value > 0 ( FINISHED or ERROR )
      this.ingests[requestId].status = status;
      if (status === IngestStatus.FINISHED) {
        this.finishTask(requestId);
      }
    }
    if (!status) { // status undefined or status = WIP (index = 0)
      this.ingests[requestId].actualChunk ++;
      if ( this.ingests[requestId].actualChunk === this.ingests[requestId].nbChunks ) {
        this.finishTask(requestId);
      }
    }
  }

  finishTask(requestId: string) {
    delete this.ingests[requestId];
    this.wipNumber --;
  }
}
