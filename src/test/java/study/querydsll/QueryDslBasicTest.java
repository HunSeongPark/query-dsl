package study.querydsll;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsll.dto.MemberDto;
import study.querydsll.dto.QMemberDto;
import study.querydsll.entity.Member;
import study.querydsll.entity.QMember;
import study.querydsll.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.StringUtils.hasText;
import static study.querydsll.entity.QMember.member;
import static study.querydsll.entity.QTeam.team;

/**
 * Created by Hunseong on 2022/04/27
 */
@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @PersistenceContext
    EntityManager em;

    @PersistenceUnit
    EntityManagerFactory emf;

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
     * ?????? ??????
     * 1. ?????? ?????? ???????????? (desc)
     * 2. ?????? ?????? ???????????? (asc)
     * 3. ???, 2?????? ?????? ????????? ????????? ???????????? ?????? (nulls last)
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

    @Test
    void aggregation() {

//        Member member1 = new Member("member1", 10, teamA);
//        Member member2 = new Member("member2", 20, teamA);
//        Member member3 = new Member("member3", 30, teamB);
//        Member member4 = new Member("member4", 40, teamB);

        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        // count
        assertThat(tuple.get(member.count())).isEqualTo(4);

        // sum 10+20+30+40
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);

        // avg (10+20+30+40) / 4
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);

        // max 40
        assertThat(tuple.get(member.age.max())).isEqualTo(40);

        // max 40
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void group() {

//        Member member1 = new Member("member1", 10, teamA);
//        Member member2 = new Member("member2", 20, teamA);
//        Member member3 = new Member("member3", 30, teamB);
//        Member member4 = new Member("member4", 40, teamB);

        // ?????? ????????? ??? ?????? ?????? ??????
        List<Tuple> fetch = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
//                .having(team.name.eq("teamA"))
                .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamB.get(team.name)).isEqualTo("teamB");

        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    @Test
    void join() {

        // teamA??? ????????? ?????? ??????
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("username").containsExactly("member1", "member2");
    }

    @Test
    void theta_join() {

        // ?????? ????????? ??? ????????? ?????? ?????? ??????

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result).extracting("username").containsExactly("teamA", "teamB");
    }

    @Test
    void join_on_filtering() {

        // ????????? ??? join, ??? ????????? teamA??? ?????? ??????, ????????? ?????? ??????
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

    }

    @Test
    void join_on_no_relation() {

        // ????????? ????????? ?????? ????????? ?????? ?????? ?????? ??????

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void fetchJoin_no() {

        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(isLoaded).isFalse();

    }

    @Test
    void fetchJoin() {

        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(isLoaded).isTrue();
    }

    @Test
    void subQuery() {

        // ????????? ?????? ?????? ?????? ??????
        QMember memberSub = new QMember("memberSub");

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)))
                .fetchOne();

        assertThat(findMember.getAge()).isEqualTo(40);

    }

    @Test
    void projection_tuple() {

        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
            System.out.println("username = " + tuple.get(member.username));
            System.out.println("age = " + tuple.get(member.age));
        }
    }

    @Test
    void projection_dto_jpa() {

        List<MemberDto> result = em.createQuery
                ("select new study.querydsll.dto.MemberDto(m.username, m.age) from Member m"
                        , MemberDto.class).getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

    }

    @Test
    void projection_dto_setter() {

        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void projection_dto_fields() {

        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, member.username.as("name"), member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void projection_dto_constructor() {

        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void projection_dto_queryprojection() {

        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    void dynamic_query_BooleanBuilder() {

        // dynamic variable
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {

        BooleanBuilder builder = new BooleanBuilder();

        if (!hasText(usernameParam)) {
            builder.and(member.username.eq(usernameParam));
        }
        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamic_query_where_parameter() {

        // dynamic variable
        String usernameParam = "member2";
        Integer ageParam = 20;

        List<Member> result = searchMember2(usernameParam, ageParam);

        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {

        return queryFactory
                .selectFrom(member)
                .where(memberEq(usernameParam, ageParam))
                .fetch();
    }

    // BooleanBuilder ??????
    private BooleanBuilder memberEq(String usernameParam, Integer ageParam) {
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }

    private BooleanBuilder usernameEq(String usernameParam) {
        return hasText(usernameParam) ?
                new BooleanBuilder(member.username.eq(usernameParam)) : new BooleanBuilder();
    }

    private BooleanBuilder ageEq(Integer ageParam) {
        return ageParam != null ?
                new BooleanBuilder(member.age.eq(ageParam)) : new BooleanBuilder();
    }

    @Test
    void bulk_update() {

        long count = queryFactory
                .update(member)
                .set(member.username, "?????????")
                .where(member.age.gt(29))
                .execute();

        em.flush();
        em.clear();

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.gt(29))
                .fetch();

        assertThat(members).extracting("username").containsExactly("?????????", "?????????");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void bulk_update_add() {

        Member beforeFirstMember = queryFactory
                .selectFrom(member)
                .where(member.age.gt(29))
                .fetchFirst();

        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .where(member.age.gt(29))
                .execute();

        em.flush();
        em.clear();

        Member afterFirstMember = queryFactory
                .selectFrom(member)
                .where(member.age.gt(29))
                .fetchFirst();

        assertThat(afterFirstMember.getAge()).isEqualTo(beforeFirstMember.getAge() + 1);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void bulk_delete() {

        long beforeMemberCount = queryFactory
                .selectFrom(member)
                .fetchCount();

        long deleteCount = queryFactory
                .delete(member)
                .where(member.age.gt(29))
                .execute();

        long afterMemberCount = queryFactory
                .selectFrom(member)
                .fetchCount();

        assertThat(afterMemberCount).isEqualTo(beforeMemberCount - deleteCount);
    }

    @Test
    void sql_function() {

        // Member1 -> M1??? ???????????? ??????

        List<String> result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
        
        // output
        // s = M1
        // s = M2
        // s = M3
        // s = M4
    }
    
    @Test
    void sql_embedded_function() {
        
        // username ????????? ?????? ??? ?????? (username??? ??????????????? ???????????? ?????? ?????? ??????)

        em.persist(new Member("Member1"));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.username.eq(member.username.lower()))
                .fetch();

        assertThat(result.size()).isEqualTo(4);
    }
}
