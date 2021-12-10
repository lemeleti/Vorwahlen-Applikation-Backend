package ch.zhaw.vorwahlen.mapper;

import ch.zhaw.vorwahlen.model.dto.PageTextDTO;
import ch.zhaw.vorwahlen.model.pagetext.PageText;
import org.springframework.stereotype.Component;

/**
 * Mapping class for {@link PageText}.
 */
@Component
public class PageTextMapper implements Mapper<PageTextDTO, PageText>{

    @Override
    public PageTextDTO toDto(PageText param) {
        return new PageTextDTO(param.getId(),
                               param.getPage(),
                               param.getUserType(),
                               param.isIpText(),
                               param.getTextNumber(),
                               param.getText());
    }

    @Override
    public PageText toInstance(PageTextDTO param) {
        return PageText.builder()
                .id(param.id())
                .page(param.page())
                .userType(param.userType())
                .isIpText(param.isIpText())
                .textNumber(param.textNumber())
                .text(param.text())
                .build();
    }

}
