package ch.zhaw.vorwahlen.model.pagetext;

/**
 * Dto for {@link ch.zhaw.vorwahlen.model.pagetext.PageText}.
 */
public record PageTextDTO(Long id,
                          String page,
                          UserType userType,
                          boolean isIpText,
                          int textNumber,
                          String text) {}
