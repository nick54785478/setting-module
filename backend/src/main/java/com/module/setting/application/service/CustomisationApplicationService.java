package com.module.setting.application.service;

import com.module.setting.application.port.in.GetCustomisationUseCase;
import com.module.setting.application.port.in.ResetCustomisationUseCase;
import com.module.setting.application.port.in.SaveCustomisationUseCase;
import com.module.setting.application.port.out.CustomisationRepositoryPort;
import com.module.setting.application.shared.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 個人化配置應用服務 (CustomisationApplicationService)
 * <p>
 * 實作個人化配置之查詢、保存與重設業務合約。
 * 遵循 Clean Architecture 依賴反轉原則 (DIP)，僅依賴 {@link CustomisationRepositoryPort}，
 * 嚴禁引用 Infrastructure 層之 ORM Entity 或持久化 VO。
 * 欄位清單於底層持久化中以「逗號分隔 (CSV)」字串形式儲存，徹底解耦前端 UI 文案 (Label) 與多語系 (i18n)。
 * </p>
 */
@Slf4j
@Service
public class CustomisationApplicationService implements
        GetCustomisationUseCase,
        SaveCustomisationUseCase,
        ResetCustomisationUseCase {

    private final CustomisationRepositoryPort repositoryPort;

    /**
     * 建構子注入倉儲輸出埠
     *
     * @param repositoryPort 個人化配置倉儲輸出埠
     */
    public CustomisationApplicationService(CustomisationRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    /**
     * 取得目前使用者與租戶在指定畫面元件的個人化配置欄位清單
     * <p>
     * 依據當前登入者上下文 (Tenant 與 Username) 向倉儲查詢配置值，
     * 將原始逗號分隔 (CSV) 字串切分解析為欄位名稱清單。
     * 若查無資料或該配置處於停用狀態，則回傳空清單，由前端採用開箱預設欄位呈現。
     * </p>
     *
     * @param component 畫面元件唯一識別碼 (例如: "SettingComponent")
     * @param type      配置類型 (例如: "FieldView" 表格可見欄位)
     * @return 欄位代碼字串清單 (依個人化排序呈現)
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getCustomisation(String component, String type) {
        String tenant = resolveTenant();
        String username = resolveUsername();

        Optional<String> optionalValue = repositoryPort.findActiveValue(tenant, username, component, type);
        if (optionalValue.isEmpty() || optionalValue.get().isBlank()) {
            log.debug("查無個人化配置或為空, 回傳預設空清單: tenant={}, username={}, component={}, type={}",
                    tenant, username, component, type);
            return Collections.emptyList();
        }

        return Arrays.stream(optionalValue.get().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * 保存目前使用者與租戶在指定畫面元件的個人化配置
     * <p>
     * 將前端自訂之欄位清單轉為標準 CSV 字串（如 "code,name,value"），
     * 呼叫倉儲輸出埠進行新增或覆蓋更新，並確保旗標設為有效。
     * </p>
     *
     * @param component 畫面元件唯一識別碼
     * @param type      配置類型
     * @param fields    欲保存之欄位代碼清單
     * @return 成功保存之欄位清單
     */
    @Transactional
    @Override
    public List<String> saveCustomisation(String component, String type, List<String> fields) {
        String tenant = resolveTenant();
        String username = resolveUsername();

        String csvValue = (fields != null && !fields.isEmpty())
                ? String.join(",", fields)
                : "";

        repositoryPort.save(tenant, username, component, type, csvValue);
        log.info("個人化配置 (CSV) 儲存成功: tenant={}, username={}, component={}, type={}, value={}",
                tenant, username, component, type, csvValue);
        return fields != null ? fields : Collections.emptyList();
    }

    /**
     * 重設/還原指定畫面元件的個人化配置為系統預設值
     * <p>
     * 清除當前使用者針對該元件與配置類型所保存之專屬設定，下次查詢時將回傳空值並觸發前端恢復預設欄位。
     * </p>
     *
     * @param component 畫面元件唯一識別碼
     * @param type      配置類型
     */
    @Transactional
    @Override
    public void resetCustomisation(String component, String type) {
        String tenant = resolveTenant();
        String username = resolveUsername();

        repositoryPort.delete(tenant, username, component, type);
        log.info("個人化配置已還原為預設: tenant={}, username={}, component={}, type={}",
                tenant, username, component, type);
    }

    /**
     * 解析當前租戶代碼
     * <p>
     * 優先自 {@link UserContextHolder} 取得執行緒上下文中的租戶代碼，若無則回退預設展示租戶 "CW"。
     * </p>
     *
     * @return 租戶識別碼
     */
    private String resolveTenant() {
        String tenant = UserContextHolder.getTenant();
        return (tenant != null && !tenant.isBlank()) ? tenant : "CW";
    }

    /**
     * 解析當前登入使用者帳號
     * <p>
     * 優先自 {@link UserContextHolder} 取得執行緒上下文中的使用者帳號，若無則回退預設使用者 "nickgh.zhang@cw.com"。
     * </p>
     *
     * @return 使用者帳號
     */
    private String resolveUsername() {
        String username = UserContextHolder.getUsername();
        return (username != null && !username.isBlank()) ? username : "nickgh.zhang@cw.com";
    }
}
