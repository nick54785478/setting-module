package com.module.setting.infrastructure.persistence.customisation.repository;

import com.module.setting.infrastructure.persistence.customisation.entity.Customisation;
import com.module.setting.infrastructure.persistence.customisation.vo.CustomisationScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomisationJpaRepositoryTest {

    @Autowired
    private CustomisationJpaRepository repository;

    @Test
    @DisplayName("測試 findByScope 是否能正常查詢出儲存的 Customisation")
    void testFindByScope() {
        CustomisationScope scope = CustomisationScope.of("CW", "nickgh.zhang@cw.com", "SettingComponent", "FieldView");
        Customisation customisation = Customisation.create(scope, "code,name,value");

        repository.saveAndFlush(customisation);

        Optional<Customisation> found = repository.findByScope(scope);
        assertThat(found).isPresent();
        assertThat(found.get().getValue()).isEqualTo("code,name,value");
    }
}
