package study.querydsll.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsll.entity.Member;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Hunseong on 2022/04/27
 */
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {

        // given
        Member member = new Member("member1", 10);


        // when
        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        List<Member> allResult = memberJpaRepository.findAll();
        List<Member> usernameResult = memberJpaRepository.findByUsername("member1");

        // then
        assertThat(findMember).isEqualTo(member);
        assertThat(allResult).containsExactly(member);
        assertThat(usernameResult).containsExactly(member);
    }

    @Test
    void basicTest_QueryDsl() {

        // given
        Member member = new Member("member1", 10);


        // when
        memberJpaRepository.save(member);
        List<Member> allResult = memberJpaRepository.findAll_QueryDsl();
        List<Member> usernameResult = memberJpaRepository.findByUsername_QueryDsl("member1");

        // then
        assertThat(allResult).containsExactly(member);
        assertThat(usernameResult).containsExactly(member);
    }
}