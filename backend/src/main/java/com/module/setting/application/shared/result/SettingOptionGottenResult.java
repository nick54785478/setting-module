package com.module.setting.application.shared.result;

/**
 * 下拉選單選項結果資料傳輸物件 (SettingOptionGottenResult)
 * <p>
 * 針對前端下拉式選單 (Select / Dropdown) 設計之純資料載體 (Pure Data Carrier)，
 * 提供 name (對應 option.label) 與 code (對應 option.value)。
 * </p>
 *
 * @param code  設定代碼 (對應 option.value)
 * @param name  設定名稱 (對應 option.label)
 * @param label 選項顯示文字 (等於 name)
 * @param value 選項數值鍵 (等於 code)
 */
public record SettingOptionGottenResult(
        String code,
        String name,
        String label,
        String value
) {
    /**
     * 常用建構子：自動將 label 對應至 name，value 對應至 code
     *
     * @param code 代碼 (option.value)
     * @param name 名稱 (option.label)
     */
    public SettingOptionGottenResult(String code, String name) {
        this(code, name, name, code);
    }
}
