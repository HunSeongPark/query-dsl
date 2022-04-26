package study.querydsll.dto;

import lombok.Data;

/**
 * Created by Hunseong on 2022/04/27
 */
@Data
public class MemberDto {

    private String name;
    private int age;

    public MemberDto() {}

    public MemberDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
