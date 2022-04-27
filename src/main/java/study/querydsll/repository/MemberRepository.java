package study.querydsll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsll.entity.Member;

import java.util.List;

/**
 * Created by Hunseong on 2022/04/27
 */
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsername(String username);
}
