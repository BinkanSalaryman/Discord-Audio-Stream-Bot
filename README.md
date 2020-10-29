# Discord Audio Stream Bot
>A simple discord audio streaming bot.

#### The way the audio flows
* 1. Audio source (e.g. your voice, music from file, sound from game)
* 2. Audio recording device (e.g. microphone, [Virtual Cable](https://www.vb-audio.com/Cable/index.htm))
* 3. Discord Audio Stream Bot (This Software)
* 4. Discord Voice Chat (Discord)

![preview](https://i.imgur.com/lODMwHI.png)

## Instructions
* Run the bot program using **run win64.bat** or **run win32.bat** (depending on your PC's architecture; in case one doesn't work, please try the other one!)
* In the settings tab, insert your bot token (assuming you have already created an application with a bot user [here](https://discordapp.com/developers/applications))
* In the home tab, click the big on/off button to log in to the bot user
* In the maintenance tab, invite/add the bot to a guild/server, if necessary
* Now you can enter commands - either by sending a direct message to the bot user, or @mention from within a channel of one of your guilds/servers. To get started, you can enter the command "help" for a list of available commands, and "help some_command_name_here" for specific information about a command. (Some hints: With the command "prefix" you can give it a short prefix to issue commands instead of the possibly lengthy @mention construct, and with the command "bind" you can restrict issuing of commands to one or more channels)
* Issue the command "join" to bring the bot user up in a voice channel
* In the settings tab, enable "speaking" and select a recording device (default one being your microphone, most likely)
* Now it should be sending audio from the selected recording device to discord. Have a look at the **Binaries>Tools** section below for a few suggestions.

## Binaries
#### Downloads
>[Latest build (2020-10-29)](https://drive.google.com/uc?export=download&id=0B6898q95NTM3eGxoSVljMlM3ekk) (yyyy-MM-dd)

#### Tools
* >[Virtual Cable](https://www.vb-audio.com/Cable/index.htm) (A virtual audio device working as virtual audio cable - After installation, restart bot program and set the recording device in settings tab to "CABLE Output (VB-Audio Virtual Cable)". Don't forget to stream audio into the device **CABLE Input** or else you'll hear nothing)
* >[Audio Router](https://github.com/audiorouterdev/audio-router) (To replug your favourite audio source to play into CABLE Input, if it doesn't support switching audio output)

## If you enjoy my work
Have a chat with me if you feel like it, my username in discord is **敏感サラリーマン#3306**.

![flag_US](https://i.imgur.com/ohuanEH.png) Hi, I'm not a native english speaker, so please bare with me if I do mindless/subconscious grammar mistakes or the like. If you are going to contact me for a problem, I expect you having fully read & understood this README.<p>
![flag_DE](https://i.imgur.com/ZwReBfl.png) Hallo, wenn du magst können wir auf Deutsch kommunizieren. Solltest du mich wegen eines Problems anschreiben, erwarte ich aber zumindest dass du das README durchgelesen und verstanden hast<p>
![flag_JP](https://i.imgur.com/hCrtRjG.png) こんばんは。日本語はちょっとだけ身につけましたけれど、ドイツの出身ですからよろしくお願いしますね。問題について連絡しようとするまえにぜひこのREADME（説明書）を全的に読んでください。config.jsonというファイルをメモ帳で編集する必要ないです！それは過去のソフトバンの方法です。<p>

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
