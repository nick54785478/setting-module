package com.module.setting.presentation.controller;

import com.module.setting.application.port.in.CreateCategoryUseCase;
import com.module.setting.application.port.in.DeleteCategoryUseCase;
import com.module.setting.application.port.in.GetCategoryUseCase;
import com.module.setting.application.port.in.UpdateCategoryUseCase;
import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.presentation.assembler.CategoryResourceAssembler;
import com.module.setting.presentation.resource.in.CreateCategoryResource;
import com.module.setting.presentation.resource.in.UpdateCategoryResource;
import com.module.setting.presentation.resource.out.CategoryCreatedResource;
import com.module.setting.presentation.resource.out.CategoryGottenResource;
import com.module.setting.presentation.resource.out.CategoryUpdatedResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 維度元資料 REST 控制器 (SettingCategoryController)
 * <p>
 * 提供業務維度字典 (Meta Category) 之獨立管理端點，完全與業務設定 (Setting) 解耦。
 * </p>
 */
@Tag(name = "Meta Category Management", description = "維度元資料字典管理端點 (SYSTEM, ROLE, GROUP, FUNCTION 等業務大分類維護)")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class SettingCategoryController {

    private final GetCategoryUseCase getCategoryUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final CategoryResourceAssembler categoryResourceAssembler;

    /**
     * 取得維度元資料清單 (供下拉選單或字典清單展示)
     */
    @Operation(summary = "取得維度元資料清單", description = "查詢可用之業務大分類清單，包含 label 與 value 供前端下拉選單綁定")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @GetMapping
    public ResponseEntity<List<CategoryGottenResource>> list(
            @Parameter(description = "租戶識別碼 (可選)", example = "ALL")
            @RequestParam(required = false) String tenant,
            @Parameter(description = "是否包含已停用項目", example = "false")
            @RequestParam(defaultValue = "false") boolean includeInactive) {

        List<CategoryGottenResult> results = includeInactive
                ? getCategoryUseCase.getAllIncludeInactive(tenant)
                : getCategoryUseCase.getAll(tenant);

        List<CategoryGottenResource> response = results.stream()
                .map(categoryResourceAssembler::toGottenResource)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 依主鍵 ID 查詢單一維度
     */
    @Operation(summary = "查詢單一維度詳情", description = "依據主鍵 ID 取得維度元資料詳情")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "查無指定 ID 之維度")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryGottenResource> getById(@PathVariable Long id) {
        CategoryGottenResult result = getCategoryUseCase.getById(id);
        return ResponseEntity.ok(categoryResourceAssembler.toGottenResource(result));
    }

    /**
     * 建立一筆新維度
     */
    @Operation(summary = "建立新維度", description = "建立一筆全新的業務功能維度字典項目")
    @ApiResponse(responseCode = "201", description = "建立成功")
    @ApiResponse(responseCode = "400", description = "參數驗證失敗或代碼已存在")
    @PostMapping
    public ResponseEntity<CategoryCreatedResource> create(@Valid @RequestBody CreateCategoryResource resource) {
        CreateCategoryCommand command = new CreateCategoryCommand(
                resource.tenant(),
                resource.code(),
                resource.name(),
                resource.description(),
                resource.priorityNo()
        );
        CategoryCreatedResult result = createCategoryUseCase.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResourceAssembler.toCreatedResource(result));
    }

    /**
     * 更新指定 ID 之維度
     */
    @Operation(summary = "更新維度元資料", description = "更新維度展示名稱、描述、順序號或有效性狀態")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "404", description = "查無指定 ID 之維度")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryUpdatedResource> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryResource resource) {

        UpdateCategoryCommand command = new UpdateCategoryCommand(
                id,
                resource.name(),
                resource.description(),
                resource.priorityNo(),
                resource.activeFlag()
        );
        CategoryUpdatedResult result = updateCategoryUseCase.update(command);
        return ResponseEntity.ok(categoryResourceAssembler.toUpdatedResource(result));
    }

    /**
     * 刪除指定 ID 之維度 (邏輯刪除)
     */
    @Operation(summary = "邏輯刪除維度", description = "將指定維度之 activeFlag 標記為 N")
    @ApiResponse(responseCode = "204", description = "刪除成功")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteCategoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
