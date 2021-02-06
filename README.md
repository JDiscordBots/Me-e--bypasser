# Mee6 (premium level-role) bypasser

This Discord Bot bypasses a premium feature of the Bot Mee6, that you can use it for free.

You can select roles, that are given to users that reach a certain Mee6-Level.

* In order to add roles, you can send a message `mb!add <Level number> <Role ID>`
* In order to remove roles, you can send a message `mb!remove <Level number>`
* In order to show all assigned roles, you can send a message `mb!show`
* In order to get the id of a role, you can send a message `mb!id <Role name>`
* In order to configure whether roles should be removed if someone reaches a higher role, you can send a message `mb!toggle`.
* Users get the roles when executing `!rank`

Do not include greater than/lower than signs (`<>`) in the commands.

Note that this does only work if the leaderboard is public.

### Ratelimits

This bot only allows one rank update per guild per minute in order to avoid API spam.

## Build the bot

### Requirements
* JDK 8 or later (You can download it [here](https://adoptopenjdk.net/))
* [Maven](https://maven.apache.org/download.cgi)

### Building
* Clone the project
* After installing JDK8+ and Maven, it is possible to create a runnable JAR using the command `mvn package`<br/>
  This creates a file called `mee-bypasser.jar` in a directory named `target`

### Running
* Obtain the file `mee-bypasser.jar` (see `Building`)
* Create a text file called `.token` and copy your bot token (from <https://discord.com/developers/applications/me>) in this file.
* Run the command `java -jar mee-bypasser.jar` in the directory with those files

### IDE Setup
* Clone the project
* Import it in your IDE of choice as a maven project
* Create a text file called `.token` in the root directory of the project and copy your bot token (from <https://discord.com/developers/applications/me>) in this file.
* Run the class `io.github.jdiscordbots.mee.bypasser.MeeBypasser`

## Public Instance

Click [here](https://discord.com/api/oauth2/authorize?client_id=644830792845099009&permissions=268520448&scope=bot) in order to invite an instance of the Bot.

We do not guarantee uptime or functionality of the bot and/or any instance of the bot in any way.

Other bots offer that feature with their own leveling system for free, you can try those too.
