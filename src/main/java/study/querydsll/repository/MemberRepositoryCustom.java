package study.querydsll.repository;

import study.querydsll.dto.MemberSearchCond;
import study.querydsll.dto.MemberTeamDto;

import java.util.List;

/**
 * Created by Hunseong on 2022/04/27
 */
public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCond condition);
}
