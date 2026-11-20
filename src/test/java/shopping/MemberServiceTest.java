package shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import({MemberService.class, PasswordConfig.class})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    void registerNormalizesEmailAndStoresPasswordHash() {
        Member saved = memberService.register("  Member@Example.COM  ", "password123!");
        entityManager.flush();
        entityManager.clear();

        Member found = memberRepository.findByEmail("member@example.com").orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getEmail()).isEqualTo("member@example.com");
        assertThat(found.getPasswordHash()).isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", found.getPasswordHash())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"member@example.com", "  MEMBER@EXAMPLE.COM  "})
    void registerRejectsDuplicateEmailWithoutAddingMember(String email) {
        memberService.register("member@example.com", "password123!");
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> memberService.register(email, "another-password"))
                .isInstanceOf(DuplicateEmailException.class);

        assertThat(memberRepository.count()).isEqualTo(1);
        Member found = memberRepository.findByEmail("member@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("password123!", found.getPasswordHash())).isTrue();
    }

    @Test
    void registerPreservesPasswordWhitespace() {
        memberService.register("member@example.com", " password123! ");
        entityManager.flush();
        entityManager.clear();

        Member found = memberRepository.findByEmail("member@example.com").orElseThrow();

        assertThat(passwordEncoder.matches(" password123! ", found.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("password123!", found.getPasswordHash())).isFalse();
    }
}
