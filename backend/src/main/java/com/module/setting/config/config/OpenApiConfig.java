package com.module.setting.config.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 與 Swagger UI 全域配置類 (OpenApiConfig)
 * <p>
 * 集中於專案根目錄下的 config 套件，定義通用功能設定模組 (Setting Module) 之 API 規範資訊、
 * 授權標頭 (X-Tenant-Id 多租戶標頭) 以及文件標籤說明。
 * </p>
 */
@Configuration
public class OpenApiConfig {

    private static final String TENANT_HEADER_SCHEME = "TenantHeader";

    /**
     * 建構 OpenAPI 核心規格與全域安全架構
     *
     * @return 完整客製化之 OpenAPI 規範物件
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Setting Module API")
                        .version("1.0.0")
                        .description("通用功能設定模組 (Setting Module) RESTful 介面文件。<br>" +
                                "支援功能維度定位 (Tenant, Category, SubCategory, Code)、動態分頁查詢 (Specification)、" +
                                "元資料 (META) 選項層級檢索與多租戶動態上下文。")
                        .contact(new Contact()
                                .name("Core Architecture Team")
                                .email("architecture@company.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .tags(List.of(
                        new Tag().name("Setting Management").description("設定主檔 CRUD 與 Specification 多條件動態分頁查詢"),
                        new Tag().name("Metadata & Options").description("元資料驅動 (META) 之 Category 與 SubCategory 選項層級檢索")
                ))
                .components(new Components()
                        .addSecuritySchemes(TENANT_HEADER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Tenant-Id")
                                .description("多租戶隔離識別碼 (Request Header)。若未傳入，系統預設自動採用 'ALL' / 'default'。")))
                .addSecurityItem(new SecurityRequirement().addList(TENANT_HEADER_SCHEME));
    }
}
