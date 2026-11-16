package com.module.setting.application.port.in;

import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.result.CategoryUpdatedResult;

/**
 * 更新維度元資料用例 (UpdateCategoryUseCase)
 */
public interface UpdateCategoryUseCase {

    /**
     * 更新指定 ID 之維度元資料
     *
     * @param command 更新維度指令
     * @return 更新成功之維度結果
     */
    CategoryUpdatedResult update(UpdateCategoryCommand command);
}
