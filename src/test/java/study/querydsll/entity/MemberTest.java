package study.querydsll.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Hunseong on 2022/04/26
 */
@SpringBootTest
@Transactional
@Commit
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @PersistenceUnit
    EntityManagerFactory factory;

    @Test
    void entityTest() {
        
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        // PersistenceContext 초기화
        em.flush();
        em.clear();

        // when
        List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();

        // then
        assertThat(result).extracting("age").containsExactly(10, 20, 30, 40);

        // Lazy Loading 확인
        for (Member member : result) {
            assertThat(factory.getPersistenceUnitUtil().isLoaded(member.getTeam())).isFalse();
        }
    }
}