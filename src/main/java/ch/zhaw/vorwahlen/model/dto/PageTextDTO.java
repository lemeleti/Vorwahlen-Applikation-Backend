package ch.zhaw.vorwahlen.model.dto;

import ch.zhaw.vorwahlen.model.pagetext.UserType;

/**
 * Dto for {@link ch.zhaw.vorwahlen.model.pagetext.PageText}.
 */
public record PageTextDTO(Long id,
                          String page,
                          UserType userType,
                          boolean isIpText,
                          int textNumber,
                          String text) {}
