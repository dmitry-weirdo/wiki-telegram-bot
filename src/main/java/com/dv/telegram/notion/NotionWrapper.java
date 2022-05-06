package com.dv.telegram.notion;

import com.dv.telegram.*;
import com.dv.telegram.util.WikiBotUtils;
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

        WikiBotConfig config = wikiBotConfigs.configs.get(0);
        GoogleSheetBotData botData = GoogleSheetLoader.readGoogleSheet(config);

        List<CityChatData> cityChatsData = botData.getCityChats();
        List<NotionCityChats> cityChats = NotionCityChats.from(cityChatsData);
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

        appendCityChats(notionToken, pageId, cityChats);
    }

    private static void appendToggleHeadingOne(String notionToken, String pageId) {
        NotionPageUtils.execute(
            notionToken,
            client -> appendToggleHeadingOne(client, pageId)
        );
    }

    private static void appendToggleHeadingOne(NotionClient client, String pageId) {
        Page page = NotionPageUtils.retrievePage(client, pageId);

        Block paragraphInHeader = NotionPageUtils.createParagraph("Paragraph as heading 1 child");

        HeadingOneBlock.Element heading1Element = new HeadingOneBlock.Element(
            NotionPageUtils.createRichTextList("Heading 1 created with Notion API, hasChildren = true try"),
            null,
            List.of(paragraphInHeader) // heading with children is created as Toggle Heading
        );

        Block heading1 = new HeadingOneBlock(heading1Element);

        client.appendBlockChildren(pageId, List.of(heading1));
    }

    public static void appendCityChats(String notionToken, String pageId, List<NotionCityChats> cityChats) {
        NotionPageUtils.execute(
            notionToken,
            client -> appendCityChats(client, pageId, cityChats)
        );
    }

    public static void appendCityChats(NotionClient client, String pageId, List<NotionCityChats> cityChats) {
        Page page = NotionPageUtils.retrievePage(client, pageId);
        String pageTitle = NotionPageUtils.getPageTitle(page);

        log.info("Page with id = {} successfully retrieved.", pageId);
        log.info("Page url: {}", page.getUrl());
        log.info("Page created by: {}", page.getCreatedBy().getName());
        log.info("Page title: {}", pageTitle);

        Blocks blocks = client.retrieveBlockChildren(pageId, null, 100);
        log.info("Total blocks retrieved from the page: {}", blocks.getResults().size());

        HeadingOneBlock rootBlock = NotionPageUtils.deleteToggleHeading1Content(client, blocks, TOGGLE_HEADER_1_TO_APPEND_TEXT);

        // append paragraph with refresh time
        String refreshTimeText = String.format("Список чатов обновлён: %s", ZonedDateTime.now().format(dateTimeFormatter));
        ParagraphBlock refreshTimeParagraph = NotionPageUtils.createParagraph(refreshTimeText);
        client.appendBlockChildren(rootBlock.getId(), List.of(refreshTimeParagraph));

        // append paragraph with total cities count
        String totalCitiesText = String.format("Всего городов: %s", cityChats.size());
        ParagraphBlock totalCitiesParagraph = NotionPageUtils.createParagraph(totalCitiesText);
        client.appendBlockChildren(rootBlock.getId(), List.of(totalCitiesParagraph));

        // append paragraph with total cities count
        Integer totalChats = NotionCityChats.countTotalChats(cityChats);

        String totalChatsText = String.format("Всего чатов: %s", totalChats);
        ParagraphBlock totalChatsParagraph = NotionPageUtils.createParagraph(totalChatsText);
        client.appendBlockChildren(rootBlock.getId(), List.of(totalChatsParagraph));

        // append toggles with city chats
        List<ToggleBlock> cityChatToggles = NotionPageUtils.getCityChatToggles(cityChats);
        client.appendBlockChildren(rootBlock.getId(), cityChatToggles);

        log.info("{} chats for {} cities appended to page {} (\"{}\").", totalChats, cityChats.size(), pageId, pageTitle);
    }

    private static List<NotionCityChats> getTestCityChats() { // get test data
        // city 1
        NotionCityChats city1 = new NotionCityChats();
        city1.setCityName("Ansbach");
        city1.addChat(
            "https://t.me/+QQ9lx56QjYU1ZjZi",
            "Ansbach/Landkreis Ansbach \uD83C\uDDE9\uD83C\uDDEA/Ukraine \uD83C\uDDFA\uD83C\uDDE6" // test with emojis
        );

        // city 2
        NotionCityChats city2 = new NotionCityChats();
        city2.setCityName("Bottrop");

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
