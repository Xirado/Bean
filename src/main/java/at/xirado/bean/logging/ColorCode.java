package at.xirado.bean.logging;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Uses JLine3's AttributedStringBuilder to format Strings with Minecraft Color-codes
 */
public class ColorCode
{

    public static final String DARK_RED = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xAA, 0x00, 0x00)).toAnsi();
    public static final String RED = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xFF, 0x55, 0x55)).toAnsi();
    public static final String GOLD = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xFF, 0xAA, 0x00)).append("").toAnsi();
    public static final String YELLOW = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xFF, 0xFF, 0x55)).append("").toAnsi();
    public static final String DARK_GREEN = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x00, 0xAA, 0x00)).append("").toAnsi();
    public static final String GREEN = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x55, 0xFF, 0x55)).append("").toAnsi();
    public static final String AQUA = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x55, 0xFF, 0xFF)).append("").toAnsi();
    public static final String DARK_AQUA = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x00, 0xAA, 0xAA)).append("").toAnsi();
    public static final String DARK_BLUE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x00, 0x00, 0xAA)).append("").toAnsi();
    public static final String BLUE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x55, 0x55, 0xFF)).append("").toAnsi();
    public static final String LIGHT_PURPLE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xFF, 0x55, 0xFF)).append("").toAnsi();
    public static final String DARK_PURPLE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xAA, 0x00, 0xAA)).append("").toAnsi();
    public static final String WHITE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xFF, 0xFF, 0xFF)).append("").toAnsi();
    public static final String GRAY = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0xAA, 0xAA, 0xAA)).append("").toAnsi();
    public static final String DARK_GRAY = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x55, 0x55, 0x55)).append("").toAnsi();
    public static final String BLACK = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.foreground(0x00, 0x00, 0x00)).append("").toAnsi();
    public static final String BOLD = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.bold()).append("").toAnsi();
    public static final String UNDERLINE = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.underline()).append("").toAnsi();
    public static final String ITALIC = new AttributedStringBuilder().style(AttributedStyle.DEFAULT.italic()).append("").toAnsi();
    public static final String RESET = new AttributedStringBuilder().style(AttributedStyle.DEFAULT).append("").toAnsi();


    public static String translateChatColor(char character, String text)
    {
        char[] chararray = text.toCharArray();
        AttributedStringBuilder attributedStringBuilder = new AttributedStringBuilder();
        for (int i = 0; i < chararray.length; i++)
        {
            if (chararray[i] == character && chararray.length > i+1)
            {
                char colorcode = chararray[i + 1];
                boolean validCode = true;
                switch (colorcode)
                {
                    case '4':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xAA, 0x00, 0x00));
                        break;
                    case 'c':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xFF, 0x55, 0x55));
                        break;
                    case '6':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xFF, 0xAA, 0x00));
                        break;
                    case 'e':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xFF, 0xFF, 0x55));
                        break;
                    case '2':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x00, 0xAA, 0x00));
                        break;
                    case 'a':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x55, 0xFF, 0x55));
                        break;
                    case 'b':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x55, 0xFF, 0xFF));
                        break;
                    case '3':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x00, 0xAA, 0xAA));
                        break;
                    case '1':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x00, 0x00, 0xAA));
                        break;
                    case '9':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x55, 0x55, 0xFF));
                        break;
                    case 'd':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xFF, 0x55, 0xFF));
                        break;
                    case '5':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xAA, 0x00, 0xAA));
                        break;
                    case 'f':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xFF, 0xFF, 0xFF));
                        break;
                    case '7':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0xAA, 0xAA, 0xAA));
                        break;
                    case '8':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x55, 0x55, 0x55));
                        break;
                    case '0':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.foreground(0x00, 0x00, 0x00));
                        break;
                    case 'l':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.boldDefault());
                        break;
                    case 'n':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.underline());
                        break;
                    case 'o':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT.italicDefault());
                        break;
                    case 'r':
                        attributedStringBuilder.style(AttributedStyle.DEFAULT);
                        break;
                    default:
                        validCode = false;

                }
                if (validCode && i+1 < chararray.length)
                {
                    chararray[i] = Character.MIN_VALUE;
                    chararray[i + 1] = Character.MIN_VALUE;
                }
            }
            attributedStringBuilder.append(chararray[i]);
        }
        return attributedStringBuilder.toAnsi();
    }
}
