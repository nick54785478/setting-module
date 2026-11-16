package com.module.setting.infrastructure.persistence.customisation.entity;

import com.module.setting.application.shared.vo.YesNo;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 個人化配置 Aggregate Root 實體 (Customisation Entity)
 * <p>
 * 支援多租戶、使用者、前端元件與種類之個人化配置 (如 Table 欄位自訂顯示 FieldView)。
 * </p>
 */
@Entity
@Getter
@ToString
@Table(
    name = "customisation",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_customisation_scope",
            columnNames = { "tenant", "username", "component", "type" }
        )
    },
    indexes = {
        @Index(name = "idx_customisation_lookup", columnList = "tenant, username, component")
    }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private CustomisationScope scope;

    @Column(name = "value", columnDefinition = "TEXT", nullable = false)
    private String value; // 儲存逗號分隔之欄位清單字串，例如: "tenant,category,code,name,value"

    @Enumerated(EnumType.STRING)
    @Column(name = "active_flag", length = 1, nullable = false)
    private YesNo activeFlag = YesNo.Y;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    /**
     * 領域工廠方法：建立個人化配置
     *
     * @param scope 個人化配置範圍
     * @param value 已序列化之 JSON 欄位清單字串
     * @return {@link Customisation}
     */
    public static Customisation create(CustomisationScope scope, String value) {
        Customisation customisation = new Customisation();
        customisation.scope = scope;
        customisation.value = value;
        customisation.activeFlag = YesNo.Y;
        return customisation;
    }

    /**
     * 領域行為：更新配置值
     *
     * @param newValue 新的 JSON 配置字串
     */
    public void updateValue(String newValue) {
        this.value = newValue;
    }

    /**
     * 領域行為：邏輯刪除 / 停用
     */
    public void delete() {
        this.activeFlag = YesNo.N;
    }

    /**
     * 領域行為：重新啟用
     */
    public void activate() {
        this.activeFlag = YesNo.Y;
    }
}
