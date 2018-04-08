import { Component, OnInit } from '@angular/core';
import { NotificationsService } from '../../services/notifications.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.less']
})
export class NotificationsComponent implements OnInit {
  // public vidOn = false;

  constructor(private notif: NotificationsService) { }

  ngOnInit() {
  }

  public getNotifications() {
    return this.notif.getNotifications();
  }

  public deleteNotification(id: number) {
    this.notif.deleteNotification(id);
  }

}
