package at.xirado.bean.misc.game;

import at.xirado.bean.misc.manager.WordleManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class Wordle
{
    private static final Color GREEN = new Color(0, 100, 0);
    private static final Color YELLOW = new Color(246, 190, 0);
    private static final Color GRAY = Color.GRAY;

    private static final int SQUARES_PER_ROW = 5;
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private static final int PADDING = 10;
    private static final int SQUARE_LENGTH = (WIDTH - (PADDING * (SQUARES_PER_ROW + 1))) / SQUARES_PER_ROW;

    private static Font FONT;

    static
    {
        try
        {
            FONT = Font.createFont(Font.TRUETYPE_FONT, Wordle.class.getResourceAsStream("/assets/fonts/NotoSans.ttf"));
        }
        catch (FontFormatException | IOException e)
        {
            e.printStackTrace();
        }
    }

    private final long userId;

    private int currentTry = 0;
    private char[][] field = new char[5][5];

    public Wordle(long userId)
    {
        this.userId = userId;
    }

    public void setTry(String word)
    {
        word = word.toUpperCase(Locale.ROOT);
        if (!WordleManager.wordleAllowedGuesses.contains(word) && !WordleManager.wordleAnswers.contains(word))
            throw new IllegalArgumentException(word + " is not a valid word!");

        field[currentTry] = word.toCharArray();
        currentTry++;
    }

    public boolean hasWon()
    {
        if (currentTry == 0)
            return false;

        for (int i = 1; i <= currentTry; i++)
        {
            if (Arrays.equals(field[i - 1], WordleManager.getCurrentWord().toCharArray()))
                return true;
        }

        return false;
    }

    public byte[] generateImage() throws IOException
    {
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        var graphics = image.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        graphics.drawImage(drawBackground(), 0, 0, WIDTH, HEIGHT, null);

        graphics.setFont(FONT.deriveFont(80f).deriveFont(Font.BOLD));

        FontMetrics metrics = graphics.getFontMetrics();

        graphics.setColor(Color.white);

        for (int i = 1; i <= SQUARES_PER_ROW; i++)
        {
            for (int j = 1; j <= SQUARES_PER_ROW; j++)
            {
                if (field[i - 1][j - 1] != 0)
                {
                    char character = field[i - 1][j - 1];
                    int xPosition = (PADDING * j) + (SQUARE_LENGTH * (j - 1));
                    int yPosition = (PADDING * i) + (SQUARE_LENGTH * (i - 1)) + SQUARE_LENGTH - (PADDING/2);

                    var stringBounds = metrics.getStringBounds(String.valueOf(character), graphics);

                    int stringWidth = (int) stringBounds.getWidth();
                    int stringHeight = (int) stringBounds.getHeight();

                    graphics.drawString(String.valueOf(character), (xPosition + (SQUARE_LENGTH/2)) - (stringWidth / 2), (yPosition + (SQUARE_LENGTH/2)) - (stringHeight / 2));
                }
            }
        }


        var outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);

        return outputStream.toByteArray();
    }

    private BufferedImage drawBackground()
    {
        var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        graphics.setColor(Color.BLACK);

        char[] currentWord = WordleManager.getCurrentWord().toCharArray();

        for (int i = 1; i <= SQUARES_PER_ROW; i++)
        {
            for (int j = 1; j <= SQUARES_PER_ROW; j++)
            {
                if (field[i - 1][j - 1] != 0)
                {
                    char character = field[i - 1][j - 1];

                    if (WordleManager.getCurrentWord().indexOf(character) != -1)
                    {
                        if (currentWord[j-1] == character)
                        {
                            graphics.setColor(GREEN);
                            graphics.fillRect((PADDING * j) + (SQUARE_LENGTH * (j - 1)), (PADDING * i) + (SQUARE_LENGTH * (i - 1)), SQUARE_LENGTH, SQUARE_LENGTH);
                            graphics.setColor(Color.black);
                        }
                        else
                        {
                            graphics.setColor(YELLOW);
                            graphics.fillRect((PADDING * j) + (SQUARE_LENGTH * (j - 1)), (PADDING * i) + (SQUARE_LENGTH * (i - 1)), SQUARE_LENGTH, SQUARE_LENGTH);
                            graphics.setColor(Color.black);
                        }
                    }
                    else
                    {
                        graphics.setColor(GRAY);
                        graphics.fillRect((PADDING * j) + (SQUARE_LENGTH * (j - 1)), (PADDING * i) + (SQUARE_LENGTH * (i - 1)), SQUARE_LENGTH, SQUARE_LENGTH);
                        graphics.setColor(Color.BLACK);
                    }
                }
                else
                {
                    graphics.drawRect((PADDING * j) + (SQUARE_LENGTH * (j - 1)), (PADDING * i) + (SQUARE_LENGTH * (i - 1)), SQUARE_LENGTH, SQUARE_LENGTH);
                }
            }
        }

        graphics.dispose();
        return image;
    }

    public String getWord()
    {
        return WordleManager.getCurrentWord();
    }

    public long getUserId()
    {
        return userId;
    }

    public int getCurrentTry()
    {
        return currentTry;
    }
}
