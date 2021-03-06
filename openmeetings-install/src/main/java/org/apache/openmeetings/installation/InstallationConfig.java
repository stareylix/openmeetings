/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.installation;

import static org.apache.openmeetings.util.OpenmeetingsVariables.DEFAULT_APP_NAME;
import static org.apache.openmeetings.util.OpenmeetingsVariables.DEFAULT_BASE_URL;

import java.io.Serializable;

import org.apache.openmeetings.util.crypt.SCryptImplementation;

public class InstallationConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	public String appName = DEFAULT_APP_NAME;
	public String username;
	private String password;
	public String email;
	public String group;
	public boolean allowFrontendRegister = true;
	public boolean createDefaultRooms = true;
	public String ical_timeZone = "Europe/Berlin";

	public String cryptClassName = SCryptImplementation.class.getCanonicalName();
	//email
	public Integer smtpPort = 25;
	public String smtpServer = "localhost";
	public String mailAuthName = "";
	public String mailAuthPass = "";
	public String mailReferer = "noreply@openmeetings.apache.org";
	public boolean mailUseTls = false;
	//paths
	public Integer docDpi = 150;
	public Integer docQuality = 90;
	public String imageMagicPath = "";
	public String ffmpegPath = "";
	public String soxPath = "";
	public String officePath = "";

	public String defaultLangId = "1";
	public boolean sendEmailAtRegister = false;
	public String urlFeed = "http://mail-archives.apache.org/mod_mbox/openmeetings-user/?format=atom";
	public String urlFeed2 = "http://mail-archives.apache.org/mod_mbox/openmeetings-dev/?format=atom";
	public String sendEmailWithVerficationCode = "false";
	public boolean sipEnable = false;
	public String sipRoomPrefix = "400";
	public String sipExtenContext = "rooms";
	public boolean replyToOrganizer = true;
	public String baseUrl = DEFAULT_BASE_URL;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "InstallationConfig [allowFrontendRegister="
				+ allowFrontendRegister + ", createDefaultRooms="
				+ createDefaultRooms + ", cryptClassName=" + cryptClassName
				+ ", smtpPort=" + smtpPort + ", smtpServer=" + smtpServer
				+ ", mailAuthName=" + mailAuthName + ", mailAuthPass="
				+ mailAuthPass + ", mailReferer=" + mailReferer
				+ ", mailUseTls=" + mailUseTls + ", docDpi=" + docDpi
				+ ", docQuality=" + docQuality
				+ ", imageMagicPath=" + imageMagicPath + ", ffmpegPath="
				+ ffmpegPath + ", soxPath=" + soxPath
				+ ", defaultLangId=" + defaultLangId + ", sendEmailAtRegister="
				+ sendEmailAtRegister + ", urlFeed=" + urlFeed + ", urlFeed2="
				+ urlFeed2 + ", sendEmailWithVerficationCode="
				+ sendEmailWithVerficationCode + ", sipEnable="
				+ sipEnable + ", sipRoomPrefix=" + sipRoomPrefix
				+ ", sipExtenContext=" + sipExtenContext
				+ ", replyToOrganizer=" + replyToOrganizer
				+ ", ical_timeZone=" + ical_timeZone
				+ "]";
	}
}
