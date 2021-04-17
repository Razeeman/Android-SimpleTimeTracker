/*
 * UNICODE, INC. LICENSE AGREEMENT - DATA FILES AND SOFTWARE
 * See Terms of Use for definitions of Unicode Inc.'s
 * Data Files and Software.
 *
 * NOTICE TO USER: Carefully read the following legal agreement.
 * BY DOWNLOADING, INSTALLING, COPYING OR OTHERWISE USING UNICODE INC.'S
 * DATA FILES ("DATA FILES"), AND/OR SOFTWARE ("SOFTWARE"),
 * YOU UNEQUIVOCALLY ACCEPT, AND AGREE TO BE BOUND BY, ALL OF THE
 * TERMS AND CONDITIONS OF THIS AGREEMENT.
 * IF YOU DO NOT AGREE, DO NOT DOWNLOAD, INSTALL, COPY, DISTRIBUTE OR USE
 * THE DATA FILES OR SOFTWARE.
 *
 * COPYRIGHT AND PERMISSION NOTICE
 *
 * Copyright © 1991-2021 Unicode, Inc. All rights reserved.
 * Distributed under the Terms of Use in https://www.unicode.org/copyright.html.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of the Unicode data files and any associated documentation
 * (the "Data Files") or Unicode software and any associated documentation
 * (the "Software") to deal in the Data Files or Software
 * without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, and/or sell copies of
 * the Data Files or Software, and to permit persons to whom the Data Files
 * or Software are furnished to do so, provided that either
 * (a) this copyright and permission notice appear with all copies
 * of the Data Files or Software, or
 * (b) this copyright and permission notice appear in associated
 * Documentation.
 *
 * THE DATA FILES AND SOFTWARE ARE PROVIDED "AS IS", WITHOUT WARRANTY OF
 * ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT OF THIRD PARTY RIGHTS.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS
 * NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL
 * DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
 * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE DATA FILES OR SOFTWARE.
 *
 * Except as contained in this notice, the name of a copyright holder
 * shall not be used in advertising or otherwise to promote the sale,
 * use or other dealings in these Data Files or Software without prior
 * written authorization of the copyright holder.
 */

package com.example.util.simpletimetracker.core.repo

import javax.inject.Inject

class EmojiRepo @Inject constructor() {

    // group: Smileys & Emotion
    fun getGroupSmileys(): List<List<Int>> = listOf(
        // subgroup: face-smiling
        listOf(0x1F600),                                          // 😀 E1.0 grinning face
        listOf(0x1F603),                                          // 😃 E0.6 grinning face with big eyes
        listOf(0x1F604),                                          // 😄 E0.6 grinning face with smiling eyes
        listOf(0x1F601),                                          // 😁 E0.6 beaming face with smiling eyes
        listOf(0x1F606),                                          // 😆 E0.6 grinning squinting face
        listOf(0x1F605),                                          // 😅 E0.6 grinning face with sweat
        listOf(0x1F923),                                          // 🤣 E3.0 rolling on the floor laughing
        listOf(0x1F602),                                          // 😂 E0.6 face with tears of joy
        listOf(0x1F642),                                          // 🙂 E1.0 slightly smiling face
        listOf(0x1F643),                                          // 🙃 E1.0 upside-down face
        listOf(0x1F609),                                          // 😉 E0.6 winking face
        listOf(0x1F60A),                                          // 😊 E0.6 smiling face with smiling eyes
        listOf(0x1F607),                                          // 😇 E1.0 smiling face with halo

        // subgroup: face-affection
        listOf(0x1F970),                                          // 🥰 E11.0 smiling face with hearts
        listOf(0x1F60D),                                          // 😍 E0.6 smiling face with heart-eyes
        listOf(0x1F929),                                          // 🤩 E5.0 star-struck
        listOf(0x1F618),                                          // 😘 E0.6 face blowing a kiss
        listOf(0x1F617),                                          // 😗 E1.0 kissing face
        listOf(0x263A, 0xFE0F),                                   // ☺️ E0.6 smiling face
        listOf(0x1F61A),                                          // 😚 E0.6 kissing face with closed eyes
        listOf(0x1F619),                                          // 😙 E1.0 kissing face with smiling eyes
        listOf(0x1F972),                                          // 🥲 E13.0 smiling face with tear

        // subgroup: face-tongue
        listOf(0x1F60B),                                          // 😋 E0.6 face savoring food
        listOf(0x1F61B),                                          // 😛 E1.0 face with tongue
        listOf(0x1F61C),                                          // 😜 E0.6 winking face with tongue
        listOf(0x1F92A),                                          // 🤪 E5.0 zany face
        listOf(0x1F61D),                                          // 😝 E0.6 squinting face with tongue
        listOf(0x1F911),                                          // 🤑 E1.0 money-mouth face

        // subgroup: face-hand
        listOf(0x1F917),                                          // 🤗 E1.0 hugging face
        listOf(0x1F92D),                                          // 🤭 E5.0 face with hand over mouth
        listOf(0x1F92B),                                          // 🤫 E5.0 shushing face
        listOf(0x1F914),                                          // 🤔 E1.0 thinking face

        // subgroup: face-neutral-skeptical
        listOf(0x1F910),                                          // 🤐 E1.0 zipper-mouth face
        listOf(0x1F928),                                          // 🤨 E5.0 face with raised eyebrow
        listOf(0x1F610),                                          // 😐 E0.7 neutral face
        listOf(0x1F611),                                          // 😑 E1.0 expressionless face
        listOf(0x1F636),                                          // 😶 E1.0 face without mouth
        listOf(0x1F636, 0x200D, 0x1F32B, 0xFE0F),                 // 😶‍🌫️ E13.1 face in clouds
        listOf(0x1F60F),                                          // 😏 E0.6 smirking face
        listOf(0x1F612),                                          // 😒 E0.6 unamused face
        listOf(0x1F644),                                          // 🙄 E1.0 face with rolling eyes
        listOf(0x1F62C),                                          // 😬 E1.0 grimacing face
        listOf(0x1F62E, 0x200D, 0x1F4A8),                         // 😮‍💨 E13.1 face exhaling
        listOf(0x1F925),                                          // 🤥 E3.0 lying face

        // subgroup: face-sleepy
        listOf(0x1F60C),                                          // 😌 E0.6 relieved face
        listOf(0x1F614),                                          // 😔 E0.6 pensive face
        listOf(0x1F62A),                                          // 😪 E0.6 sleepy face
        listOf(0x1F924),                                          // 🤤 E3.0 drooling face
        listOf(0x1F634),                                          // 😴 E1.0 sleeping face

        // subgroup: face-unwell
        listOf(0x1F637),                                          // 😷 E0.6 face with medical mask
        listOf(0x1F912),                                          // 🤒 E1.0 face with thermometer
        listOf(0x1F915),                                          // 🤕 E1.0 face with head-bandage
        listOf(0x1F922),                                          // 🤢 E3.0 nauseated face
        listOf(0x1F92E),                                          // 🤮 E5.0 face vomiting
        listOf(0x1F927),                                          // 🤧 E3.0 sneezing face
        listOf(0x1F975),                                          // 🥵 E11.0 hot face
        listOf(0x1F976),                                          // 🥶 E11.0 cold face
        listOf(0x1F974),                                          // 🥴 E11.0 woozy face
        listOf(0x1F635),                                          // 😵 E0.6 knocked-out face
        listOf(0x1F635, 0x200D, 0x1F4AB),                         // 😵‍💫 E13.1 face with spiral eyes
        listOf(0x1F92F),                                          // 🤯 E5.0 exploding head

        // subgroup: face-hat
        listOf(0x1F920),                                          // 🤠 E3.0 cowboy hat face
        listOf(0x1F973),                                          // 🥳 E11.0 partying face
        listOf(0x1F978),                                          // 🥸 E13.0 disguised face

        // subgroup: face-glasses
        listOf(0x1F60E),                                          // 😎 E1.0 smiling face with sunglasses
        listOf(0x1F913),                                          // 🤓 E1.0 nerd face
        listOf(0x1F9D0),                                          // 🧐 E5.0 face with monocle

        // subgroup: face-concerned
        listOf(0x1F615),                                          // 😕 E1.0 confused face
        listOf(0x1F61F),                                          // 😟 E1.0 worried face
        listOf(0x1F641),                                          // 🙁 E1.0 slightly frowning face
        listOf(0x2639, 0xFE0F),                                   // ☹️ E0.7 frowning face
        listOf(0x1F62E),                                          // 😮 E1.0 face with open mouth
        listOf(0x1F62F),                                          // 😯 E1.0 hushed face
        listOf(0x1F632),                                          // 😲 E0.6 astonished face
        listOf(0x1F633),                                          // 😳 E0.6 flushed face
        listOf(0x1F97A),                                          // 🥺 E11.0 pleading face
        listOf(0x1F626),                                          // 😦 E1.0 frowning face with open mouth
        listOf(0x1F627),                                          // 😧 E1.0 anguished face
        listOf(0x1F628),                                          // 😨 E0.6 fearful face
        listOf(0x1F630),                                          // 😰 E0.6 anxious face with sweat
        listOf(0x1F625),                                          // 😥 E0.6 sad but relieved face
        listOf(0x1F622),                                          // 😢 E0.6 crying face
        listOf(0x1F62D),                                          // 😭 E0.6 loudly crying face
        listOf(0x1F631),                                          // 😱 E0.6 face screaming in fear
        listOf(0x1F616),                                          // 😖 E0.6 confounded face
        listOf(0x1F623),                                          // 😣 E0.6 persevering face
        listOf(0x1F61E),                                          // 😞 E0.6 disappointed face
        listOf(0x1F613),                                          // 😓 E0.6 downcast face with sweat
        listOf(0x1F629),                                          // 😩 E0.6 weary face
        listOf(0x1F62B),                                          // 😫 E0.6 tired face
        listOf(0x1F971),                                          // 🥱 E12.0 yawning face

        // subgroup: face-negative
        listOf(0x1F624),                                          // 😤 E0.6 face with steam from nose
        listOf(0x1F621),                                          // 😡 E0.6 pouting face
        listOf(0x1F620),                                          // 😠 E0.6 angry face
        listOf(0x1F92C),                                          // 🤬 E5.0 face with symbols on mouth
        listOf(0x1F608),                                          // 😈 E1.0 smiling face with horns
        listOf(0x1F47F),                                          // 👿 E0.6 angry face with horns
        listOf(0x1F480),                                          // 💀 E0.6 skull
        listOf(0x2620, 0xFE0F),                                   // ☠️ E1.0 skull and crossbones

        // subgroup: face-costume
        listOf(0x1F4A9),                                          // 💩 E0.6 pile of poo
        listOf(0x1F921),                                          // 🤡 E3.0 clown face
        listOf(0x1F479),                                          // 👹 E0.6 ogre
        listOf(0x1F47A),                                          // 👺 E0.6 goblin
        listOf(0x1F47B),                                          // 👻 E0.6 ghost
        listOf(0x1F47D),                                          // 👽 E0.6 alien
        listOf(0x1F47E),                                          // 👾 E0.6 alien monster
        listOf(0x1F916),                                          // 🤖 E1.0 robot

        // subgroup: cat-face
        listOf(0x1F63A),                                          // 😺 E0.6 grinning cat
        listOf(0x1F638),                                          // 😸 E0.6 grinning cat with smiling eyes
        listOf(0x1F639),                                          // 😹 E0.6 cat with tears of joy
        listOf(0x1F63B),                                          // 😻 E0.6 smiling cat with heart-eyes
        listOf(0x1F63C),                                          // 😼 E0.6 cat with wry smile
        listOf(0x1F63D),                                          // 😽 E0.6 kissing cat
        listOf(0x1F640),                                          // 🙀 E0.6 weary cat
        listOf(0x1F63F),                                          // 😿 E0.6 crying cat
        listOf(0x1F63E),                                          // 😾 E0.6 pouting cat

        // subgroup: monkey-face
        listOf(0x1F648),                                          // 🙈 E0.6 see-no-evil monkey
        listOf(0x1F649),                                          // 🙉 E0.6 hear-no-evil monkey
        listOf(0x1F64A),                                          // 🙊 E0.6 speak-no-evil monkey

        // subgroup: emotion
        listOf(0x1F48B),                                          // 💋 E0.6 kiss mark
        listOf(0x1F48C),                                          // 💌 E0.6 love letter
        listOf(0x1F498),                                          // 💘 E0.6 heart with arrow
        listOf(0x1F49D),                                          // 💝 E0.6 heart with ribbon
        listOf(0x1F496),                                          // 💖 E0.6 sparkling heart
        listOf(0x1F497),                                          // 💗 E0.6 growing heart
        listOf(0x1F493),                                          // 💓 E0.6 beating heart
        listOf(0x1F49E),                                          // 💞 E0.6 revolving hearts
        listOf(0x1F495),                                          // 💕 E0.6 two hearts
        listOf(0x1F49F),                                          // 💟 E0.6 heart decoration
        listOf(0x2763, 0xFE0F),                                   // ❣️ E1.0 heart exclamation
        listOf(0x1F494),                                          // 💔 E0.6 broken heart
        listOf(0x2764, 0xFE0F, 0x200D, 0x1F525),                  // ❤️‍🔥 E13.1 heart on fire
        listOf(0x2764, 0xFE0F, 0x200D, 0x1FA79),                  // ❤️‍🩹 E13.1 mending heart
        listOf(0x2764, 0xFE0F),                                   // ❤️ E0.6 red heart
        listOf(0x1F9E1),                                          // 🧡 E5.0 orange heart
        listOf(0x1F49B),                                          // 💛 E0.6 yellow heart
        listOf(0x1F49A),                                          // 💚 E0.6 green heart
        listOf(0x1F499),                                          // 💙 E0.6 blue heart
        listOf(0x1F49C),                                          // 💜 E0.6 purple heart
        listOf(0x1F90E),                                          // 🤎 E12.0 brown heart
        listOf(0x1F5A4),                                          // 🖤 E3.0 black heart
        listOf(0x1F90D),                                          // 🤍 E12.0 white heart
        listOf(0x1F4AF),                                          // 💯 E0.6 hundred points
        listOf(0x1F4A2),                                          // 💢 E0.6 anger symbol
        listOf(0x1F4A5),                                          // 💥 E0.6 collision
        listOf(0x1F4AB),                                          // 💫 E0.6 dizzy
        listOf(0x1F4A6),                                          // 💦 E0.6 sweat droplets
        listOf(0x1F4A8),                                          // 💨 E0.6 dashing away
        listOf(0x1F573, 0xFE0F),                                  // 🕳️ E0.7 hole
        listOf(0x1F4A3),                                          // 💣 E0.6 bomb
        listOf(0x1F4AC),                                          // 💬 E0.6 speech balloon
        listOf(0x1F441, 0xFE0F, 0x200D, 0x1F5E8, 0xFE0F),         // 👁️‍🗨️ E2.0 eye in speech bubble
        listOf(0x1F5E8, 0xFE0F),                                  // 🗨️ E2.0 left speech bubble
        listOf(0x1F5EF, 0xFE0F),                                  // 🗯️ E0.7 right anger bubble
        listOf(0x1F4AD),                                          // 💭 E1.0 thought balloon
        listOf(0x1F4A4)                                           // 💤 E0.6 zzz
    )

    // group: People & Body
    fun getGroupPeople(): List<List<Int>> = listOf(
        // subgroup: hand-fingers-open
        listOf(0x1F44B, SKIN_TONE),                               // 👋 E0.6 waving hand
        listOf(0x1F91A, SKIN_TONE),                               // 🤚 E3.0 raised back of hand
        listOf(0x1F590, SKIN_TONE),                               // 🖐 E0.7 hand with fingers splayed
        listOf(0x270B, SKIN_TONE),                                // ✋ E0.6 raised hand
        listOf(0x1F596, SKIN_TONE)                                // 🖖 E1.0 vulcan salute
        // TODO add the rest
    )

    // group: Animals & Nature
    fun getGroupAnimals(): List<List<Int>> = listOf(
        // subgroup: animal-mammal
        listOf(0x1F435),                                          // 🐵 E0.6 monkey face
        listOf(0x1F412),                                          // 🐒 E0.6 monkey
        listOf(0x1F98D),                                          // 🦍 E3.0 gorilla
        listOf(0x1F9A7),                                          // 🦧 E12.0 orangutan
        listOf(0x1F436),                                          // 🐶 E0.6 dog face
        listOf(0x1F415),                                          // 🐕 E0.7 dog
        listOf(0x1F9AE),                                          // 🦮 E12.0 guide dog
        listOf(0x1F415, 0x200D, 0x1F9BA),                         // 🐕‍🦺 E12.0 service dog
        listOf(0x1F429),                                          // 🐩 E0.6 poodle
        listOf(0x1F43A),                                          // 🐺 E0.6 wolf
        listOf(0x1F98A),                                          // 🦊 E3.0 fox
        listOf(0x1F99D),                                          // 🦝 E11.0 raccoon
        listOf(0x1F431),                                          // 🐱 E0.6 cat face
        listOf(0x1F408),                                          // 🐈 E0.7 cat
        listOf(0x1F408, 0x200D, 0x2B1B),                          // 🐈‍⬛ E13.0 black cat
        listOf(0x1F981),                                          // 🦁 E1.0 lion
        listOf(0x1F42F),                                          // 🐯 E0.6 tiger face
        listOf(0x1F405),                                          // 🐅 E1.0 tiger
        listOf(0x1F406),                                          // 🐆 E1.0 leopard
        listOf(0x1F434),                                          // 🐴 E0.6 horse face
        listOf(0x1F40E),                                          // 🐎 E0.6 horse
        listOf(0x1F984),                                          // 🦄 E1.0 unicorn
        listOf(0x1F993),                                          // 🦓 E5.0 zebra
        listOf(0x1F98C),                                          // 🦌 E3.0 deer
        listOf(0x1F9AC),                                          // 🦬 E13.0 bison
        listOf(0x1F42E),                                          // 🐮 E0.6 cow face
        listOf(0x1F402),                                          // 🐂 E1.0 ox
        listOf(0x1F403),                                          // 🐃 E1.0 water buffalo
        listOf(0x1F404),                                          // 🐄 E1.0 cow
        listOf(0x1F437),                                          // 🐷 E0.6 pig face
        listOf(0x1F416),                                          // 🐖 E1.0 pig
        listOf(0x1F417),                                          // 🐗 E0.6 boar
        listOf(0x1F43D),                                          // 🐽 E0.6 pig nose
        listOf(0x1F40F),                                          // 🐏 E1.0 ram
        listOf(0x1F411),                                          // 🐑 E0.6 ewe
        listOf(0x1F410),                                          // 🐐 E1.0 goat
        listOf(0x1F42A),                                          // 🐪 E1.0 camel
        listOf(0x1F42B),                                          // 🐫 E0.6 two-hump camel
        listOf(0x1F999),                                          // 🦙 E11.0 llama
        listOf(0x1F992),                                          // 🦒 E5.0 giraffe
        listOf(0x1F418),                                          // 🐘 E0.6 elephant
        listOf(0x1F9A3),                                          // 🦣 E13.0 mammoth
        listOf(0x1F98F),                                          // 🦏 E3.0 rhinoceros
        listOf(0x1F99B),                                          // 🦛 E11.0 hippopotamus
        listOf(0x1F42D),                                          // 🐭 E0.6 mouse face
        listOf(0x1F401),                                          // 🐁 E1.0 mouse
        listOf(0x1F400),                                          // 🐀 E1.0 rat
        listOf(0x1F439),                                          // 🐹 E0.6 hamster
        listOf(0x1F430),                                          // 🐰 E0.6 rabbit face
        listOf(0x1F407),                                          // 🐇 E1.0 rabbit
        listOf(0x1F43F, 0xFE0F),                                  // 🐿️ E0.7 chipmunk
        listOf(0x1F9AB),                                          // 🦫 E13.0 beaver
        listOf(0x1F994),                                          // 🦔 E5.0 hedgehog
        listOf(0x1F987),                                          // 🦇 E3.0 bat
        listOf(0x1F43B),                                          // 🐻 E0.6 bear
        listOf(0x1F43B, 0x200D, 0x2744, 0xFE0F),                  // 🐻‍❄️ E13.0 polar bear
        listOf(0x1F428),                                          // 🐨 E0.6 koala
        listOf(0x1F43C),                                          // 🐼 E0.6 panda
        listOf(0x1F9A5),                                          // 🦥 E12.0 sloth
        listOf(0x1F9A6),                                          // 🦦 E12.0 otter
        listOf(0x1F9A8),                                          // 🦨 E12.0 skunk
        listOf(0x1F998),                                          // 🦘 E11.0 kangaroo
        listOf(0x1F9A1),                                          // 🦡 E11.0 badger
        listOf(0x1F43E),                                          // 🐾 E0.6 paw prints

        // subgroup: animal-bird
        listOf(0x1F983),                                          // 🦃 E1.0 turkey
        listOf(0x1F414),                                          // 🐔 E0.6 chicken
        listOf(0x1F413),                                          // 🐓 E1.0 rooster
        listOf(0x1F423),                                          // 🐣 E0.6 hatching chick
        listOf(0x1F424),                                          // 🐤 E0.6 baby chick
        listOf(0x1F425),                                          // 🐥 E0.6 front-facing baby chick
        listOf(0x1F426),                                          // 🐦 E0.6 bird
        listOf(0x1F427),                                          // 🐧 E0.6 penguin
        listOf(0x1F54A, 0xFE0F),                                  // 🕊️ E0.7 dove
        listOf(0x1F985),                                          // 🦅 E3.0 eagle
        listOf(0x1F986),                                          // 🦆 E3.0 duck
        listOf(0x1F9A2),                                          // 🦢 E11.0 swan
        listOf(0x1F989),                                          // 🦉 E3.0 owl
        listOf(0x1F9A4),                                          // 🦤 E13.0 dodo
        listOf(0x1FAB6),                                          // 🪶 E13.0 feather
        listOf(0x1F9A9),                                          // 🦩 E12.0 flamingo
        listOf(0x1F99A),                                          // 🦚 E11.0 peacock
        listOf(0x1F99C),                                          // 🦜 E11.0 parrot

        // subgroup: animal-amphibian
        listOf(0x1F438),                                          // 🐸 E0.6 frog

        // subgroup: animal-reptile
        listOf(0x1F40A),                                          // 🐊 E1.0 crocodile
        listOf(0x1F422),                                          // 🐢 E0.6 turtle
        listOf(0x1F98E),                                          // 🦎 E3.0 lizard
        listOf(0x1F40D),                                          // 🐍 E0.6 snake
        listOf(0x1F432),                                          // 🐲 E0.6 dragon face
        listOf(0x1F409),                                          // 🐉 E1.0 dragon
        listOf(0x1F995),                                          // 🦕 E5.0 sauropod
        listOf(0x1F996),                                          // 🦖 E5.0 T-Rex

        // subgroup: animal-marine
        listOf(0x1F433),                                          // 🐳 E0.6 spouting whale
        listOf(0x1F40B),                                          // 🐋 E1.0 whale
        listOf(0x1F42C),                                          // 🐬 E0.6 dolphin
        listOf(0x1F9AD),                                          // 🦭 E13.0 seal
        listOf(0x1F41F),                                          // 🐟 E0.6 fish
        listOf(0x1F420),                                          // 🐠 E0.6 tropical fish
        listOf(0x1F421),                                          // 🐡 E0.6 blowfish
        listOf(0x1F988),                                          // 🦈 E3.0 shark
        listOf(0x1F419),                                          // 🐙 E0.6 octopus
        listOf(0x1F41A),                                          // 🐚 E0.6 spiral shell

        // subgroup: animal-bug
        listOf(0x1F40C),                                          // 🐌 E0.6 snail
        listOf(0x1F98B),                                          // 🦋 E3.0 butterfly
        listOf(0x1F41B),                                          // 🐛 E0.6 bug
        listOf(0x1F41C),                                          // 🐜 E0.6 ant
        listOf(0x1F41D),                                          // 🐝 E0.6 honeybee
        listOf(0x1FAB2),                                          // 🪲 E13.0 beetle
        listOf(0x1F41E),                                          // 🐞 E0.6 lady beetle
        listOf(0x1F997),                                          // 🦗 E5.0 cricket
        listOf(0x1FAB3),                                          // 🪳 E13.0 cockroach
        listOf(0x1F577, 0xFE0F),                                  // 🕷️ E0.7 spider
        listOf(0x1F578, 0xFE0F),                                  // 🕸️ E0.7 spider web
        listOf(0x1F982),                                          // 🦂 E1.0 scorpion
        listOf(0x1F99F),                                          // 🦟 E11.0 mosquito
        listOf(0x1FAB0),                                          // 🪰 E13.0 fly
        listOf(0x1FAB1),                                          // 🪱 E13.0 worm
        listOf(0x1F9A0),                                          // 🦠 E11.0 microbe

        // subgroup: plant-flower
        listOf(0x1F490),                                          // 💐 E0.6 bouquet
        listOf(0x1F338),                                          // 🌸 E0.6 cherry blossom
        listOf(0x1F4AE),                                          // 💮 E0.6 white flower
        listOf(0x1F3F5, 0xFE0F),                                  // 🏵️ E0.7 rosette
        listOf(0x1F339),                                          // 🌹 E0.6 rose
        listOf(0x1F940),                                          // 🥀 E3.0 wilted flower
        listOf(0x1F33A),                                          // 🌺 E0.6 hibiscus
        listOf(0x1F33B),                                          // 🌻 E0.6 sunflower
        listOf(0x1F33C),                                          // 🌼 E0.6 blossom
        listOf(0x1F337),                                          // 🌷 E0.6 tulip

        // subgroup: plant-other
        listOf(0x1F331),                                          // 🌱 E0.6 seedling
        listOf(0x1FAB4),                                          // 🪴 E13.0 potted plant
        listOf(0x1F332),                                          // 🌲 E1.0 evergreen tree
        listOf(0x1F333),                                          // 🌳 E1.0 deciduous tree
        listOf(0x1F334),                                          // 🌴 E0.6 palm tree
        listOf(0x1F335),                                          // 🌵 E0.6 cactus
        listOf(0x1F33E),                                          // 🌾 E0.6 sheaf of rice
        listOf(0x1F33F),                                          // 🌿 E0.6 herb
        listOf(0x2618, 0xFE0F),                                   // ☘️ E1.0 shamrock
        listOf(0x1F340),                                          // 🍀 E0.6 four leaf clover
        listOf(0x1F341),                                          // 🍁 E0.6 maple leaf
        listOf(0x1F342),                                          // 🍂 E0.6 fallen leaf
        listOf(0x1F343)                                           // 🍃 E0.6 leaf fluttering in wind
    )

    // group: Food & Drink
    fun getGroupFood(): List<List<Int>> = listOf(
        // subgroup: food-fruit
        listOf(0x1F347),                                          // 🍇 E0.6 grapes
        listOf(0x1F348),                                          // 🍈 E0.6 melon
        listOf(0x1F349),                                          // 🍉 E0.6 watermelon
        listOf(0x1F34A),                                          // 🍊 E0.6 tangerine
        listOf(0x1F34B),                                          // 🍋 E1.0 lemon
        listOf(0x1F34C),                                          // 🍌 E0.6 banana
        listOf(0x1F34D),                                          // 🍍 E0.6 pineapple
        listOf(0x1F96D),                                          // 🥭 E11.0 mango
        listOf(0x1F34E),                                          // 🍎 E0.6 red apple
        listOf(0x1F34F),                                          // 🍏 E0.6 green apple
        listOf(0x1F350),                                          // 🍐 E1.0 pear
        listOf(0x1F351),                                          // 🍑 E0.6 peach
        listOf(0x1F352),                                          // 🍒 E0.6 cherries
        listOf(0x1F353),                                          // 🍓 E0.6 strawberry
        listOf(0x1FAD0),                                          // 🫐 E13.0 blueberries
        listOf(0x1F95D),                                          // 🥝 E3.0 kiwi fruit
        listOf(0x1F345),                                          // 🍅 E0.6 tomato
        listOf(0x1FAD2),                                          // 🫒 E13.0 olive
        listOf(0x1F965),                                          // 🥥 E5.0 coconut

        // subgroup: food-vegetable
        listOf(0x1F951),                                          // 🥑 E3.0 avocado
        listOf(0x1F346),                                          // 🍆 E0.6 eggplant
        listOf(0x1F954),                                          // 🥔 E3.0 potato
        listOf(0x1F955),                                          // 🥕 E3.0 carrot
        listOf(0x1F33D),                                          // 🌽 E0.6 ear of corn
        listOf(0x1F336, 0xFE0F),                                  // 🌶️ E0.7 hot pepper
        listOf(0x1FAD1),                                          // 🫑 E13.0 bell pepper
        listOf(0x1F952),                                          // 🥒 E3.0 cucumber
        listOf(0x1F96C),                                          // 🥬 E11.0 leafy green
        listOf(0x1F966),                                          // 🥦 E5.0 broccoli
        listOf(0x1F9C4),                                          // 🧄 E12.0 garlic
        listOf(0x1F9C5),                                          // 🧅 E12.0 onion
        listOf(0x1F344),                                          // 🍄 E0.6 mushroom
        listOf(0x1F95C),                                          // 🥜 E3.0 peanuts
        listOf(0x1F330),                                          // 🌰 E0.6 chestnut

        // subgroup: food-prepared
        listOf(0x1F35E),                                          // 🍞 E0.6 bread
        listOf(0x1F950),                                          // 🥐 E3.0 croissant
        listOf(0x1F956),                                          // 🥖 E3.0 baguette bread
        listOf(0x1FAD3),                                          // 🫓 E13.0 flatbread
        listOf(0x1F968),                                          // 🥨 E5.0 pretzel
        listOf(0x1F96F),                                          // 🥯 E11.0 bagel
        listOf(0x1F95E),                                          // 🥞 E3.0 pancakes
        listOf(0x1F9C7),                                          // 🧇 E12.0 waffle
        listOf(0x1F9C0),                                          // 🧀 E1.0 cheese wedge
        listOf(0x1F356),                                          // 🍖 E0.6 meat on bone
        listOf(0x1F357),                                          // 🍗 E0.6 poultry leg
        listOf(0x1F969),                                          // 🥩 E5.0 cut of meat
        listOf(0x1F953),                                          // 🥓 E3.0 bacon
        listOf(0x1F354),                                          // 🍔 E0.6 hamburger
        listOf(0x1F35F),                                          // 🍟 E0.6 french fries
        listOf(0x1F355),                                          // 🍕 E0.6 pizza
        listOf(0x1F32D),                                          // 🌭 E1.0 hot dog
        listOf(0x1F96A),                                          // 🥪 E5.0 sandwich
        listOf(0x1F32E),                                          // 🌮 E1.0 taco
        listOf(0x1F32F),                                          // 🌯 E1.0 burrito
        listOf(0x1FAD4),                                          // 🫔 E13.0 tamale
        listOf(0x1F959),                                          // 🥙 E3.0 stuffed flatbread
        listOf(0x1F9C6),                                          // 🧆 E12.0 falafel
        listOf(0x1F95A),                                          // 🥚 E3.0 egg
        listOf(0x1F373),                                          // 🍳 E0.6 cooking
        listOf(0x1F958),                                          // 🥘 E3.0 shallow pan of food
        listOf(0x1F372),                                          // 🍲 E0.6 pot of food
        listOf(0x1FAD5),                                          // 🫕 E13.0 fondue
        listOf(0x1F963),                                          // 🥣 E5.0 bowl with spoon
        listOf(0x1F957),                                          // 🥗 E3.0 green salad
        listOf(0x1F37F),                                          // 🍿 E1.0 popcorn
        listOf(0x1F9C8),                                          // 🧈 E12.0 butter
        listOf(0x1F9C2),                                          // 🧂 E11.0 salt
        listOf(0x1F96B),                                          // 🥫 E5.0 canned food

        // subgroup: food-asian
        listOf(0x1F371),                                          // 🍱 E0.6 bento box
        listOf(0x1F358),                                          // 🍘 E0.6 rice cracker
        listOf(0x1F359),                                          // 🍙 E0.6 rice ball
        listOf(0x1F35A),                                          // 🍚 E0.6 cooked rice
        listOf(0x1F35B),                                          // 🍛 E0.6 curry rice
        listOf(0x1F35C),                                          // 🍜 E0.6 steaming bowl
        listOf(0x1F35D),                                          // 🍝 E0.6 spaghetti
        listOf(0x1F360),                                          // 🍠 E0.6 roasted sweet potato
        listOf(0x1F362),                                          // 🍢 E0.6 oden
        listOf(0x1F363),                                          // 🍣 E0.6 sushi
        listOf(0x1F364),                                          // 🍤 E0.6 fried shrimp
        listOf(0x1F365),                                          // 🍥 E0.6 fish cake with swirl
        listOf(0x1F96E),                                          // 🥮 E11.0 moon cake
        listOf(0x1F361),                                          // 🍡 E0.6 dango
        listOf(0x1F95F),                                          // 🥟 E5.0 dumpling
        listOf(0x1F960),                                          // 🥠 E5.0 fortune cookie
        listOf(0x1F961),                                          // 🥡 E5.0 takeout box

        // subgroup: food-marine
        listOf(0x1F980),                                          // 🦀 E1.0 crab
        listOf(0x1F99E),                                          // 🦞 E11.0 lobster
        listOf(0x1F990),                                          // 🦐 E3.0 shrimp
        listOf(0x1F991),                                          // 🦑 E3.0 squid
        listOf(0x1F9AA),                                          // 🦪 E12.0 oyster

        // subgroup: food-sweet
        listOf(0x1F366),                                          // 🍦 E0.6 soft ice cream
        listOf(0x1F367),                                          // 🍧 E0.6 shaved ice
        listOf(0x1F368),                                          // 🍨 E0.6 ice cream
        listOf(0x1F369),                                          // 🍩 E0.6 doughnut
        listOf(0x1F36A),                                          // 🍪 E0.6 cookie
        listOf(0x1F382),                                          // 🎂 E0.6 birthday cake
        listOf(0x1F370),                                          // 🍰 E0.6 shortcake
        listOf(0x1F9C1),                                          // 🧁 E11.0 cupcake
        listOf(0x1F967),                                          // 🥧 E5.0 pie
        listOf(0x1F36B),                                          // 🍫 E0.6 chocolate bar
        listOf(0x1F36C),                                          // 🍬 E0.6 candy
        listOf(0x1F36D),                                          // 🍭 E0.6 lollipop
        listOf(0x1F36E),                                          // 🍮 E0.6 custard
        listOf(0x1F36F),                                          // 🍯 E0.6 honey pot

        // subgroup: drink
        listOf(0x1F37C),                                          // 🍼 E1.0 baby bottle
        listOf(0x1F95B),                                          // 🥛 E3.0 glass of milk
        listOf(0x2615),                                           // ☕ E0.6 hot beverage
        listOf(0x1FAD6),                                          // 🫖 E13.0 teapot
        listOf(0x1F375),                                          // 🍵 E0.6 teacup without handle
        listOf(0x1F376),                                          // 🍶 E0.6 sake
        listOf(0x1F37E),                                          // 🍾 E1.0 bottle with popping cork
        listOf(0x1F377),                                          // 🍷 E0.6 wine glass
        listOf(0x1F378),                                          // 🍸 E0.6 cocktail glass
        listOf(0x1F379),                                          // 🍹 E0.6 tropical drink
        listOf(0x1F37A),                                          // 🍺 E0.6 beer mug
        listOf(0x1F37B),                                          // 🍻 E0.6 clinking beer mugs
        listOf(0x1F942),                                          // 🥂 E3.0 clinking glasses
        listOf(0x1F943),                                          // 🥃 E3.0 tumbler glass
        listOf(0x1F964),                                          // 🥤 E5.0 cup with straw
        listOf(0x1F9CB),                                          // 🧋 E13.0 bubble tea
        listOf(0x1F9C3),                                          // 🧃 E12.0 beverage box
        listOf(0x1F9C9),                                          // 🧉 E12.0 mate
        listOf(0x1F9CA),                                          // 🧊 E12.0 ice

        // subgroup: dishware
        listOf(0x1F962),                                          // 🥢 E5.0 chopsticks
        listOf(0x1F37D, 0xFE0F),                                  // 🍽️ E0.7 fork and knife with plate
        listOf(0x1F374),                                          // 🍴 E0.6 fork and knife
        listOf(0x1F944),                                          // 🥄 E3.0 spoon
        listOf(0x1F52A),                                          // 🔪 E0.6 kitchen knife
        listOf(0x1F3FA)                                           // 🏺 E1.0 amphora
    )

    // group: Travel & Places
    fun getGroupTravel(): List<List<Int>> = listOf(
        // subgroup: place-map
        listOf(0x1F30D),                                          // 🌍 E0.7 globe showing Europe-Africa
        listOf(0x1F30E),                                          // 🌎 E0.7 globe showing Americas
        listOf(0x1F30F),                                          // 🌏 E0.6 globe showing Asia-Australia
        listOf(0x1F310),                                          // 🌐 E1.0 globe with meridians
        listOf(0x1F5FA, 0xFE0F),                                  // 🗺️ E0.7 world map
        listOf(0x1F5FE),                                          // 🗾 E0.6 map of Japan
        listOf(0x1F9ED),                                          // 🧭 E11.0 compass

        // subgroup: place-geographic
        listOf(0x1F3D4, 0xFE0F),                                  // 🏔️ E0.7 snow-capped mountain
        listOf(0x26F0, 0xFE0F),                                   // ⛰️ E0.7 mountain
        listOf(0x1F30B),                                          // 🌋 E0.6 volcano
        listOf(0x1F5FB),                                          // 🗻 E0.6 mount fuji
        listOf(0x1F3D5, 0xFE0F),                                  // 🏕️ E0.7 camping
        listOf(0x1F3D6, 0xFE0F),                                  // 🏖️ E0.7 beach with umbrella
        listOf(0x1F3DC, 0xFE0F),                                  // 🏜️ E0.7 desert
        listOf(0x1F3DD, 0xFE0F),                                  // 🏝️ E0.7 desert island
        listOf(0x1F3DE, 0xFE0F),                                  // 🏞️ E0.7 national park

        // subgroup: place-building
        listOf(0x1F3DF, 0xFE0F),                                  // 🏟️ E0.7 stadium
        listOf(0x1F3DB, 0xFE0F),                                  // 🏛️ E0.7 classical building
        listOf(0x1F3D7, 0xFE0F),                                  // 🏗️ E0.7 building construction
        listOf(0x1F9F1),                                          // 🧱 E11.0 brick
        listOf(0x1FAA8),                                          // 🪨 E13.0 rock
        listOf(0x1FAB5),                                          // 🪵 E13.0 wood
        listOf(0x1F6D6),                                          // 🛖 E13.0 hut
        listOf(0x1F3D8, 0xFE0F),                                  // 🏘️ E0.7 houses
        listOf(0x1F3DA, 0xFE0F),                                  // 🏚️ E0.7 derelict house
        listOf(0x1F3E0),                                          // 🏠 E0.6 house
        listOf(0x1F3E1),                                          // 🏡 E0.6 house with garden
        listOf(0x1F3E2),                                          // 🏢 E0.6 office building
        listOf(0x1F3E3),                                          // 🏣 E0.6 Japanese post office
        listOf(0x1F3E4),                                          // 🏤 E1.0 post office
        listOf(0x1F3E5),                                          // 🏥 E0.6 hospital
        listOf(0x1F3E6),                                          // 🏦 E0.6 bank
        listOf(0x1F3E8),                                          // 🏨 E0.6 hotel
        listOf(0x1F3E9),                                          // 🏩 E0.6 love hotel
        listOf(0x1F3EA),                                          // 🏪 E0.6 convenience store
        listOf(0x1F3EB),                                          // 🏫 E0.6 school
        listOf(0x1F3EC),                                          // 🏬 E0.6 department store
        listOf(0x1F3ED),                                          // 🏭 E0.6 factory
        listOf(0x1F3EF),                                          // 🏯 E0.6 Japanese castle
        listOf(0x1F3F0),                                          // 🏰 E0.6 castle
        listOf(0x1F492),                                          // 💒 E0.6 wedding
        listOf(0x1F5FC),                                          // 🗼 E0.6 Tokyo tower
        listOf(0x1F5FD),                                          // 🗽 E0.6 Statue of Liberty

        // subgroup: place-religious
        listOf(0x26EA),                                           // ⛪ E0.6 church
        listOf(0x1F54C),                                          // 🕌 E1.0 mosque
        listOf(0x1F6D5),                                          // 🛕 E12.0 hindu temple
        listOf(0x1F54D),                                          // 🕍 E1.0 synagogue
        listOf(0x26E9, 0xFE0F),                                   // ⛩️ E0.7 shinto shrine
        listOf(0x1F54B),                                          // 🕋 E1.0 kaaba

        // subgroup: place-other
        listOf(0x26F2),                                           // ⛲ E0.6 fountain
        listOf(0x26FA),                                           // ⛺ E0.6 tent
        listOf(0x1F301),                                          // 🌁 E0.6 foggy
        listOf(0x1F303),                                          // 🌃 E0.6 night with stars
        listOf(0x1F3D9, 0xFE0F),                                  // 🏙️ E0.7 cityscape
        listOf(0x1F304),                                          // 🌄 E0.6 sunrise over mountains
        listOf(0x1F305),                                          // 🌅 E0.6 sunrise
        listOf(0x1F306),                                          // 🌆 E0.6 cityscape at dusk
        listOf(0x1F307),                                          // 🌇 E0.6 sunset
        listOf(0x1F309),                                          // 🌉 E0.6 bridge at night
        listOf(0x2668, 0xFE0F),                                   // ♨️ E0.6 hot springs
        listOf(0x1F3A0),                                          // 🎠 E0.6 carousel horse
        listOf(0x1F3A1),                                          // 🎡 E0.6 ferris wheel
        listOf(0x1F3A2),                                          // 🎢 E0.6 roller coaster
        listOf(0x1F488),                                          // 💈 E0.6 barber pole
        listOf(0x1F3AA),                                          // 🎪 E0.6 circus tent

        // subgroup: transport-ground
        listOf(0x1F682),                                          // 🚂 E1.0 locomotive
        listOf(0x1F683),                                          // 🚃 E0.6 railway car
        listOf(0x1F684),                                          // 🚄 E0.6 high-speed train
        listOf(0x1F685),                                          // 🚅 E0.6 bullet train
        listOf(0x1F686),                                          // 🚆 E1.0 train
        listOf(0x1F687),                                          // 🚇 E0.6 metro
        listOf(0x1F688),                                          // 🚈 E1.0 light rail
        listOf(0x1F689),                                          // 🚉 E0.6 station
        listOf(0x1F68A),                                          // 🚊 E1.0 tram
        listOf(0x1F69D),                                          // 🚝 E1.0 monorail
        listOf(0x1F69E),                                          // 🚞 E1.0 mountain railway
        listOf(0x1F68B),                                          // 🚋 E1.0 tram car
        listOf(0x1F68C),                                          // 🚌 E0.6 bus
        listOf(0x1F68D),                                          // 🚍 E0.7 oncoming bus
        listOf(0x1F68E),                                          // 🚎 E1.0 trolleybus
        listOf(0x1F690),                                          // 🚐 E1.0 minibus
        listOf(0x1F691),                                          // 🚑 E0.6 ambulance
        listOf(0x1F692),                                          // 🚒 E0.6 fire engine
        listOf(0x1F693),                                          // 🚓 E0.6 police car
        listOf(0x1F694),                                          // 🚔 E0.7 oncoming police car
        listOf(0x1F695),                                          // 🚕 E0.6 taxi
        listOf(0x1F696),                                          // 🚖 E1.0 oncoming taxi
        listOf(0x1F697),                                          // 🚗 E0.6 automobile
        listOf(0x1F698),                                          // 🚘 E0.7 oncoming automobile
        listOf(0x1F699),                                          // 🚙 E0.6 sport utility vehicle
        listOf(0x1F6FB),                                          // 🛻 E13.0 pickup truck
        listOf(0x1F69A),                                          // 🚚 E0.6 delivery truck
        listOf(0x1F69B),                                          // 🚛 E1.0 articulated lorry
        listOf(0x1F69C),                                          // 🚜 E1.0 tractor
        listOf(0x1F3CE, 0xFE0F),                                  // 🏎️ E0.7 racing car
        listOf(0x1F3CD, 0xFE0F),                                  // 🏍️ E0.7 motorcycle
        listOf(0x1F6F5),                                          // 🛵 E3.0 motor scooter
        listOf(0x1F9BD),                                          // 🦽 E12.0 manual wheelchair
        listOf(0x1F9BC),                                          // 🦼 E12.0 motorized wheelchair
        listOf(0x1F6FA),                                          // 🛺 E12.0 auto rickshaw
        listOf(0x1F6B2),                                          // 🚲 E0.6 bicycle
        listOf(0x1F6F4),                                          // 🛴 E3.0 kick scooter
        listOf(0x1F6F9),                                          // 🛹 E11.0 skateboard
        listOf(0x1F6FC),                                          // 🛼 E13.0 roller skate
        listOf(0x1F68F),                                          // 🚏 E0.6 bus stop
        listOf(0x1F6E3, 0xFE0F),                                  // 🛣️ E0.7 motorway
        listOf(0x1F6E4, 0xFE0F),                                  // 🛤️ E0.7 railway track
        listOf(0x1F6E2, 0xFE0F),                                  // 🛢️ E0.7 oil drum
        listOf(0x26FD),                                           // ⛽ E0.6 fuel pump
        listOf(0x1F6A8),                                          // 🚨 E0.6 police car light
        listOf(0x1F6A5),                                          // 🚥 E0.6 horizontal traffic light
        listOf(0x1F6A6),                                          // 🚦 E1.0 vertical traffic light
        listOf(0x1F6D1),                                          // 🛑 E3.0 stop sign
        listOf(0x1F6A7),                                          // 🚧 E0.6 construction

        // subgroup: transport-water
        listOf(0x2693),                                           // ⚓ E0.6 anchor
        listOf(0x26F5),                                           // ⛵ E0.6 sailboat
        listOf(0x1F6F6),                                          // 🛶 E3.0 canoe
        listOf(0x1F6A4),                                          // 🚤 E0.6 speedboat
        listOf(0x1F6F3, 0xFE0F),                                  // 🛳️ E0.7 passenger ship
        listOf(0x26F4, 0xFE0F),                                   // ⛴️ E0.7 ferry
        listOf(0x1F6E5, 0xFE0F),                                  // 🛥️ E0.7 motor boat
        listOf(0x1F6A2),                                          // 🚢 E0.6 ship

        // subgroup: transport-air
        listOf(0x2708, 0xFE0F),                                   // ✈️ E0.6 airplane
        listOf(0x1F6E9, 0xFE0F),                                  // 🛩️ E0.7 small airplane
        listOf(0x1F6EB),                                          // 🛫 E1.0 airplane departure
        listOf(0x1F6EC),                                          // 🛬 E1.0 airplane arrival
        listOf(0x1FA82),                                          // 🪂 E12.0 parachute
        listOf(0x1F4BA),                                          // 💺 E0.6 seat
        listOf(0x1F681),                                          // 🚁 E1.0 helicopter
        listOf(0x1F69F),                                          // 🚟 E1.0 suspension railway
        listOf(0x1F6A0),                                          // 🚠 E1.0 mountain cableway
        listOf(0x1F6A1),                                          // 🚡 E1.0 aerial tramway
        listOf(0x1F6F0, 0xFE0F),                                  // 🛰️ E0.7 satellite
        listOf(0x1F680),                                          // 🚀 E0.6 rocket
        listOf(0x1F6F8),                                          // 🛸 E5.0 flying saucer

        // subgroup: hotel
        listOf(0x1F6CE, 0xFE0F),                                  // 🛎️ E0.7 bellhop bell
        listOf(0x1F9F3),                                          // 🧳 E11.0 luggage

        // subgroup: time
        listOf(0x231B),                                           // ⌛ E0.6 hourglass done
        listOf(0x23F3),                                           // ⏳ E0.6 hourglass not done
        listOf(0x231A),                                           // ⌚ E0.6 watch
        listOf(0x23F0),                                           // ⏰ E0.6 alarm clock
        listOf(0x23F1, 0xFE0F),                                   // ⏱️ E1.0 stopwatch
        listOf(0x23F2, 0xFE0F),                                   // ⏲️ E1.0 timer clock
        listOf(0x1F570, 0xFE0F),                                  // 🕰️ E0.7 mantelpiece clock
        listOf(0x1F55B),                                          // 🕛 E0.6 twelve o’clock
        listOf(0x1F567),                                          // 🕧 E0.7 twelve-thirty
        listOf(0x1F550),                                          // 🕐 E0.6 one o’clock
        listOf(0x1F55C),                                          // 🕜 E0.7 one-thirty
        listOf(0x1F551),                                          // 🕑 E0.6 two o’clock
        listOf(0x1F55D),                                          // 🕝 E0.7 two-thirty
        listOf(0x1F552),                                          // 🕒 E0.6 three o’clock
        listOf(0x1F55E),                                          // 🕞 E0.7 three-thirty
        listOf(0x1F553),                                          // 🕓 E0.6 four o’clock
        listOf(0x1F55F),                                          // 🕟 E0.7 four-thirty
        listOf(0x1F554),                                          // 🕔 E0.6 five o’clock
        listOf(0x1F560),                                          // 🕠 E0.7 five-thirty
        listOf(0x1F555),                                          // 🕕 E0.6 six o’clock
        listOf(0x1F561),                                          // 🕡 E0.7 six-thirty
        listOf(0x1F556),                                          // 🕖 E0.6 seven o’clock
        listOf(0x1F562),                                          // 🕢 E0.7 seven-thirty
        listOf(0x1F557),                                          // 🕗 E0.6 eight o’clock
        listOf(0x1F563),                                          // 🕣 E0.7 eight-thirty
        listOf(0x1F558),                                          // 🕘 E0.6 nine o’clock
        listOf(0x1F564),                                          // 🕤 E0.7 nine-thirty
        listOf(0x1F559),                                          // 🕙 E0.6 ten o’clock
        listOf(0x1F565),                                          // 🕥 E0.7 ten-thirty
        listOf(0x1F55A),                                          // 🕚 E0.6 eleven o’clock
        listOf(0x1F566),                                          // 🕦 E0.7 eleven-thirty

        // subgroup: sky & weather
        listOf(0x1F311),                                          // 🌑 E0.6 new moon
        listOf(0x1F312),                                          // 🌒 E1.0 waxing crescent moon
        listOf(0x1F313),                                          // 🌓 E0.6 first quarter moon
        listOf(0x1F314),                                          // 🌔 E0.6 waxing gibbous moon
        listOf(0x1F315),                                          // 🌕 E0.6 full moon
        listOf(0x1F316),                                          // 🌖 E1.0 waning gibbous moon
        listOf(0x1F317),                                          // 🌗 E1.0 last quarter moon
        listOf(0x1F318),                                          // 🌘 E1.0 waning crescent moon
        listOf(0x1F319),                                          // 🌙 E0.6 crescent moon
        listOf(0x1F31A),                                          // 🌚 E1.0 new moon face
        listOf(0x1F31B),                                          // 🌛 E0.6 first quarter moon face
        listOf(0x1F31C),                                          // 🌜 E0.7 last quarter moon face
        listOf(0x1F321, 0xFE0F),                                  // 🌡️ E0.7 thermometer
        listOf(0x2600, 0xFE0F),                                   // ☀️ E0.6 sun
        listOf(0x1F31D),                                          // 🌝 E1.0 full moon face
        listOf(0x1F31E),                                          // 🌞 E1.0 sun with face
        listOf(0x1FA90),                                          // 🪐 E12.0 ringed planet
        listOf(0x2B50),                                           // ⭐ E0.6 star
        listOf(0x1F31F),                                          // 🌟 E0.6 glowing star
        listOf(0x1F320),                                          // 🌠 E0.6 shooting star
        listOf(0x1F30C),                                          // 🌌 E0.6 milky way
        listOf(0x2601, 0xFE0F),                                   // ☁️ E0.6 cloud
        listOf(0x26C5),                                           // ⛅ E0.6 sun behind cloud
        listOf(0x26C8, 0xFE0F),                                   // ⛈️ E0.7 cloud with lightning and rain
        listOf(0x1F324, 0xFE0F),                                  // 🌤️ E0.7 sun behind small cloud
        listOf(0x1F325, 0xFE0F),                                  // 🌥️ E0.7 sun behind large cloud
        listOf(0x1F326, 0xFE0F),                                  // 🌦️ E0.7 sun behind rain cloud
        listOf(0x1F327, 0xFE0F),                                  // 🌧️ E0.7 cloud with rain
        listOf(0x1F328, 0xFE0F),                                  // 🌨️ E0.7 cloud with snow
        listOf(0x1F329, 0xFE0F),                                  // 🌩️ E0.7 cloud with lightning
        listOf(0x1F32A, 0xFE0F),                                  // 🌪️ E0.7 tornado
        listOf(0x1F32B, 0xFE0F),                                  // 🌫️ E0.7 fog
        listOf(0x1F32C, 0xFE0F),                                  // 🌬️ E0.7 wind face
        listOf(0x1F300),                                          // 🌀 E0.6 cyclone
        listOf(0x1F308),                                          // 🌈 E0.6 rainbow
        listOf(0x1F302),                                          // 🌂 E0.6 closed umbrella
        listOf(0x2602, 0xFE0F),                                   // ☂️ E0.7 umbrella
        listOf(0x2614),                                           // ☔ E0.6 umbrella with rain drops
        listOf(0x26F1, 0xFE0F),                                   // ⛱️ E0.7 umbrella on ground
        listOf(0x26A1),                                           // ⚡ E0.6 high voltage
        listOf(0x2744, 0xFE0F),                                   // ❄️ E0.6 snowflake
        listOf(0x2603, 0xFE0F),                                   // ☃️ E0.7 snowman
        listOf(0x26C4),                                           // ⛄ E0.6 snowman without snow
        listOf(0x2604, 0xFE0F),                                   // ☄️ E1.0 comet
        listOf(0x1F525),                                          // 🔥 E0.6 fire
        listOf(0x1F4A7),                                          // 💧 E0.6 droplet
        listOf(0x1F30A)                                           // 🌊 E0.6 water wave
    )

    companion object {
        const val SKIN_TONE: Int = 0
        val skinTones: List<Int> = listOf(
            0x1F3FB,                                              // 🏻 light skin tone
            0x1F3FC,                                              // 🏼 medium-light skin tone
            0x1F3FD,                                              // 🏽 medium skin tone
            0x1F3FE,                                              // 🏾 medium-dark skin tone
            0x1F3FF                                               // 🏿 dark skin tone
        )
    }
}