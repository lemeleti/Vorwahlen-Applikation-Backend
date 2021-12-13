package ch.zhaw.vorwahlen.repository;

import ch.zhaw.vorwahlen.model.pagetext.PageText;
import ch.zhaw.vorwahlen.model.pagetext.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * Repository for {@link PageText}.
 */
public interface PageTextRepository extends JpaRepository<PageText, Long> {

    @Query("""
    SELECT p
    FROM PageText p
    WHERE p.page = :page
    """)
    Set<PageText> findAllByPage(String page);

    @Query("""
    SELECT p
    FROM PageText p
    WHERE p.page = :page
    AND p.userType = :userType
    """)
    Set<PageText> findAllByUserType(String page, UserType userType);

}
