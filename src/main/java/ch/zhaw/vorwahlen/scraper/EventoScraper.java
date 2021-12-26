package ch.zhaw.vorwahlen.scraper;

import ch.zhaw.vorwahlen.model.evento.EventoData;
import ch.zhaw.vorwahlen.model.core.module.Module;
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
        var eventoData = new EventoData();
        eventoData.setModuleNo(module.getModuleNo());
        try {
            var modulePage = getWebsiteContent(url);
            var columns = modulePage.select(".DetailDialog_FormLabelCell, .DetailDialog_FormValueCell");

            if(columns.size() % 2 == 0) {
                for (var i = 1; i < columns.size(); i += COLUMNS_PER_ROW) {
                    var rowTitleElement = columns.get(i - 1);
                    var rowValueElement = columns.get(i);

                    var fieldName = rowTitleElement.text().trim();
                    var dataText = valueIsHtmlStructure(rowValueElement)
                            ? rowValueElement.html()
                            : rowValueElement.text();

                    setEventoDataField(fieldName, eventoData, dataText);
                }
            }
        } catch (IOException | NullPointerException e) {
            log.severe(e.getMessage());
        }
        if(doesEventoDataContainNull(eventoData)) {
            eventoData = new EventoData();
            eventoData.setModuleNo(module.getModuleNo());
        }
        return eventoData;
    }

    private static boolean doesEventoDataContainNull(EventoData eventoData) {
        if(eventoData == null) return true;
        if(eventoData.getShortDescription() == null) return true;
        if(eventoData.getCoordinator() == null) return true;
        if(eventoData.getLearningObjectives() == null) return true;
        if(eventoData.getModuleContents() == null) return true;
        if(eventoData.getLiterature() == null) return true;
        if(eventoData.getSuppLiterature() == null) return true;
        if(eventoData.getPrerequisites() == null) return true;
        if(eventoData.getModuleStructure() == null) return true;
        if(eventoData.getExams() == null) return true;
        if(eventoData.getRemarks() == null) return true;
        return false;
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
            default -> log.finer(String.format("Not scraping field %s", value));
        }
    }
}
