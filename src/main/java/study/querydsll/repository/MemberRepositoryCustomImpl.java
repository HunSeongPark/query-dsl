package study.querydsll.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import study.querydsll.dto.MemberSearchCond;
import study.querydsll.dto.MemberTeamDto;
import study.querydsll.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsll.entity.QMember.member;
import static study.querydsll.entity.QTeam.team;

/**
 * Created by Hunseong on 2022/04/27
 */
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCond condition) {
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
