package at.xirado.bean.misc.manager;

import at.xirado.bean.Bean;
import at.xirado.bean.data.database.SQLBuilder;
import at.xirado.bean.misc.EmbedUtil;
import at.xirado.bean.misc.game.Wordle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class WordleManager extends ListenerAdapter
{
    private static final int MILLIS_PER_DAY = 86400000;
    private static final String WORDLE_ANSWERS_URL = "https://gist.githubusercontent.com/cfreshman/a03ef2cba789d8cf00c08f767e0fad7b/raw/28804271b5a226628d36ee831b0e36adef9cf449/wordle-answers-alphabetical.txt";
    private static final String WORDLE_ALLOWED_GUESSES_URL = "https://gist.githubusercontent.com/cfreshman/cdcdf777450c5b5301e439061d29694c/raw/b8375870720504ecf89c1970ea4532454f12de94/wordle-allowed-guesses.txt";
    private static final Logger LOG = LoggerFactory.getLogger(WordleManager.class);

    private static final List<Wordle> currentWordleTries = Collections.synchronizedList(new ArrayList<>());
    private static final List<Long> finishedUsers = Collections.synchronizedList(new ArrayList<>());

    public static List<String> wordleAnswers;
    public static List<String> wordleAllowedGuesses;

    private static String currentWord;

    public static boolean hasFinishedDaily(long userId)
    {
        return finishedUsers.contains(userId);
    }

    public static Wordle getWordle(long userId)
    {
        return currentWordleTries.stream().filter(wordle -> wordle.getUserId() == userId).findFirst().orElse(null);
    }

    public static Wordle createWordleGame(long userId)
    {
        if (getWordle(userId) != null)
            return getWordle(userId);

        Wordle wordle = new Wordle(userId);
        currentWordleTries.add(wordle);
        return wordle;
    }

    public static void updateAnswersFile() throws IOException
    {
        String output = String.join("\n", wordleAnswers);
        File file = new File("wordle_answers.txt");
        if (!file.exists())
            throw new RuntimeException("File was deleted!");

        Files.writeString(file.toPath(), output, StandardCharsets.UTF_8);
    }

    private static void setUpTimer()
    {
        Bean.getInstance().getScheduledExecutor().scheduleWithFixedDelay(() -> {
            currentWordleTries.clear();
            finishedUsers.clear();
            wordleAnswers.remove(0);
            currentWord = wordleAnswers.get(0);
            LOG.debug("Midnight! New Wordle Word is {}!", currentWord);
            try
            {
                updateAnswersFile();
            }
            catch (IOException e)
            {
                LOG.error("Error occurred while updating answers file!", e);
            }
        }, getMillisUntilMidnight(), MILLIS_PER_DAY, TimeUnit.MILLISECONDS);
    }

    public static void initialize() throws IOException
    {
        File answersFile = new File("wordle_answers.txt");
        File guessesFile = new File("wordle_guesses.txt");

        if (!answersFile.exists())
        {
            answersFile.createNewFile();
            Response response = Bean.getInstance().getOkHttpClient().newCall(new Request.Builder().url(WORDLE_ANSWERS_URL).build()).execute();
            String responseString = response.body().string();
            List<String> answers = new ArrayList<>(Arrays.stream(responseString.split("\n")).map(String::toUpperCase).toList());
            Collections.shuffle(answers);
            wordleAnswers = answers;
            Files.writeString(answersFile.toPath(), String.join("\n", answers), StandardCharsets.UTF_8);
            response.close();
        }

        if (!guessesFile.exists())
        {
            guessesFile.createNewFile();
            Response response = Bean.getInstance().getOkHttpClient().newCall(new Request.Builder().url(WORDLE_ALLOWED_GUESSES_URL).build()).execute();
            String responseString = response.body().string();
            wordleAllowedGuesses = new ArrayList<>(Arrays.stream(responseString.split("\n")).map(String::toUpperCase).toList());
            Files.writeString(guessesFile.toPath(), responseString, StandardCharsets.UTF_8);
            response.close();
        }

        if (wordleAnswers == null)
            wordleAnswers = new ArrayList<>(Files.readAllLines(answersFile.toPath(), StandardCharsets.UTF_8));

        if (wordleAllowedGuesses == null)
            wordleAllowedGuesses = new ArrayList<>(Files.readAllLines(guessesFile.toPath(), StandardCharsets.UTF_8));

        currentWord = wordleAnswers.get(0);
        LOG.debug("Bot restart! New Wordle Word is {}!", currentWord);
        setUpTimer();
    }

    public static String getCurrentWord()
    {
        return currentWord;
    }

    private static long getMillisUntilMidnight()
    {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis() - now;
    }

    public static String getDiscordRelativeTimeUntilMidnight()
    {
        return TimeFormat.RELATIVE.format(getMillisUntilMidnight() + System.currentTimeMillis());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event)
    {
        if (!event.getComponentId().equals("wordle_next"))
            return;

        if (finishedUsers.contains(event.getUser().getIdLong()))
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("You already played today's wordle! Try again " + getDiscordRelativeTimeUntilMidnight()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (getWordle(event.getUser().getIdLong()) == null)
            createWordleGame(event.getUser().getIdLong());

        TextInput textInput = TextInput.create("word", "Word", TextInputStyle.SHORT)
                .setPlaceholder("Enter word here")
                .setRequired(true)
                .setRequiredRange(5, 5)
                .build();

        Modal modal = Modal.create("wordle", "Wordle").addActionRow(textInput).build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event)
    {
        if (finishedUsers.contains(event.getUser().getIdLong()))
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("You already played today's wordle! Try again " + getDiscordRelativeTimeUntilMidnight()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        Wordle wordle = getWordle(event.getUser().getIdLong());

        if (wordle == null)
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("This wordle is no longer active!")).setEphemeral(true).queue();
            return;
        }

        String word = event.getValue("word").getAsString().toUpperCase(Locale.ROOT);

        if (!wordleAllowedGuesses.contains(word) && !wordleAnswers.contains(word))
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("`" + word + "` is not a valid choice!")).setEphemeral(true).queue();
            return;
        }

        wordle.setTry(word);

        try
        {
            MessageEditCallbackAction action = event.editMessage("").retainFiles().addFile(wordle.generateImage(), "image.png");

            if (wordle.getCurrentTry() == 1)
                action.setActionRow(Button.success("wordle_next", "Try again"));
            else if (wordle.getCurrentTry() == 5 || wordle.hasWon())
                action.setActionRow(Button.success("wordle_next", "Try again").asDisabled());

            if (wordle.hasWon())
            {
                finishedUsers.add(event.getUser().getIdLong());
                incrementWinStreak(event.getUser().getIdLong());
                action.setContent("**Congratulations!**: You won today's wordle quiz! Next quiz will be available " + getDiscordRelativeTimeUntilMidnight() + "\nYour Win-Streak is at **" + getWinStreak(event.getUser().getIdLong()) + "**!");
            }
            else if (!wordle.hasWon() && wordle.getCurrentTry() == 5)
            {
                finishedUsers.add(event.getUser().getIdLong());
                resetWinStreak(event.getUser().getIdLong());
                action.setContent("**You lost**: You lost today's wordle quiz! Next quiz will be available " + getDiscordRelativeTimeUntilMidnight());
            }

            action.queue();
        }
        catch (IOException e)
        {
            event.replyEmbeds(EmbedUtil.errorEmbed("Oops, the image could not be generated! Please try again later!")).setEphemeral(true).queue();
            LOG.error("Could not generate image!", e);
        }
    }

    public static int getWinStreak(long userId)
    {
        try (var rs = new SQLBuilder("SELECT streak FROM wordle_streak WHERE user_id = ?", userId).executeQuery())
        {
            if (rs.next())
                return rs.getInt("streak");
            return 0;
        }
        catch (SQLException exception)
        {
            LOG.error("Could not get wordle winstreak from user {}!", userId, exception);
            return -1;
        }
    }

    public static void incrementWinStreak(long userId)
    {
        int newStreak = getWinStreak(userId) + 1;
        setWinStreak(userId, newStreak);
    }

    public static void resetWinStreak(long userId)
    {
        setWinStreak(userId, 0);
    }

    public static void setWinStreak(long userId, int streak)
    {
        try
        {
            new SQLBuilder("INSERT INTO wordle_streak values (?,?) ON DUPLICATE KEY UPDATE streak = ?", userId, streak, streak).execute();
        }
        catch (SQLException exception)
        {
            LOG.error("Could not set wordle winstreak {} for user {}", streak, userId, exception);
        }
    }
}
