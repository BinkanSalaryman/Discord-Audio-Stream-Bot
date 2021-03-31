# Discord Audio Stream Bot
>A simple discord audio streaming bot.


#### Streaming to discord - "speaking"
1. Audio source (e.g. your voice, music from file, sound from game)
2. Recording device (e.g. microphone, [Virtual Cable](https://www.vb-audio.com/Cable/index.htm))
3. Discord Audio Stream Bot (this software)
4. Discord Voice Chat (discord)

![preview](https://i.imgur.com/diLmICq.png)


#### Streaming from discord - "listening"
1. Discord Voice Chat (discord)
2. Discord Audio Stream Bot (this software)
3. Output device (e.g. your speakers, [Virtual Cable](https://www.vb-audio.com/Cable/index.htm))
4. Audio target (e.g. you, audio recording software)


#### You are the host
1. Create an application on the developer portal
2. Create a bot user
3. Copy the token
4. Enable required intents

![bot application page](https://i.imgur.com/QYbZLfn.png)


## Getting started
* Create an application with a bot user [here](https://discordapp.com/developers/applications). In the bot tab, copy the bot token and enable the "SERVER MEMBERS INTENT" (required to check if a user issuing a command has sufficient permissions)
* Run the bot program using **run win64.bat** or **run win32.bat** (depending on your PC's architecture; in case one doesn't work, please try the other one!)
* In the settings tab, paste your bot token
* In the home tab, click the big on/off button to log in to the bot user
* In the maintenance tab, invite/add the bot to a guild/server, if necessary
* Now you can enter commands - either by sending a direct message to the bot user, or @mention from within a channel of one of your guilds/servers. To get started, you can enter the command "help" for a list of available commands, and "help some_command_name_here" for specific information about a command. (Some hints: With the command "prefix" you can give it a short prefix to issue commands instead of the possibly lengthy @mention construct, and with the command "bind" you can restrict issuing of commands to one or more channels)
* Issue the command "join" to bring the bot user up in a voice channel
* In the settings tab, enable "speaking" and select a recording device (default one being your microphone, most likely)
* Now it should be sending audio from the selected recording device to discord.

### macOS

#### Building

* Install homebrew if you do not already have it, then:
  * `brew install gradle`
  * `gradle build`

#### Usage

* Follow the build steps above, then:
  * `cd build/distributions`
  * `tar -xvf Discord-Audio-Stream-Bot-1.0-SNAPSHOT.tar`
  * `cd Discord-Audio-Stream-Bot-1.0-SNAPSHOT`
  * `DISCORD_AUDIO_STREAM_BOT_OPTS='-Djava.library.path="/usr/lib:../../../natives/mac/"' ./bin/Discord-Audio-Stream-Bot`

* Create a discord application w/ a bot user:
  * https://discordapp.com/developers/applications
    * In the bot tab, copy the bot token and enable the `SERVER MEMBERS INTENT`
      * This is required to check if a user issuing a command has sufficient permissions.

* In the Java Application:
  * Add token to `Settings - General - Bot Token`
  * Check `Speaking Enabled` and pick `Recording Device`
    * [Loopback](https://rogueamoeba.com/loopback/) is a very good virtual audio cable application for macOS
  * Click `Home - Status` button to connect to Discord.

* In Discord:
  * `@bot prefix set .`
  * `.bind add "my-admin-text-channel"`
  * `.help`
  * `.autojoin set "my-voice-channel"`
  * `.join`
  * `.activity playing "background music for the channel."`

## Downloads
>[Latest build (2021-03-31)](https://drive.google.com/uc?export=download&id=0B6898q95NTM3eGxoSVljMlM3ekk) (yyyy-MM-dd)

#### Tools
* >[Virtual Cable](https://www.vb-audio.com/Cable/index.htm) (A virtual audio device working as virtual audio cable - After installation, restart bot program and set the recording device in settings tab to "CABLE Output (VB-Audio Virtual Cable)". Don't forget to stream audio into the device **CABLE Input** or else you'll hear nothing)
* >[Audio Router](https://github.com/audiorouterdev/audio-router) (To replug your favourite audio source to play into CABLE Input, if it doesn't support switching audio output)


[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
