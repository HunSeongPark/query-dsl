package study.querydsll;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsll.entity.Member;
import study.querydsll.entity.QMember;
import study.querydsll.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsll.entity.QMember.*;

/**
 * Created by Hunseong on 2022/04/27
 */
@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    void startJPQL() {
        
        // find member1
        String query = "select m from Member m where m.username = :username";

        Member findMember = em.createQuery(query, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    @Test
    void startQueryDsl() {
        
        // find Member1
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    void search_and() {

        // find member where username = member1 && age = 10
        Member findMember = queryFactory
                .selectFrom(member)
//                .where(member.username.eq("member1").and(member.age.eq(10)))
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    void search_or() {

        // find member where username = member1 || age = 20
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").or(member.age.eq(20)))
                .fetch();

        assertThat(result.size()).isEqualTo(2);

        assertThat(result).extracting("username")
                .containsExactly("member1", "member2");

    }


    /**
     * 정렬 조건
     * 1. 회원 나이 내림차순 (desc)
     * 2. 회원 이름 오름차순 (asc)
     * 3. 단, 2에서 회원 이름이 없으면 마지막에 출력 (nulls last)
     */
    @Test
    void sort() {
        Member member1 = new Member("member1", 30);
        Member member2 = new Member("member2", 21);
        Member member35 = new Member("member35", 23);
        Member member4 = new Member("member4", 23);
        Member memberNull = new Member(null, 30);
        em.persist(member1);
        em.persist(member2);
        em.persist(member35);
        em.persist(member4);
        em.persist(memberNull);

        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }
}
