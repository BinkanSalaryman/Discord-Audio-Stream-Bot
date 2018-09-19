# Discord Audio Stream Bot
>A simple discord audio streaming bot.

#### The way the audio flows
* 1. Audio source (e.g. your voice, music from file, sound from game)
* 2. Audio recording device (e.g. microphone, [Virtual Cable](https://www.vb-audio.com/Cable/index.htm))
* 3. Discord Audio Stream Bot (This Software)
* 4. Discord Voice Chat (Discord)

![preview](https://drive.google.com/uc?export=download&id=0B6898q95NTM3aG5JU3E3YjdiSk0)

## Instructions
* Run **Discord Audio Stream Bot.exe**
* **Auto:** Follow the instructions on the console window to set up your bot.
* **Manual:** Close to create a fresh **config.json** file, fill in your bot token, then run again.
* Type "@YourBotNameHere voice join" in a guild text channel to bring the bot into a voice channel. You can specify a channel by appending its name. You can type "@YourBotNameHere help" to get more details about the commands.
* Now it should be sending audio from your *default* recording device (your microphone). To change that, use Virtual Audio Cable from **Binaries>Tools** section.

## Binaries
#### Downloads
>[Latest build (2018-09-19)](https://goo.gl/S3JqnG) (yyyy-MM-dd)

#### Tools
* **Recommended:** Install [Virtual Cable](https://www.vb-audio.com/Cable/index.htm), close bot and set "recordingDeviceName" in **config.json** to "CABLE Output (VB-Audio Virtual Cable)". Don't forget to stream audio into the device **CABLE Input** or you'll hear nothing.
* **Optional:** Install [Audio Router](https://github.com/audiorouterdev/audio-router) to replug your favourite audio source to play into CABLE Input, if it doesn't support switching audio output.

#### Todo/Known issues
* If you get the error "NotSupportedException: An attempt was made to load an assembly from a network location which would have caused the assembly to be sandboxed in previous versions of the .NET Framework...", right click **Commands.dll**, go to properties>security and check the box to grant access. No worries the DLL is safe, it contains the command implementations for use in discord text chat.
* Permission sytem is not functional currently, just ignore it for the while
* Setup flow lacks recording device setting
* voice-listening feature may not work as expected or at all, it isn't important anyway (or is it?)
* After long time the bot may stop sending for unknown reason
* Losing internet connection for a while may require you to restart the bot
* Switching audio devices while in use is unhandled

#### To compile
* Install Discord.Net package (**PM> Install-Package Discord.Net**)
* Add Discord.Net library files to output folder (**opus.dll**, **sodiumlib.dll**)
* Install Newtonsoft.Json package (**PM> Install-Package Newtonsoft.Json**)
* Add [BASS.NET](http://bass.radio42.com/bass_register.html) library files to output folder (no need to register) (**bass.dll**, **bass_fx.dll**, **bassnet.dll**, **Un4seen.Bass.dll**)

## If you enjoy my work
Have a chat with me if you feel like it, my username in discord is **敏感サラリーマン#3306**.

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
