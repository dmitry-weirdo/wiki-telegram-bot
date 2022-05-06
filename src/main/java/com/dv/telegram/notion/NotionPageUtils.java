package com.dv.telegram.notion;

import com.dv.telegram.exception.CommandException;
import lombok.extern.log4j.Log4j2;
import notion.api.v1.NotionClient;
import notion.api.v1.http.OkHttp4Client;
import notion.api.v1.model.blocks.*;
import notion.api.v1.model.common.RichTextLinkType;
import notion.api.v1.model.common.RichTextType;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;

@Log4j2
public final class NotionPageUtils {

    public static final int MAX_PAGE_SIZE = 100; // setting more also returns 100

    public static final String CHAT_LINK_AND_NAME_SEPARATOR = " — ";

    private static final int CONNECT_TIMEOUT_MILLISECONDS = 60_000; // 1 minute = 60 seconds = 60000 milliseconds
    private static final int WRITE_TIMEOUT_MILLISECONDS = 60_000; // 1 minute = 60 seconds = 60000 milliseconds
    private static final int READ_TIMEOUT_MILLISECONDS = 60_000; // 1 minute = 60 seconds = 60000 milliseconds

    private NotionPageUtils() {
    }

    public static void execute(String notionToken, Consumer<NotionClient> task) {
        try (NotionClient client = new NotionClient(notionToken)) {
            setHttpClient(client);

            task.accept(client);
        }
    }

    public static void setHttpClient(NotionClient client) {
        // increase timeouts since writing a lot of toggles at once can lead to connection timeout
        OkHttp4Client httpClient = new OkHttp4Client(
            CONNECT_TIMEOUT_MILLISECONDS,
            WRITE_TIMEOUT_MILLISECONDS,
            READ_TIMEOUT_MILLISECONDS
        );

        client.setHttpClient(httpClient);
    }

    public static Page retrievePage(NotionClient client, String pageId) {
        try {
            return client.retrievePage(pageId);
        }
        catch(Exception e) {
            throw new CommandException(String.format("Ошибка при получении страницы Notion с pageId = \"%s\".", pageId), e);
        }
    }

    public static String getPageTitle(Page page) {
        return page
            .getProperties()
            .get("title")
            .getTitle()
            .get(0)
            .getText()
            .getContent();
    }

    public static HeadingOneBlock deleteToggleHeading1Content(NotionClient client, Blocks blocks, String heading1Text) {
        HeadingOneBlock heading1ToAppend = NotionPageUtils.getToggleHeading1Content(blocks, heading1Text);

        int deletedBlocksCount = 0;
        Blocks blockChildren = client.retrieveBlockChildren(heading1ToAppend.getId(), null, MAX_PAGE_SIZE); // max pageSize is 100

        while (!blockChildren.getResults().isEmpty()) {
            for (Block childBlock : blockChildren.getResults()) {
                client.deleteBlock(childBlock.getId());
            }

            deletedBlocksCount += blockChildren.getResults().size();

            blockChildren = client.retrieveBlockChildren(heading1ToAppend.getId(), null, MAX_PAGE_SIZE); // max pageSize is 100
        }

        log.info("Removed {} child blocks from heading one with text \"{}\".", deletedBlocksCount, heading1Text);

        return heading1ToAppend;
    }

    public static HeadingOneBlock getToggleHeading1Content(Blocks blocks, String heading1Text) {
        List<Block> headersWithText = blocks
            .getResults()
            .stream()
            .filter(
                block ->
                    (block.getType() == BlockType.HeadingOne)
                        && headingHasText(block.asHeadingOne(), heading1Text)
            )
            .toList();

        if (headersWithText.isEmpty()) {
            throw new CommandException(String.format("Header 1 с текстом \"%s\" не найден.", heading1Text));
        }

        if (headersWithText.size() > 1) {
            throw new CommandException(String.format("Найден более чем один header 1 с текстом \"%s\". Всего заголовков: %d.", heading1Text, headersWithText.size()));
        }

        return headersWithText.get(0).asHeadingOne();
    }

    private static boolean headingHasText(HeadingOneBlock heading1, String text) {
        List<PageProperty.RichText> richTexts = heading1
            .getHeading1()
            .getRichText();

        if (richTexts.isEmpty()) {
            return false;
        }

        PageProperty.RichText.Text richText = richTexts
            .get(0)
            .getText();

        if (richText == null) {
            return false;
        }

        String content = richText.getContent();
        if (StringUtils.isBlank(content)) {
            return false;
        }

        return content.equals(text);
    }

    // RichText methods
    public static List<PageProperty.RichText> createRichTextList(String text) {
        return List.of(
            createRichText(text)
        );
    }

    public static PageProperty.RichText createRichText(String text) {
        return new PageProperty.RichText(
            RichTextType.Text,
            new PageProperty.RichText.Text(text)
        );
    }

    public static PageProperty.RichText createRichTextLink(String text, String url) {
        PageProperty.RichText.Link link = new PageProperty.RichText.Link(RichTextLinkType.Url, url);

        return new PageProperty.RichText(
            RichTextType.Text,
            new PageProperty.RichText.Text(text, link)
        );
    }

    // ParagraphBlock methods
    public static ParagraphBlock createParagraph(String text) {
        return createParagraph(text, List.of());
    }

    public static ParagraphBlock createParagraph(String text, List<? extends Block> children) {
        return new ParagraphBlock(
            new ParagraphBlock.Element(
                createRichTextList(text),
                children
            )
        );
    }

    // ToggleBlock methods
    public static List<ToggleBlock> getCityChatToggles(List<NotionCityChats> cityChats) {
        return cityChats
            .stream()
            .map(NotionPageUtils::createToggle)
            .toList();
    }

    public static ToggleBlock createToggle(NotionCityChats city) {
        List<NotionCityChat> chats = city.getChats();
        List<BulletedListItemBlock> bullets = chats
            .stream()
            .map(NotionPageUtils::createBulletWithChatLink)
            .toList();

        return createToggle(
            city.getCityName(),
            bullets
        );
    }

    public static ToggleBlock createToggle(String text) {
        return createToggle(text, List.of());
    }

    public static ToggleBlock createToggle(String text, List<? extends Block> children) {
        return new ToggleBlock(
            new ToggleBlock.Element(
                createRichTextList(text),
                children
            )
        );
    }

    // BulletedListItemBlock methods
    public static BulletedListItemBlock createBullet(String text) {
        return createBullet(text, List.of());
    }

    public static BulletedListItemBlock createBullet(String text, List<? extends Block> children) {
        return new BulletedListItemBlock(
            new BulletedListItemBlock.Element(
                createRichTextList(text),
                children
            )
        );
    }

    public static BulletedListItemBlock createBulletWithChatLink(NotionCityChat chat) {
        return createBulletWithChatLink(
            chat.getUrl(),
            chat.getName()
        );
    }

    public static BulletedListItemBlock createBulletWithChatLink(String chatLink, String chatName) {
        return new BulletedListItemBlock(
            new BulletedListItemBlock.Element(
                List.of(
                    createRichTextLink(chatLink, chatLink),
                    createRichText(String.format("%s%s", CHAT_LINK_AND_NAME_SEPARATOR, chatName))
                )
            )
        );
    }
}
