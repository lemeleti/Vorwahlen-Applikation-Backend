package ch.zhaw.vorwahlen.model.mailtemplate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Model / Entity class for a mail template.
 */
@Entity
@Table(name = "mail_templates")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String subject;
    @Column(columnDefinition = "blob")
    private String message;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailTemplate that)) return false;
        return Objects.equals(getId(), that.getId())
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getSubject(), that.getSubject())
                && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDescription(), getSubject(), getMessage());
    }

}
