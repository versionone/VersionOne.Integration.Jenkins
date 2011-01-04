package com.versionone.hudson;

import hudson.plugins.git.GitChangeSet;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.versionone.integration.ciCommon.VcsModification;

public class GitModification implements VcsModification {

	private GitChangeSet _entry;
	
	public GitModification(GitChangeSet entry) {
		_entry = entry;
	}
	
	public String getComment() {
		return _entry.getComment();
	}

	public Date getDate()  {
		try {
			return DateFormat.getDateInstance().parse(_entry.getDate());
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return Calendar.getInstance().getTime();
	}

	public String getId() {
		return _entry.getId();
	}

	public String getUserName() {
		return _entry.getAuthorName();
	}

}
