package com.module.setting.application.port.in;

/**
 * 重設/還原個人化配置使用案例 (ResetCustomisationUseCase)
 */
public interface ResetCustomisationUseCase {

    /**
     * 清除當前使用者於特定元件與類型之自訂配置 (恢復預設值)
     *
     * @param component 前端元件名稱
     * @param type      偏好類型
     */
    void resetCustomisation(String component, String type);
}
