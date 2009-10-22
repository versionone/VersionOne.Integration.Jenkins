package com.versionone.integration.ciCommon;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rozhnev
 * Date: 22.10.2009
 * Time: 14:31:15
 * To change this template use File | Settings | File Templates.
 */
public interface VcsModification {

	String getUserName();

	String getComment();

	Date getDate();

	String getId();
}
