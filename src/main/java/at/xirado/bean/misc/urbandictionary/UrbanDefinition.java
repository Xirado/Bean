package at.xirado.bean.misc.urbandictionary;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

public class UrbanDefinition implements SerializableData
{
    private final String word;
    private final String definition;
    private final String example;
    private final String author;
    private final String writtenOn;
    private final String permalink;
    private final int definitionId;
    private final int upvotes;
    private final int downvotes;

    private UrbanDefinition(String word, String definition, String example,
                            String author, String writtenOn, String permaLink,
                            int definitionId, int upvotes, int downvotes)
    {
        this.word = word;
        this.definition = definition;
        this.example = example;
        this.author = author;
        this.writtenOn = writtenOn;
        this.permalink = permaLink;
        this.definitionId = definitionId;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public String getWord()
    {
        return word;
    }

    public String getDefinition()
    {
        return definition;
    }

    public String getExample()
    {
        return example;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getWrittenOn()
    {
        return writtenOn;
    }

    public String getPermalink()
    {
        return permalink;
    }

    public int getDefinitionId()
    {
        return definitionId;
    }

    public int getUpvotes()
    {
        return upvotes;
    }

    public int getDownvotes()
    {
        return downvotes;
    }

    public static UrbanDefinition fromData(DataObject data)
    {
        return new UrbanDefinition(
                data.getString("word"),
                data.getString("definition"),
                data.getString("example"),
                data.getString("author"),
                data.getString("written_on"),
                data.getString("permalink"),
                data.getInt("defid"),
                data.getInt("thumbs_up"),
                data.getInt("thumbs_down")
        );
    }

    @NotNull
    @Override
    public DataObject toData()
    {
        return DataObject.empty()
                .put("word", word)
                .put("definition", definition)
                .put("example", example)
                .put("author", author)
                .put("written_on", writtenOn)
                .put("permalink", permalink)
                .put("defid", definitionId)
                .put("thumbs_up", upvotes)
                .put("thumbs_down", downvotes);
    }
}
