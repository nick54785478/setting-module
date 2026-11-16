package com.module.setting.presentation.controller;

import com.module.setting.application.port.in.CreateSettingUseCase;
import com.module.setting.application.port.in.DeleteSettingUseCase;
import com.module.setting.application.port.in.GetSettingOptionUseCase;
import com.module.setting.application.port.in.GetSettingPagedUseCase;
import com.module.setting.application.port.in.GetSettingUseCase;
import com.module.setting.application.port.in.UpdateSettingUseCase;
import com.module.setting.application.shared.command.DeleteSettingCommand;
import com.module.setting.application.shared.result.PagedDataGottenResult;
import com.module.setting.application.shared.result.SettingCreatedResult;
import com.module.setting.application.shared.result.SettingGottenResult;
import com.module.setting.application.shared.result.SettingOptionGottenResult;
import com.module.setting.application.shared.result.SettingUpdatedResult;
import com.module.setting.presentation.assembler.SettingResourceAssembler;
import com.module.setting.presentation.resource.in.CreateSettingResource;
import com.module.setting.presentation.resource.in.UpdateSettingResource;
import com.module.setting.presentation.resource.out.SettingCreatedResource;
import com.module.setting.presentation.resource.out.SettingGottenResource;
import com.module.setting.presentation.resource.out.SettingOptionGottenResource;
import com.module.setting.presentation.resource.out.SettingPagedGottenResource;
import com.module.setting.presentation.resource.out.SettingUpdatedResource;
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
    private final SettingResourceAssembler settingResourceAssembler;

    public SettingController(CreateSettingUseCase createSettingUseCase,
                             UpdateSettingUseCase updateSettingUseCase,
                             GetSettingUseCase getSettingUseCase,
                             GetSettingPagedUseCase getSettingPagedUseCase,
                             GetSettingOptionUseCase getSettingOptionUseCase,
                             DeleteSettingUseCase deleteSettingUseCase,
                             SettingResourceAssembler settingResourceAssembler) {
        this.createSettingUseCase = createSettingUseCase;
        this.updateSettingUseCase = updateSettingUseCase;
        this.getSettingUseCase = getSettingUseCase;
        this.getSettingPagedUseCase = getSettingPagedUseCase;
        this.getSettingOptionUseCase = getSettingOptionUseCase;
        this.deleteSettingUseCase = deleteSettingUseCase;
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
}
