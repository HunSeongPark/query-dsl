package study.querydsll.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsll.dto.MemberSearchCond;
import study.querydsll.dto.MemberTeamDto;
import study.querydsll.dto.QMemberTeamDto;
import study.querydsll.entity.Member;

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

    /**
     * 단순한 페이징
     * 조회 쿼리 / 페이징 쿼리를 분리하지 않고 fetchResults()를 통해 한번에 조회
     */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCond condition, Pageable pageable) {

        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .join(member.team, team)
                .where(memberSearchEq(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long totalCount = results.getTotal();

        return new PageImpl<>(content, pageable, totalCount);
    }

    /**
     * 복잡한 페이징
     * 조회 쿼리 / 페이징 쿼리를 분리하여 성능 최적화 가능
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCond condition, Pageable pageable) {

        // 조회 쿼리
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .join(member.team, team)
                .where(memberSearchEq(condition))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리
        long totalCount = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(memberSearchEq(condition))
                .fetchCount();

//        return new PageImpl<>(content, pageable, totalCount);

        /**
         * 카운트 쿼리 최적화
         * PageableExecutionUtils.getPage(content, pageable, () ->) 사용
         * count 쿼리 생략 가능한 경우 생략 처리
         * ex1) 페이지의 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
         * ex2) 마지막 페이지 (offset + 컨텐츠 사이즈를 더해 전체 사이즈를 구함)
         */
        JPAQuery<Member> countQuery = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(memberSearchEq(condition));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
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
