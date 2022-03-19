# Telegram bot for "Помощь украинцам в Германии" wiki 

The current bot name is `dv-wiki-bot`. DV stands for "Digital Volunteers".

The Notion wiki is [here](https://bit.ly/35ZoUM3).

The Java library [TelegramBots](https://github.com/rubenlagus/TelegramBots) is used as the Telegram API wrapper. 

The useful "Getting Started" for this library &mdash; https://github.com/rubenlagus/TelegramBots/wiki/Getting-Started. 

To enable bot to answer ALL the messages (not only the ones starting with `/`), 
you have to disable its privacy, using the `/setprivacy` command in the [@BotFather](https://t.me/BotFather). 
After changing the privacy setting, you may need to remove and add the bot to the chat.

The bot runs as a simple Java `Main` class.
