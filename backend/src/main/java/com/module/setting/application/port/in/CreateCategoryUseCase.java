package com.module.setting.application.port.in;

import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.result.CategoryGottenResult;

/**
 * 建立維度元資料用例 (CreateCategoryUseCase)
 */
public interface CreateCategoryUseCase {

    /**
     * 建立一筆全新維度元資料
     *
     * @param command 建立維度指令
     * @return 建立成功之維度結果
     */
    CategoryGottenResult create(CreateCategoryCommand command);
}
