package study.querydsll.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsll.dto.MemberSearchCond;
import study.querydsll.dto.MemberTeamDto;

import java.util.List;

/**
 * Created by Hunseong on 2022/04/27
 */
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCond condition);

    Page<MemberTeamDto> searchPageSimple(MemberSearchCond condition, Pageable pageable);

    Page<MemberTeamDto> searchPageComplex(MemberSearchCond condition, Pageable pageable);
}
