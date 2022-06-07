package com.dv.telegram.notion;

import com.dv.telegram.GoogleSheetBotData;
import com.dv.telegram.GoogleSheetLoader;
import com.dv.telegram.WikiBotConfig;
import com.dv.telegram.WikiBotConfigs;
import com.dv.telegram.data.CityChatData;
import com.dv.telegram.util.WikiBotUtils;
import kotlin.Unit;
import lombok.extern.log4j.Log4j2;
import notion.api.v1.NotionClient;
import notion.api.v1.model.blocks.*;
import notion.api.v1.model.pages.Page;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Log4j2
public class NotionWrapper {
    private static final String NOTION_TOKEN_ENV_NAME = "NOTION_TOKEN";

    private static final String TOGGLE_HEADER_1_TO_APPEND_TEXT = "Чаты по землям и городам Германии (Telegram, WhatsApp)";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static void main(String[] args) {
        WikiBotConfigs wikiBotConfigs = WikiBotUtils.readConfigs();

        int threadsCount = wikiBotConfigs.getConfigs().size();
        log.info("Total bot configs: {}", threadsCount);

        WikiBotConfig config = wikiBotConfigs.getConfigs().get(0);
        GoogleSheetBotData botData = GoogleSheetLoader.readGoogleSheet(config);

        List<CityChatData> cityChatsData = botData.getCityChats();
        List<NotionCityChats> cityChats = NotionCityChats.Companion.from(cityChatsData);
//        List<NotionCityChats> cityChats = getCityChats();

        log.info("{} city chats read from Google Sheet.", cityChats.size());

        String notionToken = WikiBotUtils.getEnvVariable(NOTION_TOKEN_ENV_NAME);

//        String pageId = "2b4f00e80cb94440af00e8d83b758f27"; // Помощь украинцам в Германии
//        String pageId = "24ec680a988441698efe1003a304ded1"; // Test page for Notion API
//        String pageId = "9a0effe48cf34cd49c849a9e05c61fb9"; // список чатов по городам (german-city-chats)
        String pageId = "67sadfsadfjlkfdsaj"; // incorrect page

/*
        if (true) {
            appendToggleHeadingOne(notionToken, pageId);
            return;
        }
*/

/*
        appendCityChats(
            config.getNotionToken(),
            config.getCityChatsPageId(),
            config.getCityChatsToggleHeading1Text(),
            cityChats
        );
*/

        appendCityChats(
            notionToken,
            pageId,
            TOGGLE_HEADER_1_TO_APPEND_TEXT,
            cityChats
        );
    }

    private static void appendToggleHeadingOne(String notionToken, String pageId) {
        NotionPageUtils.INSTANCE.execute(
            notionToken,
            client -> appendToggleHeadingOne(client, pageId)
        );
    }

    private static Unit appendToggleHeadingOne(NotionClient client, String pageId) {
        Page page = NotionPageUtils.INSTANCE.retrievePage(client, pageId);

        Block paragraphInHeader = NotionPageUtils.INSTANCE.createParagraph("Paragraph as heading 1 child", List.of()); // todo: when Kotlin, remove empty list

        HeadingOneBlock.Element heading1Element = new HeadingOneBlock.Element(
            NotionPageUtils.INSTANCE.createRichTextList("Heading 1 created with Notion API, hasChildren = true try"),
            null,
            List.of(paragraphInHeader) // heading with children is created as Toggle Heading
        );

        Block heading1 = new HeadingOneBlock(heading1Element);

        client.appendBlockChildren(pageId, List.of(heading1));

        return Unit.INSTANCE;
    }

    public static NotionCityChatsImportResult appendCityChats(
        String notionToken,
        String pageId,
        String toggleHeading1Text,
        List<NotionCityChats> cityChats
    ) {
        NotionOperationBlocker.INSTANCE.startOperation();

        NotionCityChatsImportResult[] result = new NotionCityChatsImportResult[1]; // hack to be effectively final from lambda

        NotionPageUtils.INSTANCE.execute(
            notionToken,
            client -> {
                NotionCityChatsImportResult importResult = appendCityChats(client, pageId, toggleHeading1Text, cityChats);
                result[0] = importResult;
                return Unit.INSTANCE;
            }
        );

        NotionOperationBlocker.INSTANCE.stopOperation();

        return result[0];
    }

    private static NotionCityChatsImportResult appendCityChats(
        NotionClient client,
        String pageId,
        String toggleHeading1Text,
        List<NotionCityChats> cityChats
    ) {
        Page page = NotionPageUtils.INSTANCE.retrievePage(client, pageId);
        String pageTitle = NotionPageUtils.INSTANCE.getPageTitle(page);

        log.info("Page with id = {} successfully retrieved.", pageId);
        log.info("Page url: {}", page.getUrl());
        log.info("Page created by: {}", page.getCreatedBy().getName());
        log.info("Page title: {}", pageTitle);

        Blocks blocks = client.retrieveBlockChildren(pageId, null, 100);
        log.info("Total blocks retrieved from the page: {}", blocks.getResults().size());

        HeadingOneBlock rootBlock = NotionPageUtils.INSTANCE.deleteToggleHeading1Content(client, blocks, toggleHeading1Text);

        // append paragraph with refresh time
        String refreshTimeText = String.format("Список чатов обновлён: %s", ZonedDateTime.now().format(dateTimeFormatter));
        ParagraphBlock refreshTimeParagraph = NotionPageUtils.INSTANCE.createParagraph(refreshTimeText, List.of()); // todo: when Kotlin, remove empty list
        client.appendBlockChildren(rootBlock.getId(), List.of(refreshTimeParagraph));

        // append paragraph with total cities count
        int totalCities = cityChats.size();
        String totalCitiesText = String.format("Всего городов: %s", totalCities);
        ParagraphBlock totalCitiesParagraph = NotionPageUtils.INSTANCE.createParagraph(totalCitiesText, List.of()); // todo: when Kotlin, remove empty list
        client.appendBlockChildren(rootBlock.getId(), List.of(totalCitiesParagraph));

        // append paragraph with total cities count
        Integer totalChats = NotionCityChats.Companion.countTotalChats(cityChats);
        String totalChatsText = String.format("Всего чатов: %s", totalChats);
        ParagraphBlock totalChatsParagraph = NotionPageUtils.INSTANCE.createParagraph(totalChatsText, List.of()); // todo: when Kotlin, remove empty list
        client.appendBlockChildren(rootBlock.getId(), List.of(totalChatsParagraph));

        // append toggles with city chats
        List<ToggleBlock> cityChatToggles = NotionPageUtils.INSTANCE.getCityChatToggles(cityChats);
        client.appendBlockChildren(rootBlock.getId(), cityChatToggles);

        log.info("{} chats for {} cities appended to Notion page {} (\"{}\"), toggle header 1 \"{}\".", totalChats, totalCities, pageId, pageTitle, toggleHeading1Text);

        return new NotionCityChatsImportResult(
            pageId,
            pageTitle,
            toggleHeading1Text,
            totalCities,
            totalChats
        );
    }

    private static List<NotionCityChats> getTestCityChats() { // get test data
        // city 1
        NotionCityChats city1 = new NotionCityChats(
            "Ansbach",
            new ArrayList<>() // todo: after Kotlin migration, do not pass this parameter
        );

        city1.addChat(
            "https://t.me/+QQ9lx56QjYU1ZjZi",
            "Ansbach/Landkreis Ansbach \uD83C\uDDE9\uD83C\uDDEA/Ukraine \uD83C\uDDFA\uD83C\uDDE6" // test with emojis
        );

        // city 2
        NotionCityChats city2 = new NotionCityChats(
            "Bottrop",
            new ArrayList<>() // todo: after Kotlin migration, do not pass this parameter
        );

        city2.addChat(
            "https://t.me/+lWmTWIFgAI9iN2Qy",
            "Помощь Украинцам \uD83C\uDDFA\uD83C\uDDE6Bottrop\uD83C\uDDE9\uD83C\uDDEA Кто знает что?"
        );

        city2.addChat(
            "https://t.me/uahelp_ruhrgebiet",
            "UA Help Ruhrgebiet"
        );

        List<NotionCityChats> cities = new ArrayList<>(List.of(city1, city2)); // make mutable
        cities.sort(Comparator.comparing(NotionCityChats::getCityName)); // sort by city name
        return cities;
    }
}
