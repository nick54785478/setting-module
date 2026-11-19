package com.module.setting.application.service;

import com.module.setting.application.port.out.SettingCategoryRepositoryPort;
import com.module.setting.application.shared.command.CreateCategoryCommand;
import com.module.setting.application.shared.command.UpdateCategoryCommand;
import com.module.setting.application.shared.context.UserContextHolder;
import com.module.setting.application.shared.result.CategoryCreatedResult;
import com.module.setting.application.shared.result.CategoryGottenResult;
import com.module.setting.application.shared.result.CategoryUpdatedResult;
import com.module.setting.application.shared.vo.YesNo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingCategoryApplicationServiceTest {

    @Mock
    private SettingCategoryRepositoryPort categoryRepositoryPort;

    private SettingCategoryApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new SettingCategoryApplicationService(categoryRepositoryPort);
        UserContextHolder.set("CW", "test.user@cw.com");
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("查詢所有有效維度：應委由 repositoryPort 查詢")
    void testGetAll_CallsPort() {
        CategoryGottenResult item = CategoryGottenResult.of(
                1L, "CW", "SYSTEM", "系統維度", "描述", 1, YesNo.Y, LocalDateTime.now(), LocalDateTime.now()
        );
        when(categoryRepositoryPort.findAllByTenant("CW", YesNo.Y)).thenReturn(List.of(item));

        List<CategoryGottenResult> result = applicationService.getAll("CW");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("SYSTEM");
    }

    @Test
    @DisplayName("依 ID 查詢維度：查無資料時應拋出 IllegalArgumentException")
    void testGetById_NotFound_ThrowsException() {
        when(categoryRepositoryPort.findByIdAndActiveFlag(999L, YesNo.Y)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with ID: 999");
    }

    @Test
    @DisplayName("建立維度：代碼或名稱為空時應拋出異常")
    void testCreate_BlankCode_ThrowsException() {
        CreateCategoryCommand command = new CreateCategoryCommand("CW", "  ", "名稱", "描述", 1);

        assertThatThrownBy(() -> applicationService.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category code and name cannot be blank.");
    }

    @Test
    @DisplayName("建立維度：代碼重複時應拋出異常")
    void testCreate_DuplicateCode_ThrowsException() {
        CreateCategoryCommand command = new CreateCategoryCommand("CW", "SYSTEM", "系統維度", "描述", 1);
        when(categoryRepositoryPort.existsByTenantAndCode("CW", "SYSTEM")).thenReturn(true);

        assertThatThrownBy(() -> applicationService.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category code already exists for tenant: CW, code: SYSTEM");
    }

    @Test
    @DisplayName("建立維度：正常情境應呼叫 port.save")
    void testCreate_Success() {
        CreateCategoryCommand command = new CreateCategoryCommand("CW", "SYSTEM", "系統維度", "描述", 1);
        when(categoryRepositoryPort.existsByTenantAndCode("CW", "SYSTEM")).thenReturn(false);

        CategoryCreatedResult mockResult = CategoryCreatedResult.of(
                1L, "CW", "SYSTEM", "系統維度", "描述", 1, YesNo.Y, LocalDateTime.now(), LocalDateTime.now()
        );
        when(categoryRepositoryPort.save(any(CreateCategoryCommand.class))).thenReturn(mockResult);

        CategoryCreatedResult result = applicationService.create(command);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.code()).isEqualTo("SYSTEM");
        verify(categoryRepositoryPort).save(any(CreateCategoryCommand.class));
    }

    @Test
    @DisplayName("更新維度：ID 不存在時應拋出異常")
    void testUpdate_NotFound_ThrowsException() {
        UpdateCategoryCommand command = new UpdateCategoryCommand(888L, "新名稱", "新描述", 2, YesNo.Y);
        when(categoryRepositoryPort.findById(888L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.update(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with ID: 888");
    }

    @Test
    @DisplayName("刪除維度：應委派 port.deleteById")
    void testDelete_Success() {
        applicationService.delete(10L);

        verify(categoryRepositoryPort).deleteById(10L);
    }
}
