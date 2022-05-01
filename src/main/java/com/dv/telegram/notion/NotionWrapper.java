package com.dv.telegram.notion;

import com.dv.telegram.util.WikiBotUtils;
import lombok.extern.log4j.Log4j2;
import notion.api.v1.NotionClient;
import notion.api.v1.http.OkHttp4Client;
import notion.api.v1.model.blocks.*;
import notion.api.v1.model.common.RichTextType;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;

import java.util.List;
import java.util.UUID;

@Log4j2
public class NotionWrapper {
    private static final String NOTION_TOKEN_ENV_NAME = "NOTION_TOKEN";

    public static void main(String[] args) {
        String notionToken = WikiBotUtils.getEnvVariable(NOTION_TOKEN_ENV_NAME);

        try (NotionClient client = new NotionClient(notionToken)) {
            client.setHttpClient(new OkHttp4Client());

//            String pageId = "2b4f00e80cb94440af00e8d83b758f27"; // Помощь украинцам в Германии
//            String pageId = "9a0effe48cf34cd49c849a9e05c61fb9"; // список чатов по городам
            String pageId = "24ec680a988441698efe1003a304ded1"; // Test page for Notion API

            Page page = client.retrievePage(pageId);
            String title = page
                .getProperties()
                .get("title")
                .getTitle()
                .get(0)
                .getText()
                .getContent();

            log.info("Page with id = {} successfully retrieved.", pageId);
            log.info("Page url: {}", page.getUrl());
            log.info("Page created by: {}", page.getCreatedBy().getName());
            log.info("Page title: {}", title);


            Blocks blocks = client.retrieveBlockChildren(pageId, null, 100);
            log.info("Total blocks retrieved from the page: {}", blocks.getResults().size());

            HeadingOneBlock toggleHeading = blocks.getResults().get(2).asHeadingOne();
            log.info("Toggle heading has children: {}", toggleHeading.getHasChildren());

            Block paragraphInToggleHeading = new ParagraphBlock(
                new ParagraphBlock.Element(
                    List.of(
                        new PageProperty.RichText(
                            RichTextType.Text,
                            new PageProperty.RichText.Text("Paragraph text within toggle heading 1 (try)")
                        )
                    )
                )
            );


            List<Block> blocksInToggleHeader = List.of(paragraphInToggleHeading);
            client.appendBlockChildren(toggleHeading.getId(), blocksInToggleHeader);
            log.info("Appended paragraph to toggle-heading1 with id = {}", toggleHeading.getId());

/*
            if (true) {
                return;
            }
*/

            HeadingOneBlock.Element heading1Element = new HeadingOneBlock.Element(
                List.of(
                    new PageProperty.RichText(
                        RichTextType.Text,
                        new PageProperty.RichText.Text("Heading 1 created with Notion API, hasChildren = true try")
                    )
                )
            );

            Block heading1 = new HeadingOneBlock(
                heading1Element,
                UUID.randomUUID().toString(),
                true, // will it make it a toggle header?
                null,
                null,
                null,
                null
            );

            Block paragraph = new ParagraphBlock(
                new ParagraphBlock.Element(
                    List.of(
                        new PageProperty.RichText(
                            RichTextType.Text,
                            new PageProperty.RichText.Text("Paragraph text created with Notion API")
                        )
                    )
                )
            );

            BulletedListItemBlock bullet1_1 = createBullet("Bullet 1.1 + (by API)");
            BulletedListItemBlock bullet1_2 = createBullet("Bullet 1.2 + (by API)");
            BulletedListItemBlock bullet1 = createBullet("Bullet 1 (by API)", List.of(bullet1_1, bullet1_2));

            BulletedListItemBlock bullet2 = createBullet("Bullet 2 (by API)");

            List<Block> blocksToAppend = List.of(heading1, paragraph, bullet1, bullet2);
            Blocks appendedBlocks = client.appendBlockChildren(pageId, blocksToAppend);

            int index = 0;
            HeadingOneBlock appendedHeader1 = appendedBlocks.getResults().get(index++).asHeadingOne();
            ParagraphBlock appendedParagraph = appendedBlocks.getResults().get(index++).asParagraph();
            BulletedListItemBlock appendedBullet1 = appendedBlocks.getResults().get(index++).asBulletedListItem();
            BulletedListItemBlock appendedBullet2 = appendedBlocks.getResults().get(index).asBulletedListItem();

            log.info("Appended heading1 and paragraph to page {}.", pageId);
            log.info("Heading1 id: {}.", appendedHeader1.getId());
            log.info("Paragraph id: {}.", appendedParagraph.getId());
            log.info("Bullet 1 id: {}.", appendedBullet1.getId());
            log.info("Bullet 2 id: {}.", appendedBullet2.getId());

/*
            Appending to non-block header does not work :(

            Block paragraphInHeading = new ParagraphBlock(
                new ParagraphBlock.Element(
                    List.of(
                        new PageProperty.RichText(
                            RichTextType.Text,
                            new PageProperty.RichText.Text("Paragraph text within heading 1 (try)")
                        )
                    )
                )
            );

            List<Block> blocksInHeader = List.of(paragraphInHeading);
            client.appendBlockChildren(appendedHeader1.getId(), blocksInHeader);
            log.info("Appended paragraph to heading1 with id = {}", heading1.getId());
*/
        }
    }

    private static BulletedListItemBlock createBullet(String text) {
        return createBullet(text, List.of());
    }

    private static BulletedListItemBlock createBullet(String text, List<? extends Block> children) {
        return new BulletedListItemBlock(
            new BulletedListItemBlock.Element(
                List.of(
                    new PageProperty.RichText(
                        RichTextType.Text,
                        new PageProperty.RichText.Text(text)
                    )
                ),
                children
            )
        );
    }
}
