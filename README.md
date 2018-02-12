# Discord Audio Stream Bot
>A simple discord audio streaming bot.

#### The way the audio flows
* Audio source
* ⇛　Configurable audio recording device     ( **CABLE output ([VB-Audio Virtual Cable](https://www.vb-audio.com/Cable/index.htm))** )
* ⇛　Discord Audio Stream Bot
* ⇛　Discord Voice Chat

![preview](https://drive.google.com/uc?export=download&id=0B6898q95NTM3aG5JU3E3YjdiSk0)

## Instructions (Manual)
* Run once, and close to create a fresh **config.json** file.
* Fill the bot token in **config.json**.
* Run again to apply changes, then type **@YouBotNameHere voice join** to bring the bot into a voice channel.
* Now it should be sending audio from your *default* recording device (your microphone). To change that, use Virtual Audio Cable from **Tools** section.

## Instructions (Auto)
* Run once and follow the instructions on the console window to set up your bot.
* Now it should be sending audio from your *default* recording device (your microphone). To change that, use Virtual Audio Cable from **Tools** section.

## Binaries
#### Downloads
>[Latest build (02.02.2018)](https://goo.gl/S3JqnG) (dd.mm.yyyy)

#### To compile
* Install Discord.Net package (**PM> Install-Package Discord.Net -version 1.0.2**)
* Add Discord.Net library files to output folder (**opus.dll**, **sodiumlib.dll**)
* Install Newtonsoft.Json package (**PM> Install-Package Newtonsoft.Json -version 10.0.2**)
* Add [BASS.NET](http://bass.radio42.com/bass_register.html) library files to output folder (no need to register) (**bass.dll**, **bass_fx.dll**, **bassnet.dll**, **Un4seen.Bass.dll**)

## Tools
* **Recommended:** Install [Virtual Cable](https://www.vb-audio.com/Cable/index.htm), close bot and set "recordingDeviceName" in **config.json** to "CABLE Output (VB-Audio Virtual Cable)". Stream audio in the device **CABLE Input**.
* **Optional:** Install [Audio Router](https://github.com/audiorouterdev/audio-router) to replug your favourite audio source to play into CABLE Input, if it doesn't support switching target audio device.

## If you enjoy my work
My username in discord is **敏感サラリーマン#3306**, have a chat with me :)

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
