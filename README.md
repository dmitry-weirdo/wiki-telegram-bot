# Telegram bot for "Помощь украинцам в Германии" wiki 

The current bot name is `dv-wiki-bot`. DV stands for "Digital Volunteers".

The Notion wiki is [here](https://uahelp.wiki/).

The Java library [TelegramBots](https://github.com/rubenlagus/TelegramBots) is used as the Telegram API wrapper. 

The useful "Getting Started" for this library &mdash; https://github.com/rubenlagus/TelegramBots/wiki/Getting-Started. 

To enable bot to answer ALL the messages (not only the ones starting with `/`), 
you have to disable its privacy, using the `/setprivacy` command in the [@BotFather](https://t.me/BotFather). 
After changing the privacy setting, you may need to remove and add the bot to the chat.

To change the bot username in Telegram, use the `/setname` command in the [@BotFather](https://t.me/BotFather). 

The bot runs as a simple Java `Main` class.

# Run the bot from the jar with dependencies

Set the environment variable `WIKI_BOT_CONFIG_FILE_PATH`:
```bash
export WIKI_BOT_CONFIG_FILE_PATH=/path/to/your/file/wiki-bot-config.json
```

In the json config, set `environmentName` property to indicate the environment:
```json
  "environmentName": "My local super environment",
```

and `botToken` for your bot instance (test, prod, etc)
```json
  "botToken": "1234567890:AAFxrf7...",
```

Execute the java Main class from the jar file.
You can override the log4j configuration via the `log4j.configurationFile` property.

```bash
java -Dlog4j.configurationFile=./log4j2-override.xml -jar wiki-telegram-bot-1.0-SNAPSHOT-jar-with-dependencies.jar
```

# Run bot on AWS EC2 instance

## Install java 17 on AWS EC instance
https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/amazon-linux-install.html

## Connect to EC2 via Putty
https://asf.alaska.edu/how-to/data-recipes/connect-to-your-ec2-instance-using-putty-v1-1/ 

And then login under `ec2-user`.

# Run the bot in a screen session
See https://linuxize.com/post/how-to-use-linux-screen/

We have to run the java process of the bot in a separate `screen` session to not drop it after we disconnected from PuTTY.

Copy bot files to the EC2 instance using the `copy-to-ec2.sh` file.

Log in to the EC2 instance using PuTTY, username `ec2-user`.

Go to the bot directory
```bash 
cd /home/ec2-user/wiki-bot
```

Create a `screen` session for the bot:
```bash
screen -S wiki-bot
```

Make sure you've set up the environment variable:
```bash
export WIKI_BOT_CONFIG_FILE_PATH="/home/ec2-user/wiki-bot/wiki-bot-config.json"
```

Run the bot using the `run-ec2.sh` script

```bash
source ./run-ec2.sh
```

Disconnect from the current `screen` session: `Ctrl + A`, `D`.

You can watch the logs of the bot:
```bash
tail -f ./wiki-bot.log
```

Now you can disconnect from PuTTY and the bot will continue to work!

To stop the bot, connect to PuTTY again.

Get back to the `screen` session:
```bash
screen -r wiki-bot
```

And press `Ctrl + C` to stop the bot.
