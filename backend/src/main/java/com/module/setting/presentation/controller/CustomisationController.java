package com.module.setting.presentation.controller;

import com.module.setting.application.port.in.GetCustomisationUseCase;
import com.module.setting.application.port.in.ResetCustomisationUseCase;
import com.module.setting.application.port.in.SaveCustomisationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 個人化配置控制器 (CustomisationController)
 * <p>
 * 提供使用者畫面自訂設定 (例如 Table 欄位自訂顯示 FieldView) 之查詢、儲存與重設端點。
 * 身分與租戶自動由 UserContextHolder 解析，欄位清單採用輕量化逗號分隔儲存。
 * </p>
 */
@Tag(name = "User Customisation", description = "使用者個人化配置端點 (Table 欄位顯示/順序 FieldView 等)")
@RestController
@RequestMapping("/api/v1/user/customisations")
@RequiredArgsConstructor
public class CustomisationController {

    private final GetCustomisationUseCase getCustomisationUseCase;
    private final SaveCustomisationUseCase saveCustomisationUseCase;
    private final ResetCustomisationUseCase resetCustomisationUseCase;

    /**
     * 查詢個人化欄位代碼清單
     *
     * @param component 前端元件名稱 (例如: "SettingComponent")
     * @param type      偏好種類 (例如: "FieldView")
     * @return 顯示之欄位清單 (例如: ["tenant", "category", "code"])
     */
    @Operation(summary = "查詢個人化配置", description = "依據元件名稱與偏好類型查詢當前使用者的個人化欄位清單")
    @ApiResponse(responseCode = "200", description = "查詢成功，若無自訂則回傳空陣列")
    @GetMapping("/{component}/{type}")
    public ResponseEntity<List<String>> getCustomisation(
            @Parameter(description = "前端元件名稱", example = "SettingComponent")
            @PathVariable String component,
            @Parameter(description = "偏好類型", example = "FieldView")
            @PathVariable(required = false) String type) {

        String preferenceType = (type != null && !type.isBlank()) ? type : "FieldView";
        List<String> results = getCustomisationUseCase.getCustomisation(component, preferenceType);
        return ResponseEntity.ok(results);
    }

    /**
     * 儲存個人化欄位代碼清單
     *
     * @param component 前端元件名稱 (例如: "SettingComponent")
     * @param type      偏好種類 (例如: "FieldView")
     * @param fields    欲顯示之欄位代碼清單
     * @return 儲存後之欄位清單
     */
    @Operation(summary = "儲存個人化配置", description = "儲存或覆寫當前使用者的個人化欄位清單")
    @ApiResponse(responseCode = "200", description = "儲存成功")
    @PutMapping("/{component}/{type}")
    public ResponseEntity<List<String>> saveCustomisation(
            @Parameter(description = "前端元件名稱", example = "SettingComponent")
            @PathVariable String component,
            @Parameter(description = "偏好類型", example = "FieldView")
            @PathVariable(required = false) String type,
            @RequestBody List<String> fields) {

        String preferenceType = (type != null && !type.isBlank()) ? type : "FieldView";
        List<String> saved = saveCustomisationUseCase.saveCustomisation(component, preferenceType, fields);
        return ResponseEntity.ok(saved);
    }

    /**
     * 還原預設配置
     *
     * @param component 前端元件名稱
     * @param type      偏好種類
     */
    @Operation(summary = "還原預設配置", description = "清除使用者的個人化自訂，還原回系統預設")
    @ApiResponse(responseCode = "204", description = "還原成功")
    @DeleteMapping("/{component}/{type}")
    public ResponseEntity<Void> resetCustomisation(
            @Parameter(description = "前端元件名稱", example = "SettingComponent")
            @PathVariable String component,
            @Parameter(description = "偏好類型", example = "FieldView")
            @PathVariable(required = false) String type) {

        String preferenceType = (type != null && !type.isBlank()) ? type : "FieldView";
        resetCustomisationUseCase.resetCustomisation(component, preferenceType);
        return ResponseEntity.noContent().build();
    }
}
