package ch.zhaw.vorwahlen.model.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Model / Entity class for a class list entry
 */
@Entity
@Table(name = "class_list")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Student {

    @Id
    private String email;
    private String name;
    @Column(name = "class")
    private String clazz;

}
