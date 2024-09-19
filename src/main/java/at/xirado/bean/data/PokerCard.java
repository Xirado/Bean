/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.xirado.bean.data;

import java.util.Arrays;

public enum PokerCard {
    // Clubs
    ACE_CLUB("<:ace_of_clubs:909935152019410975>", CardSuit.CLUB, true, false, false, 11),
    TWO_CLUB("<:2_of_clubs:909935143601446922>", CardSuit.CLUB, false, true, false, 2),
    THREE_CLUB("<:3_of_clubs:909935148483629106>", CardSuit.CLUB, false, true, false, 3),
    FOUR_CLUB("<:4_of_clubs:909935149611876382>", CardSuit.CLUB, false, true, false, 4),
    FIVE_CLUB("<:5_of_clubs:909935149867728907>", CardSuit.CLUB, false, true, false, 5),
    SIX_CLUB("<:6_of_clubs:909935150182314084>", CardSuit.CLUB, false, true, false, 6),
    SEVEN_CLUB("<:7_of_clubs:909935152073932861>", CardSuit.CLUB, false, true, false, 7),
    EIGHT_CLUB("<:8_of_clubs:909935152174624790>", CardSuit.CLUB, false, true, false, 8),
    NINE_CLUB("<:9_of_clubs:909935152526921799>", CardSuit.CLUB, false, true, false, 9),
    TEN_CLUB("<:10_of_clubs:909935152728248370>", CardSuit.CLUB, false, true, false, 10),
    JACK_CLUB("<:jack_of_clubs:909938947629662229>", CardSuit.CLUB, false, false, true, 10),
    QUEEN_CLUB("<:queen_of_clubs:909938949038956594>", CardSuit.CLUB, false, false, true, 10),
    KING_CLUB("<:king_of_clubs:909938949596798976>", CardSuit.CLUB, false, false, true, 10),

    // Diamonds
    ACE_DIAMOND("<:ace_of_diamonds:909935151016988682>", CardSuit.DIAMOND, true, false, false, 11),
    TWO_DIAMOND("<:2_of_diamonds:909935144884916285>", CardSuit.DIAMOND, false, true, false, 2),
    THREE_DIAMOND("<:3_of_diamonds:909935146004787221>", CardSuit.DIAMOND, false, true, false, 3),
    FOUR_DIAMOND("<:4_of_diamonds:909935146675867662>", CardSuit.DIAMOND, false, true, false, 4),
    FIVE_DIAMOND("<:5_of_diamonds:909935150308147241>", CardSuit.DIAMOND, false, true, false, 5),
    SIX_DIAMOND("<:6_of_diamonds:909935150106816573>", CardSuit.DIAMOND, false, true, false, 6),
    SEVEN_DIAMOND("<:7_of_diamonds:909935150173937665>", CardSuit.DIAMOND, false, true, false, 7),
    EIGHT_DIAMOND("<:8_of_diamonds:909935152174624789>", CardSuit.DIAMOND, false, true, false, 8),
    NINE_DIAMOND("<:9_of_diamonds:909935152162033674>", CardSuit.DIAMOND, false, true, false, 9),
    TEN_DIAMOND("<:10_of_diamonds:909935152287842314>", CardSuit.DIAMOND, false, true, false, 10),
    JACK_DIAMOND("<:jack_of_diamonds:909938948137189396>", CardSuit.DIAMOND, false, false, true, 10),
    QUEEN_DIAMOND("<:queen_of_diamonds:909938948854415420>", CardSuit.DIAMOND, false, false, true, 10),
    KING_DIAMOND("<:king_of_diamonds:909938948724371547>", CardSuit.DIAMOND, false, false, true, 10),

    // Hearts
    ACE_HEART("<:ace_of_hearts:909935152354979920>", CardSuit.HEART, true, false, false, 11),
    TWO_HEART("<:2_of_hearts:909935145094619176>", CardSuit.HEART, false, true, false, 2),
    THREE_HEART("<:3_of_hearts:909935149087588372>", CardSuit.HEART, false, true, false, 3),
    FOUR_HEART("<:4_of_hearts:909935147187593239>", CardSuit.HEART, false, true, false, 4),
    FIVE_HEART("<:5_of_hearts:909935150119399494>", CardSuit.HEART, false, true, false, 5),
    SIX_HEART("<:6_of_hearts:909935150308147242>", CardSuit.HEART, false, true, false, 6),
    SEVEN_HEART("<:7_of_hearts:909935150383648828>", CardSuit.HEART, false, true, false, 7),
    EIGHT_HEART("<:8_of_hearts:909935152065576990>", CardSuit.HEART, false, true, false, 8),
    NINE_HEART("<:9_of_hearts:909935152266895400>", CardSuit.HEART, false, true, false, 9),
    TEN_HEART("<:10_of_hearts:909935152719888384>", CardSuit.HEART, false, true, false, 10),
    JACK_HEART("<:jack_of_hearts:909938948426592277>", CardSuit.HEART, false, false, true, 10),
    QUEEN_HEART("<:queen_of_hearts:909938949114454066>", CardSuit.HEART, false, false, true, 10),
    KING_HEART("<:king_of_hearts:909938948854403115>", CardSuit.HEART, false, false, true, 10),

    //Spades
    ACE_SPADE("<:ace_of_spades2:909935152162033675>", CardSuit.SPADE, true, false, false, 11),
    TWO_SPADE("<:2_of_spades:909935145086226522>", CardSuit.SPADE, false, true, false, 2),
    THREE_SPADE("<:3_of_spades:909935146852028456>", CardSuit.SPADE, false, true, false, 3),
    FOUR_SPADE("<:4_of_spades:909935147644776488>", CardSuit.SPADE, false, true, false, 4),
    FIVE_SPADE("<:5_of_spades:909935150144557076>", CardSuit.SPADE, false, true, false, 5),
    SIX_SPADE("<:6_of_spades:909935150773719100>", CardSuit.SPADE, false, true, false, 6),
    SEVEN_SPADE("<:7_of_spades:909935150886965330>", CardSuit.SPADE, false, true, false, 7),
    EIGHT_SPADE("<:8_of_spades:909935152166211644>", CardSuit.SPADE, false, true, false, 8),
    NINE_SPADE("<:9_of_spades:909935152203984960>", CardSuit.SPADE, false, true, false, 9),
    TEN_SPADE("<:10_of_spades:909935152711483463>", CardSuit.SPADE, false, true, false, 10),
    JACK_SPADE("<:jack_of_spades:909938949068312586>", CardSuit.SPADE, false, false, true, 10),
    QUEEN_SPADE("<:queen_of_spades:909938948854403113>", CardSuit.SPADE, false, false, true, 10),
    KING_SPADE("<:king_of_spades:909938948854403112>", CardSuit.SPADE, false, false, true, 10);

    enum CardSuit {
        CLUB(),
        DIAMOND(),
        HEART(),
        SPADE();
    }

    private final String emote;
    private final CardSuit cardType;
    private final boolean isAce, isNumber, isPicture;
    private final int value;

    PokerCard(String emote, CardSuit cardType, boolean isAce, boolean isNumber, boolean isPicture, int value) {
        this.emote = emote;
        this.cardType = cardType;
        this.isAce = isAce;
        this.isNumber = isNumber;
        this.isPicture = isPicture;
        this.value = value;
    }

    public String getEmote() {
        return emote;
    }

    public CardSuit getCardType() {
        return cardType;
    }

    public boolean isAce() {
        return isAce;
    }

    public boolean isNumber() {
        return isNumber;
    }

    public boolean isPicture() {
        return isPicture;
    }

    public int getValue() {
        return value;
    }

    public static PokerCard[] getCards() {
        return PokerCard.values();
    }

    public static PokerCard[] getCards(CardSuit cardType) {
        return Arrays.stream(PokerCard.values())
                .filter(card -> card.cardType == cardType)
                .toArray(PokerCard[]::new);
    }
}
