package study.querydsll.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsll.dto.MemberSearchCond;
import study.querydsll.dto.MemberTeamDto;
import study.querydsll.entity.Member;
import study.querydsll.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Hunseong on 2022/04/27
 */
@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() {

        // given
        Member member = new Member("member1", 10);


        // when
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).get();
        List<Member> allResult = memberRepository.findAll();
        List<Member> usernameResult = memberRepository.findByUsername("member1");

        // then
        assertThat(findMember).isEqualTo(member);
        assertThat(allResult).containsExactly(member);
        assertThat(usernameResult).containsExactly(member);
    }

    @Test
    void searchTest() {

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

        MemberSearchCond condition = new MemberSearchCond(
                null, "teamB", 35, 40);

        List<MemberTeamDto> whereResult = memberRepository.search(condition);

        assertThat(whereResult).extracting("teamName").containsExactly("teamB");
    }
}