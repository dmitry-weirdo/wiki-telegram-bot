scp -i /c/_wiki-bot/ec2-wiki-bot-keypair.pem \
  ./target/wiki-telegram-bot-1.0-SNAPSHOT-jar-with-dependencies.jar \
  ec2-user@ec2-3-64-53-227.eu-central-1.compute.amazonaws.com:/home/ec2-user/wiki-bot/wiki-telegram-bot-1.0-SNAPSHOT-jar-with-dependencies.jar

scp -i /c/_wiki-bot/ec2-wiki-bot-keypair.pem \
  ./src/main/resources/log4j2-ec2.xml \
  ec2-user@ec2-3-64-53-227.eu-central-1.compute.amazonaws.com:/home/ec2-user/wiki-bot/log4j2-ec2.xml

scp -i /c/_wiki-bot/ec2-wiki-bot-keypair.pem \
  ./src/main/resources/run-ec2.sh \
  ec2-user@ec2-3-64-53-227.eu-central-1.compute.amazonaws.com:/home/ec2-user/wiki-bot/run-ec2.sh

scp -i /c/_wiki-bot/ec2-wiki-bot-keypair.pem \
  ./.ignoreme/wiki-bot-config-prod.json \
  ec2-user@ec2-3-64-53-227.eu-central-1.compute.amazonaws.com:/home/ec2-user/wiki-bot/wiki-bot-config-prod.json
