import { Injectable } from '@angular/core';
import { Notification } from '../classes/notification';

@Injectable()
export class NotificationsService {
  private MAX_NOTIFICATIONS = 5;
  private notifications: Notification[] = [];

  constructor() { }

  /**
   * Adds a notification to the corner of the screen.
   * @param message The message to show the user
   * @param type Type of message 0=general, 1=success, 2=warning, 3=error(danger)
   */
  public addNotification(message: string, type: number, timeout: number = 3000): void {
    const id = this.notifications.length === 0 ? 0 : this.notifications[this.notifications.length - 1].id + 1;
    const t = {
      id: id,
      message: message,
      type: type
    };
    this.notifications.push(t);
    if (this.notifications.length > this.MAX_NOTIFICATIONS) {
      this.notifications.shift();
    }
    setTimeout(() => {
      this.deleteNotification(id);
    }, timeout);
  }

  public deleteNotification(id: number) {
    this.notifications = this.notifications.filter((n) => n.id !== id);
  }

  public getNotifications(): Notification[] {
    return this.notifications;
  }

}
