# Discord Audio Stream Bot
>A simple discord audio streaming bot.

#### Preview: Streaming to discord ("speaking")
![preview](https://i.imgur.com/diLmICq.png)
1. Audio source (e.g. your voice, music from file, sound from game)
2. Recording device (e.g. microphone, [Virtual Cable](https://www.vb-audio.com/Cable/index.htm))
3. Discord Audio Stream Bot (this software)
4. Discord Voice Chat (discord)


## Getting started
#### Part 1 - Bot User
* Create a discord developer application [here](https://discordapp.com/developers/applications)
* In the bot tab, add a bot user
* Copy the bot token
* Enable the "SERVER MEMBERS INTENT" (required to check if a user issuing a command has sufficient permissions)
#### Part 2 - Bot Program
* Check that a Java Runtime Environment (JRE) is installed and is added to the PATH system environment variable
* Run the bot program using **run win64.bat** or **run win32.bat** (depending on your PC's architecture; in case one doesn't work, please try the other one!). **Note: This step is only valid for Windows.** *If you intend to run it on mac or linux, I would appreciate your help in making launch scripts or putting explanations in this document!*
* In the settings tab, paste your bot token and hit return
* In the home tab, click the big on/off button to log in to the bot user with the token
* In the maintenance tab, invite/add the bot to a guild/server, if necessary
#### Part 3 - Command Introduction
* Now you can enter commands - either by sending a direct message to the bot user, or with @mention as prefix from within a channel of a guild/server shared by you and the bot user
* Enter the command "help" for a complete list of available commands
* Enter "help <command_name_in_question>" for details about a specific command and especially its arguments.
* If an argument contains a whitespace character (` `), you need to surround it with quotation marks (`"`). You need to escape quotation marks and backslashes (`\`) with a backslash. For example, `@bot activity playing "\"Favourite\" Music\\"` would set the bot user's activity to `"Favourite" Music\`. **Note: I will omit command arguments from now on**
* Use the "prefix" command to avoid the possibly lengthy @mention prefix
* Use "bind" to restrict command usage to one or more channels
* Use "autojoin" or "follow-voice" to automate *join*ing voice channels
#### Part 4 - Test run
* Issue the command "join" to bring the bot user up in a voice channel
* In the settings tab of the bot program, unmute the bot
* Choose & select a recording device (default one being your microphone, most likely)
* Now it should be sending audio from the selected recording device to discord. Enjoy!


### macOS (credits to spkane)

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

* >[Loopback](https://rogueamoeba.com/loopback/) is a very good virtual audio cable application for macOS


## Downloads
>[Latest build (2021-07-03)](https://drive.google.com/uc?export=download&id=0B6898q95NTM3eGxoSVljMlM3ekk) (yyyy-MM-dd)

#### Tools
* >[Virtual Cable](https://www.vb-audio.com/Cable/index.htm) (A virtual audio device working as virtual audio cable - After installation, restart bot program and set the recording device in settings tab to "CABLE Output (VB-Audio Virtual Cable)". Don't forget to stream audio into the device **CABLE Input** or else you'll hear nothing)
* >[Audio Router](https://github.com/audiorouterdev/audio-router) (To replug your favourite audio source to play into CABLE Input, if it doesn't support switching audio output)

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
