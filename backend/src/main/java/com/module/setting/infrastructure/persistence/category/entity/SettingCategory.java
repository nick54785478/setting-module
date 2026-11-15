package com.module.setting.infrastructure.persistence.category.entity;

import com.module.setting.application.shared.vo.YesNo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 維度元資料實體 (SettingCategory Entity)
 * <p>
 * 專門負責管理系統業務大分類字典 (Meta Category)，獨立於一般業務設定 (Setting)，
 * 避免元資料字典與業務配置記錄混淆。
 * </p>
 */
@Getter
@Setter
@Entity
@Table(
        name = "setting_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_setting_category_tenant_code",
                columnNames = {"tenant", "code"}
        ),
        indexes = {
                @Index(name = "idx_category_tenant_active_priority", columnList = "tenant, active_flag, priority_no"),
                @Index(name = "idx_category_priority", columnList = "priority_no")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 租戶識別碼 (ALL 代表全域共用)
     */
    @Column(name = "tenant", nullable = false, length = 64)
    private String tenant;

    /**
     * 維度唯一代碼 (例如: SYSTEM, ROLE, GROUP, FUNCTION)
     */
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    /**
     * 維度中文名稱 (例如: 系統配置, 角色配置)
     */
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    /**
     * 維度備註說明
     */
    @Column(name = "description", length = 512)
    private String description;

    /**
     * 排序序號
     */
    @Column(name = "priority_no", nullable = false)
    private Integer priorityNo = 1;

    /**
     * 有效性狀態 (Y: 有效, N: 停用)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "active_flag", nullable = false, length = 1)
    private YesNo activeFlag = YesNo.Y;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static SettingCategory of(String tenant, String code, String name, String description, Integer priorityNo) {
        SettingCategory category = new SettingCategory();
        category.tenant = (tenant != null && !tenant.isBlank()) ? tenant.trim() : "ALL";
        category.code = code != null ? code.trim() : "";
        category.name = name != null ? name.trim() : "";
        category.description = description;
        category.priorityNo = priorityNo != null ? priorityNo : 1;
        category.activeFlag = YesNo.Y;
        return category;
    }

    public void update(String name, String description, Integer priorityNo, YesNo activeFlag) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        this.description = description;
        if (priorityNo != null) {
            this.priorityNo = priorityNo;
        }
        if (activeFlag != null) {
            this.activeFlag = activeFlag;
        }
    }

    public void delete() {
        this.activeFlag = YesNo.N;
    }
}
