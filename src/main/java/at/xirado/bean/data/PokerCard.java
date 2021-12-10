package at.xirado.bean.data;

import java.util.Arrays;

public enum PokerCard
{
    // Clubs
    ACE_CLUB("<:ace_of_clubs:909935152019410975>", CardColor.CLUB, true, false, false, 11),
    TWO_CLUB("<:2_of_clubs:909935143601446922>", CardColor.CLUB, false, true, false, 2),
    THREE_CLUB("<:3_of_clubs:909935148483629106>", CardColor.CLUB, false, true, false, 3),
    FOUR_CLUB("<:4_of_clubs:909935149611876382>", CardColor.CLUB, false, true, false, 4),
    FIVE_CLUB("<:5_of_clubs:909935149867728907>", CardColor.CLUB, false, true, false, 5),
    SIX_CLUB("<:6_of_clubs:909935150182314084>", CardColor.CLUB, false, true, false, 6),
    SEVEN_CLUB("<:7_of_clubs:909935152073932861>", CardColor.CLUB, false, true, false, 7),
    EIGHT_CLUB("<:8_of_clubs:909935152174624790>", CardColor.CLUB, false, true, false, 8),
    NINE_CLUB("<:9_of_clubs:909935152526921799>", CardColor.CLUB, false, true, false, 9),
    TEN_CLUB("<:10_of_clubs:909935152728248370>", CardColor.CLUB, false, true, false, 10),
    JACK_CLUB("<:jack_of_clubs:909938947629662229>", CardColor.CLUB, false, false, true, 10),
    QUEEN_CLUB("<:queen_of_clubs:909938949038956594>", CardColor.CLUB, false, false, true, 10),
    KING_CLUB("<:king_of_clubs:909938949596798976>", CardColor.CLUB, false, false, true, 10),

    // Diamonds
    ACE_DIAMOND("<:ace_of_diamonds:909935151016988682>", CardColor.DIAMOND, true, false, false, 11),
    TWO_DIAMOND("<:2_of_diamonds:909935144884916285>", CardColor.DIAMOND, false, true, false, 2),
    THREE_DIAMOND("<:3_of_diamonds:909935146004787221>", CardColor.DIAMOND, false, true, false, 3),
    FOUR_DIAMOND("<:4_of_diamonds:909935146675867662>", CardColor.DIAMOND, false, true, false, 4),
    FIVE_DIAMOND("<:5_of_diamonds:909935150308147241>", CardColor.DIAMOND, false, true, false, 5),
    SIX_DIAMOND("<:6_of_diamonds:909935150106816573>", CardColor.DIAMOND, false, true, false, 6),
    SEVEN_DIAMOND("<:7_of_diamonds:909935150173937665>", CardColor.DIAMOND, false, true, false, 7),
    EIGHT_DIAMOND("<:8_of_diamonds:909935152174624789>", CardColor.DIAMOND, false, true, false, 8),
    NINE_DIAMOND("<:9_of_diamonds:909935152162033674>", CardColor.DIAMOND, false, true, false, 9),
    TEN_DIAMOND("<:10_of_diamonds:909935152287842314>", CardColor.DIAMOND, false, true, false, 10),
    JACK_DIAMOND("<:jack_of_diamonds:909938948137189396>", CardColor.DIAMOND, false, false, true, 10),
    QUEEN_DIAMOND("<:queen_of_diamonds:909938948854415420>", CardColor.DIAMOND, false, false, true, 10),
    KING_DIAMOND("<:king_of_diamonds:909938948724371547>", CardColor.DIAMOND, false, false, true, 10),

    // Hearts
    ACE_HEART("<:ace_of_hearts:909935152354979920>", CardColor.HEART, true, false, false, 11),
    TWO_HEART("<:2_of_hearts:909935145094619176>", CardColor.HEART, false, true, false, 2),
    THREE_HEART("<:3_of_hearts:909935149087588372>", CardColor.HEART, false, true, false, 3),
    FOUR_HEART("<:4_of_hearts:909935147187593239>", CardColor.HEART, false, true, false, 4),
    FIVE_HEART("<:5_of_hearts:909935150119399494>", CardColor.HEART, false, true, false, 5),
    SIX_HEART("<:6_of_hearts:909935150308147242>", CardColor.HEART, false, true, false, 6),
    SEVEN_HEART("<:7_of_hearts:909935150383648828>", CardColor.HEART, false, true, false, 7),
    EIGHT_HEART("<:8_of_hearts:909935152065576990>", CardColor.HEART, false, true, false, 8),
    NINE_HEART("<:9_of_hearts:909935152266895400>", CardColor.HEART, false, true, false, 9),
    TEN_HEART("<:10_of_hearts:909935152719888384>", CardColor.HEART, false, true, false, 10),
    JACK_HEART("<:jack_of_hearts:909938948426592277>", CardColor.HEART, false, false, true, 10),
    QUEEN_HEART("<:queen_of_hearts:909938949114454066>", CardColor.HEART, false, false, true, 10),
    KING_HEART("<:king_of_hearts:909938948854403115>", CardColor.HEART, false, false, true, 10),

    //Spades
    ACE_SPADE("<:ace_of_spades2:909935152162033675>", CardColor.SPADE, true, false, false, 11),
    TWO_SPADE("<:2_of_spades:909935145086226522>", CardColor.SPADE, false, true, false, 2),
    THREE_SPADE("<:3_of_spades:909935146852028456>", CardColor.SPADE, false, true, false, 3),
    FOUR_SPADE("<:4_of_spades:909935147644776488>", CardColor.SPADE, false, true, false, 4),
    FIVE_SPADE("<:5_of_spades:909935150144557076>", CardColor.SPADE, false, true, false, 5),
    SIX_SPADE("<:6_of_spades:909935150773719100>", CardColor.SPADE, false, true, false, 6),
    SEVEN_SPADE("<:7_of_spades:909935150886965330>", CardColor.SPADE, false, true, false, 7),
    EIGHT_SPADE("<:8_of_spades:909935152166211644>", CardColor.SPADE, false, true, false, 8),
    NINE_SPADE("<:9_of_spades:909935152203984960>", CardColor.SPADE, false, true, false, 9),
    TEN_SPADE("<:10_of_spades:909935152711483463>", CardColor.SPADE, false, true, false, 10),
    JACK_SPADE("<:jack_of_spades:909938949068312586>", CardColor.SPADE, false, false, true, 10),
    QUEEN_SPADE("<:queen_of_spades:909938948854403113>", CardColor.SPADE, false, false, true, 10),
    KING_SPADE("<:king_of_spades:909938948854403112>", CardColor.SPADE, false, false, true, 10);
    enum CardColor
    {
        CLUB(),
        DIAMOND(),
        HEART(),
        SPADE();
    }

    private final String emote;
    private final CardColor cardType;
    private final boolean isAce, isNumber, isPicture;
    private final int value;

    PokerCard(String emote, CardColor cardType, boolean isAce, boolean isNumber, boolean isPicture, int value)
    {
        this.emote = emote;
        this.cardType = cardType;
        this.isAce = isAce;
        this.isNumber = isNumber;
        this.isPicture = isPicture;
        this.value = value;
    }

    public String getEmote()
    {
        return emote;
    }

    public CardColor getCardType()
    {
        return cardType;
    }

    public boolean isAce()
    {
        return isAce;
    }

    public boolean isNumber()
    {
        return isNumber;
    }

    public boolean isPicture()
    {
        return isPicture;
    }

    public int getValue()
    {
        return value;
    }

    public static PokerCard[] getCards()
    {
        return PokerCard.values();
    }

    public static PokerCard[] getCards(CardColor cardType)
    {
        return Arrays.stream(PokerCard.values())
                .filter(card -> card.cardType == cardType)
                .toArray(PokerCard[]::new);
    }
}
