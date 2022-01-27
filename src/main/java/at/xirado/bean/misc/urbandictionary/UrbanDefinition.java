package at.xirado.bean.misc.urbandictionary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UrbanDefinition {

    private String word;
    private String definition;
    private String example;
    private String author;
    @JsonProperty("written_on")
    private String writtenOn;
    private String permalink;
    private int defid;

    @JsonIgnore
    private String[] sound_urls;
    @JsonIgnore
    private String current_vote;

    @JsonProperty("thumbs_up")
    private int upvotes;
    @JsonProperty("thumbs_down")
    private int downvotes;

    /**
     * Initialize an instance of a Definition object after parsing the UrbanDictionary JSON interface.
     *
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
    public UrbanDefinition(String wordName, String definition, String example, String author, String writtenDate, String permaLink, int refID, int likes, int dislikes) {
        this.word = wordName;
        this.definition = definition;
        this.example = example;
        this.author = author;
        this.writtenOn = writtenDate;
        this.permalink = permaLink;
        this.defid = refID;
        this.upvotes = likes;
        this.downvotes = dislikes;
    }

    public UrbanDefinition() {

    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setWrittenOn(String writtenOn) {
        this.writtenOn = writtenOn;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public void setDefid(int defid) {
        this.defid = defid;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Returns a CSV-formatted String containing the information of the definition in the following order:
     * Name, Definition, Example, Author, Date Written (ISO-8601), Definition ID#, # Likes, # Dislikes.
     *
     * @return a comma-delimited String.
     */
    @Override
    public String toString() {
        return "\"" + word + "\",\"" + definition + "\",\"" + example + "\",\"" + author + "\",\"" + writtenOn + "\"," + defid + "," + upvotes + "," + downvotes;
    }

    /**
     * Returns a String URL permalink to the definition the class entails.
     *
     * @return a shortened permalink to the UrbanDictionary definition of the word based on reference ID.
     */
    public String getPermalink() {
        return permalink;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public String getExample() {
        return example;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * Returns the date that the post was made.
     *
     * @return the date of the Definition in ISO-8601 format.
     */
    public String getWrittenOn() {
        return writtenOn;
    }

    public int getDefid() {
        return defid;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public Object getSound_urls() {
        return sound_urls;
    }
}
