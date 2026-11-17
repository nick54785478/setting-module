package com.module.setting.application.port.in;

import com.module.setting.application.shared.command.ReorderSettingItemCommand;
import com.module.setting.application.shared.result.SettingGottenResult;

import java.util.List;

/**
 * 設定項目排序用例介面 (ReorderSettingUseCase)
 * <p>
 * 應用層 Inbound Port，定義批次調整設定 priorityNo 並保證依序回傳的業務合約。
 * </p>
 */
public interface ReorderSettingUseCase {

    /**
     * 執行批次設定項目排序
     *
     * @param commands 排序指令清單 (包含 id 與 priorityNo)
     * @return 排序更新後之設定結果清單 (嚴格依 priorityNo 升冪排序)
     */
    List<SettingGottenResult> reorder(List<ReorderSettingItemCommand> commands);
}
