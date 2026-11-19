package com.module.setting.application.port.out;

import java.util.Optional;

/**
 * 個人化配置倉儲輸出埠 (CustomisationRepositoryPort)
 * <p>
 * 應用層 Outbound Port，遵循 Clean Architecture 依賴反轉原則 (DIP)，
 * 嚴禁引用 Infrastructure 層之 ORM Entity (Customisation) 或特定實體物件，
 * 僅透過純業務型別與外界持久化存取交互。
 * </p>
 */
public interface CustomisationRepositoryPort {

    /**
     * 依據使用者坐標查詢已啟用的個人化配置字串值
     *
     * @param tenant    租戶識別碼
     * @param username  使用者帳號
     * @param component 畫面元件識別碼
     * @param type      配置類型 (如 FieldView)
     * @return 配置字串值 (如 CSV)，若無紀錄或已停用則回傳 empty
     */
    Optional<String> findActiveValue(String tenant, String username, String component, String type);

    /**
     * 儲存或更新個人化配置
     *
     * @param tenant    租戶識別碼
     * @param username  使用者帳號
     * @param component 畫面元件識別碼
     * @param type      配置類型
     * @param value     配置字串值 (如 CSV)
     */
    void save(String tenant, String username, String component, String type, String value);

    /**
     * 依據使用者坐標重設/刪除個人化配置
     *
     * @param tenant    租戶識別碼
     * @param username  使用者帳號
     * @param component 畫面元件識別碼
     * @param type      配置類型
     */
    void delete(String tenant, String username, String component, String type);
}
