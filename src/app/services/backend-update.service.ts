import { Injectable } from '@angular/core';

@Injectable()
export class BackendUpdateService {
  public is_online: boolean = true;
  public backlog: Backlog[] = [];

  constructor() { }

  public submitPitScout(data: any): void {
    if(this.is_online) {
      // make post request to server to submit data
      console.log(data);
    }else {
      this.backlog.push({
        type: "pitscout",
        data: data
      });
    }
  }

  public submitMatchScout(data: any): void {
    if(this.is_online){
      // post data to submit match endpoint
    }else {
      this.backlog.push({
        type: "matchscout",
        data: data
      });
    }
  }
  
  public publishBacklog(): void {
    while(this.is_online && this.backlog.length > 0) {
      let log: Backlog = this.backlog.shift();
      switch(log.type){
        case "pitscout": 
          this.submitPitScout(log.data);
          break;
        case "matchscout":
          this.submitMatchScout(log.data);
        default:
          console.log("Unknown Backlog type");
      }
    }
  }
  
}

interface Backlog {
    type: string;
    data: any;
}