package com.module.setting.presentation.assembler;

import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.command.ReorderSettingItemCommand;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.query.GetSettingPagedQuery;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.presentation.resource.in.CreateSettingResource;
import com.module.setting.presentation.resource.in.ReorderSettingItemResource;
import com.module.setting.presentation.resource.in.UpdateSettingResource;
import com.module.setting.presentation.resource.out.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 表現層資源組裝器 (SettingResourceAssembler)
 * <p>
 * 作為表現層與應用層之間的防腐層 (Anti-Corruption Layer)，
 * 專門負責 Request Resource 轉 Command 以及 Result DTO 轉 Response Resource，
 * 保證 Resource 物件之純資料載體特徵。
 * </p>
 */
@Component
public class SettingResourceAssembler {

    /**
     * 將建立設定請求資源轉換為建立設定指令
     * <p>
     * 若請求體中未傳入 tenant，則自動從 {@link UserContextHolder} 取得當前請求之租戶標識。
     * </p>
     *
     * @param resource 建立設定請求資源
     * @return 建立設定指令
     */
    public CreateSettingCommand toCommand(CreateSettingResource resource) {
        String tenant = (resource.tenant() != null && !resource.tenant().isBlank())
                ? resource.tenant()
                : UserContextHolder.getTenant();

        return new CreateSettingCommand(
                tenant,
                resource.category(),
                resource.subCategory(),
                resource.code(),
                resource.name(),
                resource.description(),
                resource.value(),
                resource.priorityNo()
        );
    }

    /**
     * 將更新設定請求資源與 ID 轉換為更新設定指令
     *
     * @param id       設定 ID
     * @param resource 更新設定請求資源
     * @return 更新設定指令
     */
    public UpdateSettingCommand toCommand(Long id, UpdateSettingResource resource) {
        return new UpdateSettingCommand(
                id,
                resource.category(),
                resource.subCategory(),
                resource.name(),
                resource.description(),
                resource.value(),
                resource.priorityNo(),
                resource.activeFlag()
        );
    }

    /**
     * 建立分頁查詢指令物件
     *
     * @param tenant      租戶代碼 (若為空將由下層或此處自動對齊 UserContextHolder)
     * @param category    功能大維度
     * @param subCategory 業務子分類
     * @param code        設定代碼
     * @param name        名稱關鍵字
     * @param activeFlag  有效性旗標
     * @param page        頁碼
     * @param size        每頁筆數
     * @return 分頁查詢指令
     */
    public GetSettingPagedQuery toPagedQuery(String tenant, String category, String subCategory,
                                            String code, String name, String activeFlag,
                                            int page, int size) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        return new GetSettingPagedQuery(resolvedTenant, category, subCategory, code, name, activeFlag, page, size);
    }

    /**
     * 將應用層結果 DTO 轉換為建立成功回應資源
     *
     * @param result 設定建立結果 DTO
     * @return 建立成功回應資源
     */
    public SettingCreatedResource toCreatedResource(SettingCreatedResult result) {
        if (result == null) {
            return null;
        }
        return new SettingCreatedResource(
                result.id(),
                result.tenant(),
                result.category(),
                result.subCategory(),
                result.code(),
                result.name(),
                result.description(),
                result.value(),
                result.priorityNo(),
                result.activeFlag()
        );
    }

    /**
     * 將應用層結果 DTO 轉換為更新成功回應資源
     *
     * @param result 設定更新結果 DTO
     * @return 更新成功回應資源
     */
    public SettingUpdatedResource toUpdatedResource(SettingUpdatedResult result) {
        if (result == null) {
            return null;
        }
        return new SettingUpdatedResource(
                result.id(),
                result.tenant(),
                result.category(),
                result.subCategory(),
                result.code(),
                result.name(),
                result.description(),
                result.value(),
                result.priorityNo(),
                result.activeFlag()
        );
    }

    /**
     * 將應用層結果 DTO 轉換為查詢回應資源
     *
     * @param result 設定結果 DTO
     * @return 查詢回應資源
     */
    public SettingGottenResource toGottenResource(SettingGottenResult result) {
        return new SettingGottenResource(
                result.id(),
                result.tenant(),
                result.category(),
                result.subCategory(),
                result.code(),
                result.name(),
                result.description(),
                result.value(),
                result.priorityNo(),
                result.activeFlag()
        );
    }

    /**
     * 將應用層通用分頁結果 DTO 轉換為表現層分頁回應資源
     *
     * @param pagedResult 應用層分頁結果 DTO
     * @return 表現層分頁回應資源
     */
    public SettingPagedGottenResource toPagedResource(PagedDataGottenResult<SettingGottenResult> pagedResult) {
        List<SettingGottenResource> content = pagedResult.content().stream()
                .map(this::toGottenResource)
                .toList();

        return new SettingPagedGottenResource(
                content,
                pagedResult.totalElements(),
                pagedResult.totalPages(),
                pagedResult.page(),
                pagedResult.size()
        );
    }

    /**
     * 將下拉選單結果 DTO 轉換為表現層下拉選項回應資源
     *
     * @param result 下拉選單結果 DTO
     * @return 下拉選項回應資源 (name = option.label, code = option.value)
     */
    public SettingOptionGottenResource toOptionResource(SettingOptionGottenResult result) {
        return new SettingOptionGottenResource(
                result.code(),
                result.name(),
                result.label(),
                result.value()
        );
    }

    /**
     * 將設定查詢結果 DTO 轉換為側邊欄選單項目資源
     *
     * @param result 設定查詢結果 DTO
     * @return 側邊欄選單項目資源
     */
    public TenantMenuItemGottenResource toMenuItemResource(SettingGottenResult result) {
        if (result == null) {
            return null;
        }
        return new TenantMenuItemGottenResource(
                result.id(),
                result.tenant(),
                result.subCategory(),
                result.code(),
                result.name(),
                result.description(),
                result.value(),
                result.priorityNo(),
                result.activeFlag()
        );
    }

    /**
     * 將排序請求資源清單轉換為排序指令清單
     *
     * @param resources 排序請求資源清單
     * @return 排序指令清單
     */
    public List<ReorderSettingItemCommand> toReorderCommands(List<ReorderSettingItemResource> resources) {
        if (resources == null) {
            return List.of();
        }
        return resources.stream()
                .map(r -> new ReorderSettingItemCommand(r.id(), r.priorityNo()))
                .toList();
    }
}
