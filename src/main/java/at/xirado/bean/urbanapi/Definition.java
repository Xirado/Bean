package at.xirado.bean.urbanapi;

public class Definition
{
    private String wordName;
    private String definition;
    private String example;
    private String author;
    private String writtenDate;
    private String permaLink;

    private int refID;

    private int likes;
    private int dislikes;

    /**
     * Initialize an instance of a Definition object after parsing the UrbanDictionary JSON interface.
     * @param wordName
     * @param definition
     * @param example
     * @param author
     * @param writtenDate
     * @param permaLink
     * @param refID
     * @param likes
     * @param dislikes
     */
    public Definition(String wordName, String definition, String example, String author, String writtenDate, String permaLink, int refID, int likes, int dislikes)
    {
        this.wordName = wordName;
        this.definition = definition;
        this.example = example;
        this.author = author;
        this.writtenDate = writtenDate;
        this.permaLink = permaLink;
        this.refID = refID;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    /**
     * Returns a CSV-formatted String containing the information of the definition in the following order:
     * Name, Definition, Example, Author, Date Written (ISO-8601), Definition ID#, # Likes, # Dislikes.
     *
     * @return a comma-delimited String.
     */
    @Override
    public String toString()
    {
        return "\"" + wordName + "\",\"" + definition + "\",\"" + example + "\",\"" + author + "\",\"" + writtenDate + "\"," + refID + "," + likes + "," + dislikes;
    }

    /**
     * Returns a String URL permalink to the definition the class entails.
     * @return a shortened permalink to the UrbanDictionary definition of the word based on reference ID.
     */
    public String getPermalink()
    {
        return permaLink;
    }

    public String getWordName()
    {
        return wordName;
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

    /**
     * Returns the date that the post was made.
     * @return the date of the Definition in ISO-8601 format.
     */
    public String getWrittenDate()
    {
        return writtenDate;
    }

    public int getRefID()
    {
        return refID;
    }

    public int getLikes()
    {
        return likes;
    }

    public int getDislikes()
    {
        return dislikes;
    }

}
