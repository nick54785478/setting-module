package com.module.setting.presentation.controller;

import com.module.setting.application.port.in.CreateSettingUseCase;
import com.module.setting.application.port.in.DeleteSettingUseCase;
import com.module.setting.application.port.in.GetSettingOptionUseCase;
import com.module.setting.application.port.in.GetSettingPagedUseCase;
import com.module.setting.application.port.in.GetSettingUseCase;
import com.module.setting.application.port.in.ReorderSettingUseCase;
import com.module.setting.application.port.in.UpdateSettingUseCase;
import com.module.setting.application.shared.command.CreateSettingCommand;
import com.module.setting.application.shared.command.DeleteSettingCommand;
import com.module.setting.application.shared.command.UpdateSettingCommand;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.presentation.assembler.SettingResourceAssembler;
import com.module.setting.presentation.resource.in.CreateSettingResource;
import com.module.setting.presentation.resource.in.ReorderSettingItemResource;
import com.module.setting.presentation.resource.in.UpdateSettingResource;
import com.module.setting.presentation.resource.out.SettingCreatedResource;
import com.module.setting.presentation.resource.out.SettingGottenResource;
import com.module.setting.presentation.resource.out.SettingOptionGottenResource;
import com.module.setting.presentation.resource.out.SettingPagedGottenResource;
import com.module.setting.presentation.resource.out.SettingUpdatedResource;
import com.module.setting.presentation.resource.out.TenantLocaleOptionGottenResource;
import com.module.setting.presentation.resource.out.TenantMenuItemGottenResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 設定表現層控制器 (SettingController)
 * <p>
 * 表現層 Inbound Adapter，接收 HTTP Request 透過 Assembler 轉換為 Command/Query，
 * 委派 UseCase 業務合約處理並回傳對應的 Response Resource。
 * </p>
 */
@Tag(name = "Setting Management", description = "通用設定管理、Specification 動態分頁與階層選項 API")
@RestController
@RequestMapping("/api/v1/settings")
public class SettingController {

    private final CreateSettingUseCase createSettingUseCase;
    private final UpdateSettingUseCase updateSettingUseCase;
    private final GetSettingUseCase getSettingUseCase;
    private final GetSettingPagedUseCase getSettingPagedUseCase;
    private final GetSettingOptionUseCase getSettingOptionUseCase;
    private final DeleteSettingUseCase deleteSettingUseCase;
    private final ReorderSettingUseCase reorderSettingUseCase;
    private final SettingResourceAssembler settingResourceAssembler;

    public SettingController(CreateSettingUseCase createSettingUseCase,
                             UpdateSettingUseCase updateSettingUseCase,
                             GetSettingUseCase getSettingUseCase,
                             GetSettingPagedUseCase getSettingPagedUseCase,
                             GetSettingOptionUseCase getSettingOptionUseCase,
                             DeleteSettingUseCase deleteSettingUseCase,
                             ReorderSettingUseCase reorderSettingUseCase,
                             SettingResourceAssembler settingResourceAssembler) {
        this.createSettingUseCase = createSettingUseCase;
        this.updateSettingUseCase = updateSettingUseCase;
        this.getSettingUseCase = getSettingUseCase;
        this.getSettingPagedUseCase = getSettingPagedUseCase;
        this.getSettingOptionUseCase = getSettingOptionUseCase;
        this.deleteSettingUseCase = deleteSettingUseCase;
        this.reorderSettingUseCase = reorderSettingUseCase;
        this.settingResourceAssembler = settingResourceAssembler;
    }

    /**
     * 建立一筆新設定
     *
     * @param resource 建立設定請求資源
     * @return 建立成功之設定回應資源
     */
    @Operation(summary = "建立新設定", description = "建立一筆全新功能設定項目，支援多維定位範圍與順序號排序")
    @ApiResponse(responseCode = "201", description = "設定建立成功")
    @ApiResponse(responseCode = "400", description = "傳入參數格式不符或驗證失敗")
    @PostMapping
    public ResponseEntity<SettingCreatedResource> create(@Valid @RequestBody CreateSettingResource resource) {
        var command = settingResourceAssembler.toCommand(resource);
        SettingCreatedResult result = createSettingUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(settingResourceAssembler.toCreatedResource(result));
    }

    /**
     * 更新指定 ID 之設定
     *
     * @param id       設定主鍵 ID
     * @param resource 更新設定請求資源
     * @return 更新成功之設定回應資源
     */
    @Operation(summary = "更新既有設定", description = "依據主鍵 ID 更新設定展示名稱、數值、排序或有效性狀態")
    @ApiResponse(responseCode = "200", description = "設定更新成功")
    @ApiResponse(responseCode = "404", description = "查無指定 ID 之設定項目")
    @ApiResponse(responseCode = "400", description = "傳入參數格式不符")
    @PutMapping("/{id}")
    public ResponseEntity<SettingUpdatedResource> update(
            @Parameter(description = "設定主鍵 ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateSettingResource resource) {
        var command = settingResourceAssembler.toCommand(id, resource);
        SettingUpdatedResult result = updateSettingUseCase.update(command);
        return ResponseEntity.ok(settingResourceAssembler.toUpdatedResource(result));
    }

    /**
     * 依據主鍵 ID 查詢設定
     *
     * @param id 設定主鍵 ID
     * @return 設定回應資源
     */
    @Operation(summary = "查詢單一設定詳情", description = "依據主鍵 ID 取得設定之完整明細資料")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "查無指定 ID 之設定項目")
    @GetMapping("/{id}")
    public ResponseEntity<SettingGottenResource> getById(
            @Parameter(description = "設定主鍵 ID", required = true, example = "1")
            @PathVariable Long id) {
        SettingGottenResult result = getSettingUseCase.getById(id);
        return ResponseEntity.ok(settingResourceAssembler.toGottenResource(result));
    }

    /**
     * 多條件動態分頁查詢設定清單
     *
     * @param tenant      租戶代碼 (可選)
     * @param category    功能大維度 (ROLE, GROUP, FUNCTION 等，可選)
     * @param subCategory 業務子分類 (ADMIN, PM, DASHBOARD 等，可選)
     * @param code        設定代碼 (可選)
     * @param name        名稱關鍵字模糊查詢 (可選)
     * @param activeFlag  有效性旗標 (可選，預設為 "Y")
     * @param page        分頁頁碼 (預設 0)
     * @param size        每頁顯示筆數 (預設 10)
     * @return 分頁設定回應資源
     */
    @Operation(summary = "多條件動態分頁查詢 (Specification)",
            description = "支援依據 Tenant、Category、SubCategory、Code、Name 模糊搜尋與 ActiveFlag 組合過濾之動態分頁清單")
    @ApiResponse(responseCode = "200", description = "分頁查詢成功")
    @GetMapping("/summary")
    public ResponseEntity<SettingPagedGottenResource> summary(
            @Parameter(description = "租戶識別碼 (可選，未傳入將自動採用 Request Header 值)", example = "tenant-01")
            @RequestParam(required = false) String tenant,
            @Parameter(description = "功能大分類 / 維度", example = "FUNCTION")
            @RequestParam(required = false) String category,
            @Parameter(description = "業務子分類 / 模組", example = "DASHBOARD")
            @RequestParam(required = false) String subCategory,
            @Parameter(description = "設定代碼 (精準比對)", example = "DASHBOARD_ANALYTICS")
            @RequestParam(required = false) String code,
            @Parameter(description = "設定名稱 (模糊包含查詢)", example = "分析")
            @RequestParam(required = false) String name,
            @Parameter(description = "有效性旗標 (Y: 有效, N: 停用)", example = "Y")
            @RequestParam(required = false) String activeFlag,
            @Parameter(description = "頁碼 (從 0 開始)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁資料筆數", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        var query = settingResourceAssembler.toPagedQuery(
                tenant, category, subCategory, code, name, activeFlag, page, size);
        PagedDataGottenResult<SettingGottenResult> pagedResult = getSettingPagedUseCase.getPaged(query);
        return ResponseEntity.ok(settingResourceAssembler.toPagedResource(pagedResult));
    }

    /**
     * 取得所有業務功能大分類 (Category) 清單 (RESTful 資源集合，供前端下拉選單綁定)
     *
     * @param tenant 租戶代碼 (可選)
     * @return Category 下拉選單選項清單 (name = option.label, code = option.value)
     */
    @Operation(summary = "取得 Category 下拉選項清單",
            description = "檢索元資料 (META) 註冊之業務大維度 (Category) 定義，提供給前端下拉選單綁定")
    @ApiResponse(responseCode = "200", description = "選項檢索成功")
    @GetMapping("/categories")
    public ResponseEntity<List<SettingOptionGottenResource>> getCategoryOptions(
            @Parameter(description = "租戶識別碼 (可選)", example = "tenant-01")
            @RequestParam(required = false) String tenant) {
        List<SettingOptionGottenResult> options = getSettingOptionUseCase.getCategoryOptions(tenant);
        List<SettingOptionGottenResource> response = options.stream()
                .map(settingResourceAssembler::toOptionResource)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 透過 Category 取得其下屬 SubCategory 清單 (RESTful 階層子資源，供前端下拉選單綁定)
     *
     * @param category 業務功能大分類 (路徑變數: 例如 FUNCTION, ROLE, GROUP)
     * @param tenant   租戶代碼 (可選)
     * @return SubCategory 下拉選單選項清單 (name = option.label, code = option.value)
     */
    @Operation(summary = "取得指定 Category 下屬之 SubCategory 選項清單",
            description = "依據指定的 Category 路徑取得其下轄的所有子分類或設定項目，支援二級聯動選單")
    @ApiResponse(responseCode = "200", description = "子選項檢索成功")
    @ApiResponse(responseCode = "400", description = "Category 參數不得為空")
    @GetMapping("/categories/{category}/sub-categories")
    public ResponseEntity<List<SettingOptionGottenResource>> getSubCategoryOptions(
            @Parameter(description = "功能大分類 (例如: FUNCTION, ROLE, GROUP)", required = true, example = "FUNCTION")
            @PathVariable String category,
            @Parameter(description = "租戶識別碼 (可選)", example = "tenant-01")
            @RequestParam(required = false) String tenant) {
        List<SettingOptionGottenResult> options = getSettingOptionUseCase.getSubCategoryOptions(tenant, category);
        List<SettingOptionGottenResource> response = options.stream()
                .map(settingResourceAssembler::toOptionResource)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 刪除指定 ID 之設定 (邏輯刪除)
     *
     * @param id 設定主鍵 ID
     * @return 204 No Content
     */
    @Operation(summary = "邏輯刪除設定", description = "依據主鍵 ID 執行邏輯刪除 (activeFlag 設為 'N')")
    @ApiResponse(responseCode = "204", description = "刪除成功 (無回傳內容)")
    @ApiResponse(responseCode = "404", description = "查無指定 ID 之設定項目")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "設定主鍵 ID", required = true, example = "1")
            @PathVariable Long id) {
        deleteSettingUseCase.delete(new DeleteSettingCommand(id));
        return ResponseEntity.noContent().build();
    }

    /**
     * 取得指定租戶已啟用之系統語系列表 (供 Header 地球選單使用)
     *
     * @param tenant 租戶代碼 (可選)
     * @return 語系選項清單
     */
    @Operation(summary = "取得指定租戶之啟用系統語系列表", description = "依據租戶代碼檢索 category=LOCALE 且有效之語系配置，由各 Tenant 獨立定義與維護，不使用全域回退")
    @ApiResponse(responseCode = "200", description = "檢索成功")
    @GetMapping("/locales")
    public ResponseEntity<List<TenantLocaleOptionGottenResource>> getTenantLocales(
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        List<SettingGottenResult> list = getSettingUseCase.getByCategory(resolvedTenant, "LOCALE");

        List<TenantLocaleOptionGottenResource> response = list.stream()
                .map(item -> new TenantLocaleOptionGottenResource(
                        item.id(),
                        item.tenant(),
                        item.code(),
                        item.name(),
                        item.description(),
                        "DEFAULT".equalsIgnoreCase(item.value()),
                        item.priorityNo(),
                        item.activeFlag()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 取得指定租戶之所有系統語系列表 (包含停用，供語系維護彈窗使用)
     *
     * @param tenant 租戶代碼 (可選)
     * @return 完整語系清單
     */
    @Operation(summary = "取得指定租戶之所有語系列表 (含停用)", description = "供維護彈窗檢視與編輯當前租戶的所有語系項目")
    @ApiResponse(responseCode = "200", description = "檢索成功")
    @GetMapping("/locales/all")
    public ResponseEntity<List<TenantLocaleOptionGottenResource>> getAllTenantLocales(
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        var query = settingResourceAssembler.toPagedQuery(
                resolvedTenant, "LOCALE", null, null, null, null, 0, 100);
        var pagedResult = getSettingPagedUseCase.getPaged(query);

        List<TenantLocaleOptionGottenResource> response = pagedResult.content().stream()
                .map(item -> new TenantLocaleOptionGottenResource(
                        item.id(),
                        item.tenant(),
                        item.code(),
                        item.name(),
                        item.description(),
                        "DEFAULT".equalsIgnoreCase(item.value()),
                        item.priorityNo(),
                        item.activeFlag()
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 設定指定語系為該租戶之預設語系
     *
     * @param id 設定主鍵 ID
     * @return 更新後之語系資源
     */
    @Operation(summary = "設定為租戶預設語系", description = "將該語系設為 DEFAULT，並清除該租戶下其他語系的預設標記")
    @ApiResponse(responseCode = "200", description = "設定成功")
    @PutMapping("/locales/{id}/default")
    public ResponseEntity<TenantLocaleOptionGottenResource> setLocaleDefault(
            @Parameter(description = "設定主鍵 ID", required = true, example = "1")
            @PathVariable Long id) {
        SettingGottenResult target = getSettingUseCase.getById(id);
        String tenant = target.tenant();

        // 取得該租戶所有語系，若有舊的 DEFAULT 則更新清空
        List<SettingGottenResult> allLocales = getSettingUseCase.getByCategory(tenant, "LOCALE");
        for (SettingGottenResult item : allLocales) {
            if ("DEFAULT".equalsIgnoreCase(item.value()) && !item.id().equals(id)) {
                var clearCommand = new UpdateSettingCommand(
                        item.id(),
                        item.category(),
                        item.subCategory(),
                        item.name(),
                        item.description(),
                        "",
                        item.priorityNo(),
                        item.activeFlag()
                );
                updateSettingUseCase.update(clearCommand);
            }
        }

        // 將 target 設為 DEFAULT
        var updateCommand = new UpdateSettingCommand(
                target.id(),
                target.category(),
                target.subCategory(),
                target.name(),
                target.description(),
                "DEFAULT",
                target.priorityNo(),
                target.activeFlag()
        );
        SettingUpdatedResult updated = updateSettingUseCase.update(updateCommand);

        return ResponseEntity.ok(new TenantLocaleOptionGottenResource(
                updated.id(),
                updated.tenant(),
                updated.code(),
                updated.name(),
                updated.description(),
                true,
                updated.priorityNo(),
                updated.activeFlag()
        ));
    }

    /**
     * 取得指定租戶已啟用之側邊欄選單清單
     *
     * @param tenant 租戶代碼 (可選)
     * @return 側邊欄選單項目清單
     */
    @Operation(summary = "取得指定租戶之啟用側邊欄選單清單", description = "依據租戶代碼檢索 category=MENU 且有效之選單配置，依 priorityNo 排序")
    @ApiResponse(responseCode = "200", description = "檢索成功")
    @GetMapping("/menus")
    public ResponseEntity<List<TenantMenuItemGottenResource>> getTenantMenus(
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        List<SettingGottenResult> list = getSettingUseCase.getByCategory(resolvedTenant, "MENU");

        List<TenantMenuItemGottenResource> response = list.stream()
                .map(settingResourceAssembler::toMenuItemResource)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 批次排序設定項目
     *
     * @param resources 排序請求項目清單 (包含 id 與選擇性的 priorityNo)
     * @return 排序更新後之設定回應清單 (依 priorityNo 升冪排序)
     */
    @Operation(summary = "批次排序設定項目", description = "更新傳入設定之 priorityNo，並確保回傳資料嚴格依照 priorityNo 升冪排序")
    @ApiResponse(responseCode = "200", description = "排序更新成功")
    @ApiResponse(responseCode = "400", description = "傳入參數格式不符")
    @PutMapping("/sort")
    public ResponseEntity<List<SettingGottenResource>> reorderSettings(
            @Valid @RequestBody List<ReorderSettingItemResource> resources) {
        var commands = settingResourceAssembler.toReorderCommands(resources);
        List<SettingGottenResult> results = reorderSettingUseCase.reorder(commands);
        List<SettingGottenResource> response = results.stream()
                .map(settingResourceAssembler::toGottenResource)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 批次排序側邊欄選單項目
     *
     * @param resources 排序請求項目清單 (主要用於排序選單項目)
     * @return 排序更新後之側邊欄選單項目清單 (依 priorityNo 升冪排序)
     */
    @Operation(summary = "批次排序側邊欄選單項目", description = "主要用來排序側邊欄選單項目，更新 priorityNo 並保證依 priorityNo 升冪排序回傳")
    @ApiResponse(responseCode = "200", description = "選單排序更新成功")
    @ApiResponse(responseCode = "400", description = "傳入參數格式不符")
    @PutMapping("/menus/sort")
    public ResponseEntity<List<TenantMenuItemGottenResource>> reorderMenus(
            @Valid @RequestBody List<ReorderSettingItemResource> resources) {
        var commands = settingResourceAssembler.toReorderCommands(resources);
        List<SettingGottenResult> results = reorderSettingUseCase.reorder(commands);
        List<TenantMenuItemGottenResource> response = results.stream()
                .map(settingResourceAssembler::toMenuItemResource)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * 取得指定租戶之動態頁面配置與表單資料
     *
     * @param pageCode 頁面代碼 (例如: TENANT_PROFILE)
     * @param tenant   租戶代碼 (可選)
     * @return 動態頁面設定資料
     */
    @Operation(summary = "取得指定租戶之動態頁面配置資料", description = "依據租戶代碼與頁面代碼 (pageCode) 檢索動態頁面表單資料")
    @ApiResponse(responseCode = "200", description = "檢索成功")
    @GetMapping("/pages/{pageCode}")
    public ResponseEntity<SettingGottenResource> getPageSetting(
            @PathVariable String pageCode,
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        try {
            SettingGottenResult result = getSettingUseCase.getByScope(resolvedTenant, pageCode);
            return ResponseEntity.ok(settingResourceAssembler.toGottenResource(result));
        } catch (IllegalArgumentException e) {
            // 若尚無此頁面配置，回傳預設空結構
            return ResponseEntity.ok(new SettingGottenResource(
                    null,
                    resolvedTenant,
                    "DYNAMIC_PAGE",
                    "PAGE_DATA",
                    pageCode,
                    pageCode,
                    "Dynamic Page Form Data",
                    "{}",
                    1,
                    "Y"
            ));
        }
    }

    /**
     * 儲存或更新指定租戶之動態頁面配置與表單資料
     *
     * @param pageCode     頁面代碼 (例如: TENANT_PROFILE)
     * @param tenant       租戶代碼 (可選)
     * @param pageDataJson 動態表單 JSON 資料
     * @return 儲存後之設定資料
     */
    @Operation(summary = "儲存或更新指定租戶之動態頁面配置資料", description = "儲存各租戶專屬之動態表單資料 (JSON 字串)")
    @ApiResponse(responseCode = "200", description = "儲存成功")
    @PutMapping("/pages/{pageCode}")
    public ResponseEntity<SettingGottenResource> savePageSetting(
            @PathVariable String pageCode,
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant,
            @RequestBody(required = false) String pageDataJson) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        String dataJson = (pageDataJson != null && !pageDataJson.isBlank()) ? pageDataJson : "{}";

        try {
            SettingGottenResult existing = getSettingUseCase.getByScope(resolvedTenant, pageCode);
            var updateCommand = new UpdateSettingCommand(
                    existing.id(),
                    existing.category(),
                    existing.subCategory(),
                    existing.name(),
                    existing.description(),
                    dataJson,
                    existing.priorityNo(),
                    "Y"
            );
            var updated = updateSettingUseCase.update(updateCommand);
            return ResponseEntity.ok(new SettingGottenResource(
                    updated.id(),
                    updated.tenant(),
                    updated.category(),
                    updated.subCategory(),
                    updated.code(),
                    updated.name(),
                    updated.description(),
                    updated.value(),
                    updated.priorityNo(),
                    updated.activeFlag()
            ));
        } catch (IllegalArgumentException e) {
            var createCommand = new CreateSettingCommand(
                    resolvedTenant,
                    "DYNAMIC_PAGE",
                    "PAGE_DATA",
                    pageCode,
                    pageCode,
                    "Dynamic Page Form Data for " + pageCode,
                    dataJson,
                    1
            );
            var created = createSettingUseCase.create(createCommand);
            return ResponseEntity.ok(new SettingGottenResource(
                    created.id(),
                    created.tenant(),
                    created.category(),
                    created.subCategory(),
                    created.code(),
                    created.name(),
                    created.description(),
                    created.value(),
                    created.priorityNo(),
                    created.activeFlag()
            ));
        }
    }

    /**
     * 取得指定租戶之動態頁面欄位定義 Schema
     *
     * @param pageCode 頁面代碼 (例如: TENANT_PROFILE)
     * @param tenant   租戶代碼 (可選)
     * @return 動態頁面 Schema 設定資料
     */
    @Operation(summary = "取得指定租戶之動態頁面欄位定義 Schema", description = "依據租戶代碼與頁面代碼檢索表單設計 Schema (JSON 字串)")
    @ApiResponse(responseCode = "200", description = "檢索成功")
    @GetMapping("/pages/{pageCode}/schema")
    public ResponseEntity<SettingGottenResource> getPageSchema(
            @PathVariable String pageCode,
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        String schemaCode = pageCode + "_SCHEMA";
        try {
            SettingGottenResult result = getSettingUseCase.getByScope(resolvedTenant, schemaCode);
            return ResponseEntity.ok(settingResourceAssembler.toGottenResource(result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(new SettingGottenResource(
                    null,
                    resolvedTenant,
                    "DYNAMIC_PAGE",
                    "PAGE_SCHEMA",
                    schemaCode,
                    pageCode + " Schema",
                    "Dynamic Page Form Schema for " + pageCode,
                    "[]",
                    1,
                    "Y"
            ));
        }
    }

    /**
     * 儲存或更新指定租戶之動態頁面欄位定義 Schema
     *
     * @param pageCode   頁面代碼 (例如: TENANT_PROFILE)
     * @param tenant     租戶代碼 (可選)
     * @param schemaJson 動態表單 Schema JSON 資料
     * @return 儲存後之 Schema 設定資料
     */
    @Operation(summary = "儲存或更新指定租戶之動態頁面欄位定義 Schema", description = "儲存各租戶自定義之表單設計結構 Schema (JSON 字串)")
    @ApiResponse(responseCode = "200", description = "儲存成功")
    @PutMapping("/pages/{pageCode}/schema")
    public ResponseEntity<SettingGottenResource> savePageSchema(
            @PathVariable String pageCode,
            @Parameter(description = "租戶識別碼 (可選)", example = "CW")
            @RequestParam(required = false) String tenant,
            @RequestBody(required = false) String schemaJson) {
        String resolvedTenant = (tenant != null && !tenant.isBlank())
                ? tenant
                : UserContextHolder.getTenant();
        if (resolvedTenant == null || resolvedTenant.isBlank()) {
            resolvedTenant = "CW";
        }

        String schemaCode = pageCode + "_SCHEMA";
        String contentJson = (schemaJson != null && !schemaJson.isBlank()) ? schemaJson : "[]";

        try {
            SettingGottenResult existing = getSettingUseCase.getByScope(resolvedTenant, schemaCode);
            var updateCommand = new UpdateSettingCommand(
                    existing.id(),
                    existing.category(),
                    existing.subCategory(),
                    existing.name(),
                    existing.description(),
                    contentJson,
                    existing.priorityNo(),
                    "Y"
            );
            var updated = updateSettingUseCase.update(updateCommand);
            return ResponseEntity.ok(new SettingGottenResource(
                    updated.id(),
                    updated.tenant(),
                    updated.category(),
                    updated.subCategory(),
                    updated.code(),
                    updated.name(),
                    updated.description(),
                    updated.value(),
                    updated.priorityNo(),
                    updated.activeFlag()
            ));
        } catch (IllegalArgumentException e) {
            var createCommand = new CreateSettingCommand(
                    resolvedTenant,
                    "DYNAMIC_PAGE",
                    "PAGE_SCHEMA",
                    schemaCode,
                    pageCode + " Schema",
                    "Dynamic Page Form Schema for " + pageCode,
                    contentJson,
                    1
            );
            var created = createSettingUseCase.create(createCommand);
            return ResponseEntity.ok(new SettingGottenResource(
                    created.id(),
                    created.tenant(),
                    created.category(),
                    created.subCategory(),
                    created.code(),
                    created.name(),
                    created.description(),
                    created.value(),
                    created.priorityNo(),
                    created.activeFlag()
            ));
        }
    }
}
