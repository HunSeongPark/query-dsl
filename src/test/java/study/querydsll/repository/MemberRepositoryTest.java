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
}