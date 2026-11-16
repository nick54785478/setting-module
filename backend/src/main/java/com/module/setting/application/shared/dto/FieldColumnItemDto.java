package com.module.setting.application.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 表格欄位自訂項目 DTO (FieldColumnItemDto)
 * <p>
 * 對應 value JSON 陣列元素結構：
 * [{"field":"service","label":"服務"},{"field":"actionType","label":"動作"}]
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldColumnItemDto implements Serializable {

    private String field; // 欄位屬性鍵值 (例如: "priorityNo", "code", "name")
    private String label; // 欄位顯示名稱 (例如: "順序號", "設定代碼", "顯示名稱")
}
