package at.xirado.bean.misc;

import java.io.Serializable;

@SuppressWarnings("all")
public class Reaction implements Serializable
{

	private String emote;
	private Long roleid;
	private Boolean custom;
	public Reaction(String emote, Long roleid, Boolean custom)
	{
		this.emote = emote;
		this.roleid = roleid;
		this.custom = custom;
	}
	public String getEmote()
	{
		return emote;
	}
	public Long getRoleID()
	{
		return roleid;
	}
	public boolean isCustom()
	{
		return custom;
	}
}
