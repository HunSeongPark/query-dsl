package study.querydsll.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Hunseong on 2022/04/26
 */
@Entity
@Getter
public class Hello {

    @Id @GeneratedValue
    private Long id;
}
