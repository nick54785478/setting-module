package com.module.setting.infrastructure.persistence.customisation.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 個人化配置範圍 (業務自然鍵坐標)
 * <p>
 * 唯一定位：[租戶] + [帳號] + [前端元件] + [偏好種類]
 * </p>
 */
@Getter
@Embeddable
@ToString
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomisationScope implements Serializable {

    @Column(name = "tenant", length = 50, nullable = false)
    private String tenant;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "component", length = 100, nullable = false)
    private String component;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    /**
     * 專用工廠方法：建立表格欄位偏好 (type 預設固定為 FieldView)
     */
    public static CustomisationScope ofFieldView(String tenant, String username, String component) {
        return of(tenant, username, component, "FieldView");
    }

    /**
     * 通用工廠方法
     */
    public static CustomisationScope of(String tenant, String username, String component, String type) {
        CustomisationScope scope = new CustomisationScope();
        scope.tenant = tenant;
        scope.username = username;
        scope.component = component;
        scope.type = type;
        return scope;
    }
}
