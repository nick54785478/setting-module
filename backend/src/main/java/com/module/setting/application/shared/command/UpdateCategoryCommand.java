package com.module.setting.application.shared.command;

import com.module.setting.application.shared.vo.YesNo;

/**
 * 更新維度元資料指令 (UpdateCategoryCommand)
 */
public record UpdateCategoryCommand(
        Long id,
        String name,
        String description,
        Integer priorityNo,
        YesNo activeFlag
) {
}
