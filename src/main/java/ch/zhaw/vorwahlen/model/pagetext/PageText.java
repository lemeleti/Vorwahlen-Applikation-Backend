package ch.zhaw.vorwahlen.model.pagetext;

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
 * Model / Entity class for a page text.
 */
@Entity
@Table(name = "page_texts")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageText {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String page;
    private UserType userType;
    private boolean isIpText;
    private int textNumber;
    @Column(columnDefinition = "blob")
    private String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageText that)) return false;
        return isIpText() == that.isIpText()
                && getTextNumber() == that.getTextNumber()
                && Objects.equals(getId(), that.getId())
                && Objects.equals(getPage(), that.getPage())
                && getUserType() == that.getUserType() && Objects.equals(getText(), that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPage(), getUserType(), isIpText(), getTextNumber(), getText());
    }

}
