<p align="center"><img src="http://i.imgur.com/nDvcxDa.jpg" alt="NoSwear Logo"></p>

[![travis-ci](https://travis-ci.org/A5H73Y/NoSwear.svg?branch=master)](https://travis-ci.org/A5H73Y/NoSwear/branches)
[![tutorials](https://img.shields.io/badge/tutorials-github-brightgreen.svg)](https://a5h73y.github.io/NoSwear/)
[![bStats](https://img.shields.io/badge/statistics-bstats-brightgreen.svg)](https://bstats.org/plugin/bukkit/NoSwear)
[![license: MIT](https://img.shields.io/badge/license-MIT-lightgrey.svg)](https://tldrlegal.com/license/mit-license)
[![repo](https://api.bintray.com/packages/a5h73y/repo/NoSwear/images/download.svg)](https://bintray.com/a5h73y/repo/NoSwear/_latestVersion)

NoSwear is the ultimate chat control plugin. Allowing you to prevent: unwanted swear words, spam, advertising / links being posted.<p />
First released in April 2012, and has been updated since. NoSwear is now open source, allowing you to contribute ideas and enhancements, or create your own spin on the plugin.<p />

[<img src="https://i.imgur.com/VoFdY6y.png" alt="Discord Support">](https://discord.gg/f5pzDzu)<p />

## Installation
* Install [Spigot](https://www.spigotmc.org/threads/buildtools-updates-information.42865/) _(v1.8 to 1.14)_
* Download NoSwear from [dev.bukkit.org/projects/a5h73y/files](https://dev.bukkit.org/projects/a5h73y/files)
* Place the _NoSwear.jar_ into the _/plugins_ folder of the server.
* Start your server and check the server logs to ensure the plugin started successfully.
* Check the _config.yml_ and configure it to your preference before fully implementing the plugin.

## Supported plugins
| Plugin        | Description  |
| ------------- | ------------- |
| [Vault](https://dev.bukkit.org/projects/vault) | Add economy support to the plugin, reward or penalise the player. <br>[GitHub Project by MilkBowl](https://github.com/MilkBowl/Vault) |

## Maven
```
<repository>
    <id>a5h73y-repo</id>
    <url>https://dl.bintray.com/a5h73y/repo/</url>
</repository>
```

```
<dependency>
    <groupId>me.A5H73Y</groupId>
    <artifactId>NoSwear</artifactId>
    <version>7.4</version>
    <type>jar</type>
    <scope>provided</scope>
</dependency>
```

## Gradle
```
repositories { 
    maven { 
        url "https://dl.bintray.com/a5h73y/repo"
    } 
}
```

```
compile 'me.A5H73Y:NoSwear:7.4'
```
