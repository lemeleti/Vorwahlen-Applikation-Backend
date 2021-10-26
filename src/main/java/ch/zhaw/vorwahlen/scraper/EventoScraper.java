package ch.zhaw.vorwahlen.scraper;

import ch.zhaw.vorwahlen.model.modules.EventoData;
import ch.zhaw.vorwahlen.model.modules.Module;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

/**
 * Scraper class to retrieve detailed data of a module on eventoweb.zhaw.ch.
 */
@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventoScraper {

    public static final String SITE_URL =
            "https://eventoweb.zhaw.ch/Evt_Pages/Brn_ModulDetailAZ.aspx?IDAnlass=%d&IdLanguage=1&date=662249088000000000";

    private static final int SITE_LOADING_TIMEOUT_MS = 30000;
    private static final int COLUMNS_PER_ROW = 2;


    /**
     * Retrieve the parsed module.
     * @param url Eventoweb module website.
     * @return {@link EventoData}
     */
    public static EventoData parseModuleByURL(String url, Module module) {
        EventoData eventoData = null;
        try {
            var modulePage = getWebsiteContent(url);
            var columns = modulePage.select(".DetailDialog_FormLabelCell, .DetailDialog_FormValueCell");

            eventoData = new EventoData();
            eventoData.setModuleNo(module.getModuleNo());
            for (var i = 1; i < columns.size(); i += COLUMNS_PER_ROW) {
                var rowTitleElement = columns.get(i - 1);
                var rowValueElement = columns.get(i);

                var fieldName = rowTitleElement.text().trim();
                var dataText = valueIsHtmlStructure(rowValueElement)
                        ? rowValueElement.html()
                        : rowValueElement.text();

                setEventoDataField(fieldName, eventoData, dataText);
            }
        } catch (IOException | NullPointerException e) {
            log.severe(e.getMessage());
        }
        return eventoData;
    }

    private static boolean valueIsHtmlStructure(Element rowValueElement) {
        return !rowValueElement.select("table").isEmpty() ||
                !rowValueElement.select("li").isEmpty();
    }

    private static Document getWebsiteContent(String url) throws IOException {
        return Jsoup
                .connect(url)
                .timeout(SITE_LOADING_TIMEOUT_MS)
                .execute()
                .parse();
    }

    private static void setEventoDataField(String value, EventoData eventoData, String dataText) {
        switch (value) {
            case "Kurzbeschrieb" -> eventoData.setShortDescription(dataText);
            case "Modulverantwortung" -> eventoData.setCoordinator(dataText);
            case "Lernziele (Kompetenzen)" -> eventoData.setLearningObjectives(dataText);
            case "Modulinhalte" -> eventoData.setModuleContents(dataText);
            case "Lehrmittel/Materialien" -> eventoData.setLiterature(dataText);
            case "Ergänzende Literatur" -> eventoData.setSuppLiterature(dataText);
            case "Zulassungs-voraussetzungen" -> eventoData.setPrerequisites(dataText);
            case "Modulausprägung", "Modulstruktur" -> eventoData.setModuleStructure(dataText);
            case "Leistungsnachweise" -> eventoData.setExams(dataText);
            case "Bemerkungen" -> eventoData.setRemarks(dataText);
            default -> log.info(String.format("Not scraping field %s", value));
        }
    }
}
