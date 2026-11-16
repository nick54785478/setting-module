import { Injectable } from '@angular/core';

/**
 * 全域使用者資訊服務 (UserService)
 * <p>
 * 管理當前登入使用者之識別資訊，現階段預設固定為 nickgh.zhang@cw.com。
 * </p>
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  // 目前固定預設帳號
  private currentUsername: string = 'nickgh.zhang@cw.com';

  /**
   * 取得當前使用者名稱
   */
  getCurrentUsername(): string {
    return this.currentUsername;
  }

  /**
   * 設定當前使用者名稱
   */
  setUsername(username: string): void {
    this.currentUsername = username;
  }
}
