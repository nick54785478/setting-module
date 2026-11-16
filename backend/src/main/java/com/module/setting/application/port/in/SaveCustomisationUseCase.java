package com.module.setting.application.port.in;

import java.util.List;

/**
 * 儲存個人化配置使用案例 (SaveCustomisationUseCase)
 */
public interface SaveCustomisationUseCase {

    /**
     * 儲存或更新當前使用者之個人化欄位清單 (儲存為逗號分隔)
     *
     * @param component 前端元件名稱 (例如: "SettingComponent")
     * @param type      偏好類型 (例如: "FieldView")
     * @param fields    欄位名稱清單 (例如: ["tenant", "category", "code"])
     * @return 儲存後之欄位清單
     */
    List<String> saveCustomisation(String component, String type, List<String> fields);
}
