package com.module.setting.application.port.in;

import com.module.setting.application.shared.command.DeleteSettingCommand;

/**
 * 刪除設定用例介面 (DeleteSettingUseCase)
 * <p>
 * 應用層 Inbound Port，定義刪除設定的業務合約。
 * </p>
 */
public interface DeleteSettingUseCase {

    /**
     * 執行刪除設定 (邏輯刪除或實體刪除)
     *
     * @param command 刪除設定指令
     */
    void delete(DeleteSettingCommand command);
}
