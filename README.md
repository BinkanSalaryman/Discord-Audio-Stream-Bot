# Discord Audio Stream Bot
>A simple discord audio streaming bot.

#### The way the audio flows
* Audio source
* ⇛　Configurable audio recording device     ( **CABLE output ([VB-Audio Virtual Cable](https://www.vb-audio.com/Cable/index.htm))** )
* ⇛　Discord Audio Stream Bot
* ⇛　Discord Voice Chat

![preview](https://drive.google.com/uc?export=download&id=0B6898q95NTM3aG5JU3E3YjdiSk0)

## Instructions
* Run once, and close. (It creates a fresh config.json file)
* Fill the required values in **config.json**.
* Run again. It should connect to a voice channel on your server, sending audio from your *default* recording device (your microphone).
* **Recommended**: Install [Virtual Cable](https://www.vb-audio.com/Cable/index.htm) and set "recordingDeviceName" in **config.json** to "CABLE Output (VB-Audio Virtual Cable)". Restart to apply changes.
* Stream audio into the recording device. (**CABLE Input** from your favourite audio source if you use Virtual Cable)
* **Recommended**: If you installed Virtual Cable but your favourite audio source doesn't support switching target audio device, you can use [Audio Router](https://github.com/audiorouterdev/audio-router) to replug the program to play into CABLE Input.

## Binaries
#### Downloads
>[Latest build (07.10.2017)](https://goo.gl/S3JqnG) (dd.mm.yyyy)

#### To compile
* Install Discord.Net package (**PM> Install-Package Discord.Net -version 1.0.2**)
* Add Discord.Net library files to output folder (**opus.dll**, **sodiumlib.dll**)
* Install Newtonsoft.Json package (**PM> Install-Package Newtonsoft.Json -version 10.0.2**)
* Add [BASS.NET](http://bass.radio42.com/bass_register.html) library files to output folder (no need to register) (**bass.dll**, **bass_fx.dll**, **bassnet.dll**, **Un4seen.Bass.dll**)

## If you enjoy my work
My username in discord is 敏感サラリーマン#3306

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://goo.gl/x3BXFW)
