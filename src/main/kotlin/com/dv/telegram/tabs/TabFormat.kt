package com.dv.telegram.tabs

/**
 * Defines how to parse the config tab.
 */
enum class TabFormat {
    /**
     * * Page name
     * * Page URL
     * * Keywords
     */
    WIKI_PAGES,

    /**
     * * Chat label (name)
     * * Keywords
     * * Multiple columns — chat answers
     */
    CHATS,

    /**
     * * Answer
     * * Keywords
     */
    COMMANDS,
}
