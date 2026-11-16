package com.module.setting.application.port.in;

import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.result.SettingCreatedResult;

/**
 * 建立設定用例介面 (CreateSettingUseCase)
 * <p>
 * 應用層 Inbound Port，定義新增設定的業務合約。
 * </p>
 */
public interface CreateSettingUseCase {

    /**
     * 執行建立設定
     *
     * @param command 建立設定指令
     * @return 建立完成後的設定結果 DTO
     */
    SettingCreatedResult create(CreateSettingCommand command);
}
