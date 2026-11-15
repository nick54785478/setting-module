package com.module.setting.application.shared.command;

/**
 * 建立維度元資料指令 (CreateCategoryCommand)
 */
public record CreateCategoryCommand(
        String tenant,
        String code,
        String name,
        String description,
        Integer priorityNo
) {
}
