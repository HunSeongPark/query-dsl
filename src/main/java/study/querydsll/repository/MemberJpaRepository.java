package study.querydsll.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsll.dto.MemberSearchCond;
import study.querydsll.dto.MemberTeamDto;
import study.querydsll.dto.QMemberDto;
import study.querydsll.dto.QMemberTeamDto;
import study.querydsll.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsll.entity.QMember.member;
import static study.querydsll.entity.QTeam.team;

/**
 * Created by Hunseong on 2022/04/27
 */
@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_QueryDsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findByUsername_QueryDsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    // ========== Member Search (MemberSearchCond) ========== //
    public List<MemberTeamDto> searchByBuilder(MemberSearchCond condition) {

        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .join(member.team, team)
                .where(builder)
                .fetch();
    }

    public List<MemberTeamDto> searchByWhereParam(MemberSearchCond condition) {
        return queryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .join(member.team, team)
                .where(memberSearchEq(condition))
                .fetch();
    }

    // BooleanBuilder 조립
    private BooleanBuilder memberSearchEq(MemberSearchCond condition) {
        return usernameEq(condition.getUsername())
                .and(teamNameEq(condition.getTeamName()))
                .and(ageGoe(condition.getAgeGoe()))
                .and(ageLoe(condition.getAgeLoe()));
    }

    private BooleanBuilder usernameEq(String username) {
        return hasText(username) ? new BooleanBuilder(member.username.eq(username)) : new BooleanBuilder();
    }

    private BooleanBuilder teamNameEq(String teamName) {
        return hasText(teamName) ? new BooleanBuilder(team.name.eq(teamName)) : new BooleanBuilder();
    }

    private BooleanBuilder ageGoe(Integer ageGoe) {
        return ageGoe != null ? new BooleanBuilder(member.age.goe(ageGoe)) : new BooleanBuilder();
    }

    private BooleanBuilder ageLoe(Integer ageLoe) {
        return ageLoe != null ? new BooleanBuilder(member.age.loe(ageLoe)) : new BooleanBuilder();
    }
}
