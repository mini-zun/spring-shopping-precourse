package shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(TokenService.class)
class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private MemberRepository members;

    @Autowired
    private MemberTokenRepository tokens;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private Clock clock;

    private final Instant now = Instant.parse("2026-11-21T00:00:00Z");

    @BeforeEach
    void fixTime() {
        given(clock.instant()).willReturn(now);
    }

    @Test
    void persistsTokenHashWithMemberAndExpiry() {
        Member member = members.save(new Member("member@example.com", "hash"));
        AuthTokenResponse response = tokenService.issue(member);
        entityManager.flush();
        entityManager.clear();
        MemberToken stored = tokens.findById(TokenService.hashToken(response.token())).orElseThrow();
        assertThat(response.token()).matches("[A-Za-z0-9_-]{43}");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(86400);
        assertThat(stored.getMemberId()).isEqualTo(member.getId());
        assertThat(stored.getExpiresAt()).isEqualTo(now.plusSeconds(86400));
        assertThat(tokens.findById(response.token())).isEmpty();
    }

    @Test
    void issuesDistinctTokensForSameMember() {
        Member member = members.save(new Member("member@example.com", "hash"));
        AuthTokenResponse first = tokenService.issue(member);
        AuthTokenResponse second = tokenService.issue(member);
        entityManager.flush();
        assertThat(first.token()).isNotEqualTo(second.token());
        assertThat(tokens.count()).isEqualTo(2);
    }
}
