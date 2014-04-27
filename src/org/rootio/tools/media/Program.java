package org.rootio.tools.media;

import java.util.ArrayList;
import java.util.Date;

import org.rootio.tools.persistence.DBAgent;
import org.rootio.tools.radio.EventTime;
import org.rootio.tools.utils.Utils;

import android.content.ContentValues;
import android.content.Context;

public class Program {

	private String title;
	private PlayList playList;
	private ProgramType programType;
	private String tag;
	private ArrayList<EventTime> eventTimes;
	private int scheduledIndex;
	private Long id, cloudId;
	private Context parent;

	public Program(Context parent, long cloudId, String title, int programTypeId) {
		this.parent = parent;
		this.setProgramType(programTypeId);
		this.cloudId = cloudId;
		this.title = title;
		this.id = Utils.getProgramId(this.parent, title, cloudId);
		if (this.id <= 0) {
			this.id = this.persist();
		}
		this.loadEventTimes(this.id);
		this.setTag();
		this.createPlayList();
	}
	
	public Program(Context parent, long cloudId, String title, int programTypeId, String tag) {
		this.parent = parent;
		this.tag = tag;
		this.setProgramType(programTypeId);
		this.cloudId = cloudId;
		this.title = title;
		this.id = Utils.getProgramId(this.parent, title, this.cloudId);
		if (this.id == null) {
			this.id = this.persist();
		}
		this.loadEventTimes(this.id);
		this.createPlayList();
	}
	
	public Program(Context  parent, long cloudId)
	{
		this.parent = parent;
		this.cloudId = cloudId;
		this.loadProgramInfo();
	}
	
	/**
	 * Sets the tag with which the show is associated
	 */
	private void setTag()
	{
		if(this.programType == ProgramType.Music)
		{
			this.tag = this.getTag();
		}
		
		else if (this.programType == ProgramType.Media)
		{
			EpisodeManager episodeManager = new EpisodeManager(this.parent, this);
			this.tag = episodeManager.getEpisodeTag();
		}
	}
	
	/**
	 * Sets the program type for the specified program
	 * @param programTypeId The ID of the program whose ID to set
	 */
	private void setProgramType(int programTypeId)
	{
		switch(programTypeId)
		{
		case 1:
			this.programType = ProgramType.Media;
			break;
		case 2: 
			this.programType = ProgramType.Call;
			break;
		case 3:
			this.programType = ProgramType.Music;
			break;
		case 4:
			this.programType = ProgramType.Stream;
			break;
		}
	}
	
	/**
	 * Initializes the playlist for this program
	 */
	private void createPlayList()
	{
			this.playList = new PlayList(this.parent, this.tag, this.programType);
	}

	/**
	 * Get the playlist associated with this Program
	 * @return PlayList object of this program's playlist
	 */
	public PlayList getPlayList() {
		return this.playList;
	}

	/**
	 * Get the program type of this program
	 * @return ProgramType object of this program's type
	 */
	public ProgramType getProgramType() {
		return this.programType;
	}

	/**
	 * Returns the title of this program
	 * @return String representation of the title of this program
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Gets the ID for this program
	 * @return long representation of the ID of this program
	 */
	public long getId()
	{
		return this.id;
	}

	public void run() {
		if (this.programType == ProgramType.Call) {
			//sit and wait for incoming phone calls. The telephony service will handle the calls
			} 
		else if(this.programType == ProgramType.Stream)
			{
			  
			}
			else {
			}
			playList.load();
			new JingleManager(this.parent, this).playJingle();
		}
	
	/**
	 * Called upon on completion of the jingle to play the content for the program
	 */
	void onJinglePlayFinish()
	{
		playList.play();
	}
	
	/**
	 * pauses this program
	 */
	public void pause()
	{
		playList.pause();
	}
	
	/**
	 * resumes this program if it was previously paused
	 */
	public void resume()
	{
		playList.resume();
	}

	/**
	 * Stops this program
	 */
	public void stop() {
		playList.stop();
	}
	
	

	/**
	 * Return the EventTime objects associated with this program
	 * 
	 * @return Array of EventTime objects
	 */
	public EventTime[] getEventTimes() {
		return this.eventTimes.toArray(new EventTime[this.eventTimes.size()]);
	}
	
	/**
	 * Gets the index of the event time for this program that is playing
	 * @return Index of the event time for this program that is currently playing
	 */
	public int getScheduledIndex()
	{
		return this.scheduledIndex;
	}
	
	/**
	 * Sets the index of the event time for this program that is playing
	 * @param scheduledIndex The index of the event time that is playing
	 */
	public void setScheduledIndex(int scheduledIndex)
	{
		this.scheduledIndex = scheduledIndex;
	}
	
	/**
	 * Gets the Tag associated with this program
	 * @return The tag for this program
	 */
	private String getTag()
	{
		String tableName = "program";
		String[] columns = new String[]{"tag"};
		String whereClause = "id = ?";
		String[] whereArgs = new String[]{String.valueOf(this.id)};
		DBAgent dbAgent = new DBAgent(this.parent);
		String[][] results = dbAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
		return results.length > 0 ? results[0][0] : null;
	}
	
	/**
	 * Fetches the event times for which this program is scheduled
	 * @param programId The ID of the program whose event times to fetch
	 */
	private void loadEventTimes(long programId)
	{
		this.eventTimes = new ArrayList<EventTime>();
	    String tableName = "eventtime";
	    String[] columns = new String[]{"programid", "scheduledate", "duration", "isrepeat"};
	    String whereClause = "programid = ?" ;
	    String[] whereArgs = new String[]{String.valueOf(programId)};
	    DBAgent dbAgent = new DBAgent(this.parent);
	    String[][] results = dbAgent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
	    for(String[] result : results)
	    {
	    	long eventTimeProgramId = Utils.parseLongFromString(result[0]);
	    	Date eventTimeScheduleDate = Utils.getDateFromString(result[1],"yyyy-MM-dd HH:mm:ss");
	    	int duration = Utils.parseIntFromString(result[2]);
	    	boolean isRepeat = false; //result[3].equals("1");
	    	this.eventTimes.add(new EventTime(this.parent, eventTimeProgramId, eventTimeScheduleDate, duration, isRepeat ));
	    }
	}
	
	/**
	 * Save this Program to the Rootio Database in case it is not yet persisted
	 * 
	 * @return Long id of the row stored in the Rootio database
	 */
	private Long persist() {
		String tableName = "program";
		ContentValues data = new ContentValues();
		data.put("title", this.title);
		data.put("programtypeid", this.programType.ordinal());
		data.put("cloudid", this.cloudId);
		DBAgent agent = new DBAgent(this.parent);
		return agent.saveData(tableName, null, data);
	}
	
	private void loadProgramInfo()
	{
		String tableName = "program";
		String[] columns = new String[]{"id","title","tag"};
		String whereClause = "cloudid = ?";
		String[] whereArgs = new String[]{String.valueOf(this.cloudId)};
		DBAgent agent = new DBAgent(this.parent);
		String[][] result = agent.getData(true, tableName, columns, whereClause, whereArgs, null, null, null, null);
		if(result.length > 0)
		{
			this.title = result[0][1];
			this.tag = result[0][2];
			this.id = Utils.parseLongFromString(result[0][0]);
		}
	}
}
