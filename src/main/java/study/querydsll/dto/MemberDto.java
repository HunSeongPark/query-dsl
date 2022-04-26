package study.querydsll.dto;

import lombok.Data;

/**
 * Created by Hunseong on 2022/04/27
 */
@Data
public class MemberDto {

    private String username;
    private int age;

    public MemberDto() {}

    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
