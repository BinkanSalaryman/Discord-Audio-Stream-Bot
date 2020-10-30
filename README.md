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
* Now it should be sending audio from the selected recording device to discord. Have a look at the **Downloads>Tools** section below for a few suggestions.


## Downloads
>[Latest build (2020-10-30)](https://drive.google.com/uc?export=download&id=0B6898q95NTM3eGxoSVljMlM3ekk) (yyyy-MM-dd)

#### Tools
* >[Virtual Cable](https://www.vb-audio.com/Cable/index.htm) (A virtual audio device working as virtual audio cable - After installation, restart bot program and set the recording device in settings tab to "CABLE Output (VB-Audio Virtual Cable)". Don't forget to stream audio into the device **CABLE Input** or else you'll hear nothing)
* >[Audio Router](https://github.com/audiorouterdev/audio-router) (To replug your favourite audio source to play into CABLE Input, if it doesn't support switching audio output)


## Have a seat
If there is something to talk about - my discord username is **敏感サラリーマン#3306**.

![flag_US](https://i.imgur.com/ohuanEH.png) Hi, since I'm not a native english speaker, please bare with me if I do mindless/subconscious grammar mistakes or the like. If you are going to contact me for a problem, I expect you to have fully read & understood this README.<p>
![flag_DE](https://i.imgur.com/ZwReBfl.png) Hallo, wenn du magst können wir auf Deutsch kommunizieren. Solltest du mich wegen eines Problems anschreiben, erwarte ich aber zumindest dass du das README durchgelesen und verstanden hast<p>
![flag_JP](https://i.imgur.com/hCrtRjG.png) こんばんは。日本語はちょっとだけ身につけましたけれど、ドイツの出身ですから辛抱してね。問題について連絡しようとするまえにぜひこのREADME（説明書）を全的に読んでください。config.jsonというファイルをメモ帳で編集する必要ないです！それは過去のソフトバンの方法です。<p>

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
