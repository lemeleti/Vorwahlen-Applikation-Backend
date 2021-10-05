package ch.zhaw.vorwahlen.scraper;

import ch.zhaw.vorwahlen.model.modules.EventoData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class EventoScraper {

    public static final String SITE_URL =
            "https://eventoweb.zhaw.ch/Evt_Pages/Brn_ModulDetailAZ.aspx?IDAnlass=%d&IdLanguage=1";
    private static final int SITE_LOADING_TIMEOUT = 30000;

    private static final Logger LOGGER = Logger.getLogger(EventoScraper.class.getName());

    public static EventoData parseModuleByURL(String url) {
        EventoData eventoData = null;
        try {
            Document modulePage = getWebsiteContent(url);
            List<Element> elements = modulePage.select(".DetailDialog_FormLabelCell, .DetailDialog_FormValueCell");

            eventoData = new EventoData();

            for (int i = 1; i < elements.size(); i += 2) {
                Element currElement = elements.get(i);
                String dataText = currElement.text();
                if (currElement.select("table").size() > 0 && currElement.select("li").size() > 0) {
                    dataText = currElement.html();
                }

                String fieldName = elements.get(i - 1).text().trim();
                setEventoDataField(fieldName, eventoData, dataText);
            }
        } catch (IOException | NullPointerException e) {
            LOGGER.severe(e.getMessage());
        }
        return eventoData;
    }

    private static Document getWebsiteContent(String url) throws IOException {
        return Jsoup
                .connect(url)
                .timeout(SITE_LOADING_TIMEOUT)
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
            case "Unterrichtssprache" -> eventoData.setLanguage(dataText);
            case "Modulausprägung", "Modulstruktur" -> eventoData.setModuleStructure(dataText);
            case "Leistungsnachweise" -> eventoData.setExams(dataText);
            case "Bemerkungen" -> eventoData.setRemarks(dataText);
            default -> {
            }
        }
    }
}
