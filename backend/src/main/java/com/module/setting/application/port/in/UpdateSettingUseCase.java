package com.module.setting.application.port.in;

import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.result.SettingUpdatedResult;

/**
 * 更新設定用例介面 (UpdateSettingUseCase)
 * <p>
 * 應用層 Inbound Port，定義異動設定內容的業務合約。
 * </p>
 */
public interface UpdateSettingUseCase {

    /**
     * 執行更新設定
     *
     * @param command 更新設定指令
     * @return 更新完成後的設定結果 DTO
     */
    SettingUpdatedResult update(UpdateSettingCommand command);
}
