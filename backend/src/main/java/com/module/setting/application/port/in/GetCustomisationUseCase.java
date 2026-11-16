package com.module.setting.application.port.in;

import java.util.List;

/**
 * 取得個人化配置使用案例 (GetCustomisationUseCase)
 */
public interface GetCustomisationUseCase {

    /**
     * 依據元件與類型取得當前使用者的個人化欄位清單 (由逗號分隔解析)
     *
     * @param component 前端元件名稱 (例如: "SettingComponent")
     * @param type      偏好類型 (例如: "FieldView")
     * @return 顯示之欄位代碼清單 (例如: ["tenant", "category", "code"])
     */
    List<String> getCustomisation(String component, String type);
}
