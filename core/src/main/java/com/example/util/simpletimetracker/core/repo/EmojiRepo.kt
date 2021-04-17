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
    )

    companion object {
        const val SKIN_TONE: Int = 0
    }
}