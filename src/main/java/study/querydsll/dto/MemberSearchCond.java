package study.querydsll.dto;

import lombok.Getter;

/**
 * Created by Hunseong on 2022/04/27
 */
@Getter
public class MemberSearchCond {

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;

    public MemberSearchCond(String username, String teamName, Integer ageGoe, Integer ageLoe) {
        this.username = username;
        this.teamName = teamName;
        this.ageGoe = ageGoe;
        this.ageLoe = ageLoe;
    }
}
