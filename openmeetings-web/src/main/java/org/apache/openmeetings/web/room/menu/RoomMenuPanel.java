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
package org.apache.openmeetings.web.room.menu;

import static org.apache.openmeetings.util.OmFileHelper.EXTENSION_JPG;
import static org.apache.openmeetings.util.OmFileHelper.EXTENSION_PDF;
import static org.apache.openmeetings.util.OmFileHelper.EXTENSION_PNG;
import static org.apache.openmeetings.util.OpenmeetingsVariables.CONFIG_APPLICATION_BASE_URL;
import static org.apache.openmeetings.util.OpenmeetingsVariables.CONFIG_REDIRECT_URL_FOR_EXTERNAL;
import static org.apache.openmeetings.web.app.Application.exitRoom;
import static org.apache.openmeetings.web.app.Application.getBean;
import static org.apache.openmeetings.web.app.Application.getClientBySid;
import static org.apache.openmeetings.web.app.WebSession.getUserId;
import static org.apache.openmeetings.web.util.GroupLogoResourceReference.getUrl;
import static org.apache.openmeetings.web.util.OmUrlFragment.ROOMS_PUBLIC;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.openmeetings.core.util.WebSocketHelper;
import org.apache.openmeetings.db.dao.basic.ConfigurationDao;
import org.apache.openmeetings.db.dao.room.PollDao;
import org.apache.openmeetings.db.entity.basic.Client;
import org.apache.openmeetings.db.entity.room.Room;
import org.apache.openmeetings.db.entity.room.Room.RoomElement;
import org.apache.openmeetings.db.entity.room.RoomPoll;
import org.apache.openmeetings.db.entity.user.Group;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.util.message.RoomMessage.Type;
import org.apache.openmeetings.util.message.TextRoomMessage;
import org.apache.openmeetings.web.app.Application;
import org.apache.openmeetings.web.app.WebSession;
import org.apache.openmeetings.web.common.ImagePanel;
import org.apache.openmeetings.web.common.InvitationDialog;
import org.apache.openmeetings.web.common.OmButton;
import org.apache.openmeetings.web.common.menu.MenuPanel;
import org.apache.openmeetings.web.common.menu.RoomMenuItem;
import org.apache.openmeetings.web.room.OmRedirectTimerBehavior;
import org.apache.openmeetings.web.room.RoomPanel;
import org.apache.openmeetings.web.room.poll.CreatePollDialog;
import org.apache.openmeetings.web.room.poll.PollResultsDialog;
import org.apache.openmeetings.web.room.poll.VoteDialog;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.util.string.Strings;

import com.googlecode.wicket.jquery.ui.widget.menu.IMenuItem;

public class RoomMenuPanel extends Panel {
	private static final long serialVersionUID = 1L;
	private final InvitationDialog invite;
	private final CreatePollDialog createPoll;
	private final VoteDialog vote;
	private final PollResultsDialog pollResults;
	private final SipDialerDialog sipDialer;
	private final MenuPanel menuPanel;
	private final StartSharingButton shareBtn;
	private final Label roomName;
	private static final FastDateFormat df = FastDateFormat.getInstance("dd.MM.yyyy HH:mm");
	private final OmButton askBtn = new OmButton("ask") {
		private static final long serialVersionUID = 1L;
		{
			setOutputMarkupPlaceholderTag(true);
			setVisible(false);
		}
		@Override
		public void onClick(AjaxRequestTarget target) {
			Client c = room.getClient();
			WebSocketHelper.sendRoom(new TextRoomMessage(c.getRoom().getId(), c.getUserId(), Type.haveQuestion, c.getUid()));
		}
	};
	private final RoomPanel room;
	private final RoomMenuItem exitMenuItem = new RoomMenuItem(Application.getString("308"), Application.getString("309"), "room menu exit") {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			exit(target);
		}
	};
	private final RoomMenuItem filesMenu = new RoomMenuItem(Application.getString("245"), null, false);
	private final RoomMenuItem actionsMenu = new RoomMenuItem(Application.getString("635"), null, false);
	private final RoomMenuItem inviteMenuItem = new RoomMenuItem(Application.getString("213"), Application.getString("1489"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			invite.updateModel(target);
			invite.open(target);
		}
	};
	private final RoomMenuItem shareMenuItem = new RoomMenuItem(Application.getString("239"), Application.getString("1480"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			shareBtn.onClick(target);
		}
	};
	private final RoomMenuItem applyModerMenuItem = new RoomMenuItem(Application.getString("784"), Application.getString("1481"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			room.requestRight(Room.Right.moderator, target);
		}
	};
	private final RoomMenuItem applyWbMenuItem = new RoomMenuItem(Application.getString("785"), Application.getString("1492"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			room.requestRight(Room.Right.whiteBoard, target);
		}
	};
	private final RoomMenuItem applyAvMenuItem = new RoomMenuItem(Application.getString("786"), Application.getString("1482"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			room.requestRight(Room.Right.video, target);
		}
	};
	private final RoomMenuItem pollCreateMenuItem = new RoomMenuItem(Application.getString("24"), Application.getString("1483"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			createPoll.updateModel(target);
			createPoll.open(target);
		}
	};
	private final RoomMenuItem pollVoteMenuItem = new RoomMenuItem(Application.getString("32"), Application.getString("1485"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			RoomPoll rp = getBean(PollDao.class).getByRoom(room.getRoom().getId());
			if (rp != null) {
				vote.updateModel(target, rp);
				vote.open(target);
			}
		}
	};
	private final RoomMenuItem pollResultMenuItem = new RoomMenuItem(Application.getString("37"), Application.getString("1484"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			pollResults.updateModel(target, room.getClient().hasRight(Room.Right.moderator));
			pollResults.open(target);
		}
	};
	private final RoomMenuItem sipDialerMenuItem = new RoomMenuItem(Application.getString("1447"), Application.getString("1488"), false) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			sipDialer.open(target);
		}
	};
	private final RoomMenuItem downloadPngMenuItem = new RoomMenuItem(Application.getString("download.png"), Application.getString("download.png")) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			target.appendJavaScript(String.format("WbArea.download('%s');", EXTENSION_PNG));
		}
	};
	private final RoomMenuItem downloadJpgMenuItem = new RoomMenuItem(Application.getString("download.jpg"), Application.getString("download.jpg")) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			target.appendJavaScript(String.format("WbArea.download('%s');", EXTENSION_JPG));
		}
	};
	private final RoomMenuItem downloadPdfMenuItem = new RoomMenuItem(Application.getString("download.pdf"), Application.getString("download.pdf")) {
		private static final long serialVersionUID = 1L;

		@Override
		public void onClick(AjaxRequestTarget target) {
			target.appendJavaScript(String.format("WbArea.download('%s');", EXTENSION_PDF));
		}
	};
	private final ImagePanel logo = new ImagePanel("logo") {
		private static final long serialVersionUID = 1L;

		@Override
		protected String getImageUrl() {
			return getUrl(getRequestCycle(), getGroup().getId());
		}
	};

	private Group getGroup() {
		Room r = room.getRoom();
		return r.getGroups() == null || r.getGroups().isEmpty()
				? new Group()
				: r.getGroups().get(0).getGroup();
	}

	public RoomMenuPanel(String id, final RoomPanel room) {
		super(id);
		setOutputMarkupPlaceholderTag(true);
		this.room = room;
		Room r = room.getRoom();
		setVisible(!r.isHidden(RoomElement.TopBar));
		add((menuPanel = new MenuPanel("menu", getMenu())).setVisible(isVisible()));
		add((roomName = new Label("roomName", r.getName())).setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true));
		String tag = getGroup().getTag();
		add(logo, new Label("tag", tag).setVisible(!Strings.isEmpty(tag)));
		add((shareBtn = new StartSharingButton("share", room.getUid()))
				.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true));
		RoomInvitationForm rif = new RoomInvitationForm("form", room.getRoom().getId());
		add(invite = new InvitationDialog("invite", rif));
		rif.setDialog(invite);
		add(createPoll = new CreatePollDialog("createPoll", room.getRoom().getId()));
		add(vote = new VoteDialog("vote"));
		add(pollResults = new PollResultsDialog("pollResults", room.getRoom().getId()));
		add(sipDialer = new SipDialerDialog("sipDialer", room));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(askBtn.add(new AttributeAppender("title", getString("84"))));
		Label demo = new Label("demo", Model.of(""));
		Room r = room.getRoom();
		add(demo.setVisible(r.isDemoRoom() && r.getDemoTime() != null && room.getRoom().getDemoTime().intValue() > 0));
		if (demo.isVisible()) {
			demo.add(new OmRedirectTimerBehavior(room.getRoom().getDemoTime().intValue(), "637") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onTimer(int remain) {
					getComponent().add(AttributeModifier.replace("title", getText("639", remain)));
				}

				@Override
				protected void onFinish(AjaxRequestTarget target) {
					exit(target);
				}
			});
		}
	}

	private List<IMenuItem> getMenu() {
		List<IMenuItem> menu = new ArrayList<>();
		exitMenuItem.setEnabled(false);
		menu.add(exitMenuItem.setTop(true));

		filesMenu.getItems().add(new RoomMenuItem(Application.getString("15"), Application.getString("1479")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				room.getSidebar().showUpload(target);
			}
		});
		menu.add(filesMenu.setTop(true));

		actionsMenu.setTop(true);
		actionsMenu.getItems().add(inviteMenuItem);
		actionsMenu.getItems().add(shareMenuItem); //FIXME enable/disable
		actionsMenu.getItems().add(applyModerMenuItem); //FIXME enable/disable
		actionsMenu.getItems().add(applyWbMenuItem); //FIXME enable/disable
		actionsMenu.getItems().add(applyAvMenuItem); //FIXME enable/disable
		actionsMenu.getItems().add(pollCreateMenuItem);
		actionsMenu.getItems().add(pollResultMenuItem); //FIXME enable/disable
		actionsMenu.getItems().add(pollVoteMenuItem); //FIXME enable/disable
		actionsMenu.getItems().add(sipDialerMenuItem);
		actionsMenu.getItems().add(downloadPngMenuItem);
		actionsMenu.getItems().add(downloadJpgMenuItem);
		actionsMenu.getItems().add(downloadPdfMenuItem);
		//TODO seems need to be removed actionsMenu.getItems().add(new RoomMenuItem(Application.getString(1126), Application.getString(1490)));
		menu.add(actionsMenu);
		return menu;
	}

	public void update(IPartialPageRequestHandler handler) {
		if (!isVisible()) {
			return;
		}
		Room r = room.getRoom();
		boolean isInterview = Room.Type.interview == r.getType();
		downloadPngMenuItem.setEnabled(!isInterview);
		downloadJpgMenuItem.setEnabled(!isInterview);
		downloadPdfMenuItem.setEnabled(!isInterview);
		PollDao pollDao = getBean(PollDao.class);
		boolean pollExists = pollDao.hasPoll(r.getId());
		User u = room.getClient().getUser();
		boolean notExternalUser = u.getType() != User.Type.external && u.getType() != User.Type.contact;
		exitMenuItem.setEnabled(notExternalUser);//TODO check this
		filesMenu.setEnabled(!isInterview && room.getSidebar().isShowFiles());
		boolean moder = room.getClient().hasRight(Room.Right.moderator);
		actionsMenu.setEnabled((moder && !r.isHidden(RoomElement.ActionMenu)) || (!moder && r.isAllowUserQuestions()));
		inviteMenuItem.setEnabled(notExternalUser && moder);
		//TODO add check "sharing started"
		boolean shareVisible = room.screenShareAllowed();
		shareMenuItem.setEnabled(shareVisible);
		applyModerMenuItem.setEnabled(!moder);
		applyWbMenuItem.setEnabled(!room.getClient().hasRight(Room.Right.whiteBoard));
		applyAvMenuItem.setEnabled(!room.getClient().hasRight(Room.Right.audio) || !room.getClient().hasRight(Room.Right.video));
		pollCreateMenuItem.setEnabled(moder);
		pollVoteMenuItem.setEnabled(pollExists && notExternalUser && !pollDao.hasVoted(r.getId(), getUserId()));
		pollResultMenuItem.setEnabled(pollExists || pollDao.getArchived(r.getId()).size() > 0);
		sipDialerMenuItem.setEnabled(r.isSipEnabled() && getBean(ConfigurationDao.class).isSipEnabled());
		//TODO sip menus
		menuPanel.update(handler);
		StringBuilder roomClass = new StringBuilder("room name");
		StringBuilder roomTitle = new StringBuilder();
		if (room.getRecordingUser() != null) {
			Client recClient = getClientBySid(room.getRecordingUser());
			if (recClient != null) {
				roomTitle.append(String.format("%s %s %s %s %s", getString("419")
						, recClient.getUser().getLogin(), recClient.getUser().getFirstname(), recClient.getUser().getLastname(), df.format(recClient.getConnectedSince())));
				//FIXME TODO get ConnectedSince of StreamClient
				roomClass.append(" screen");
			}
			Client pubClient = getClientBySid(room.getPublishingUser());
			if (pubClient != null) {
				if (recClient != null) {
					roomTitle.append('\n');
				}
				roomTitle.append(String.format("%s %s %s %s %s", getString("1504")
						, pubClient.getUser().getLogin(), pubClient.getUser().getFirstname(), pubClient.getUser().getLastname(), "URL")); //TODO add URL
				roomClass.append(" screen");
			}
		}
		handler.add(roomName.add(AttributeModifier.replace("class", roomClass), AttributeModifier.replace("title", roomTitle)));
		handler.add(askBtn.setVisible(!moder && r.isAllowUserQuestions()));
		handler.add(shareBtn.setVisible(shareVisible));
	}

	public void updatePoll(IPartialPageRequestHandler handler, Long createdBy) {
		RoomPoll rp = getBean(PollDao.class).getByRoom(room.getRoom().getId());
		if (rp != null) {
			vote.updateModel(handler, rp);
		} else {
			vote.close(handler, null);
		}
		if (createdBy != null && !getUserId().equals(createdBy)) {
			vote.open(handler);
		}
		if (pollResults.isOpened()) {
			pollResults.updateModel(handler, room.getClient().hasRight(Room.Right.moderator));
		}
		update(handler);
	}

	public void exit(IPartialPageRequestHandler handler) {
		if (WebSession.getRights().contains(User.Right.Dashboard)) {
			exitRoom(room.getClient());
			room.getMainPanel().updateContents(ROOMS_PUBLIC, handler);
		} else {
			String url = getBean(ConfigurationDao.class).getString(CONFIG_REDIRECT_URL_FOR_EXTERNAL, "");
			if (Strings.isEmpty(url)) {
				url = getBean(ConfigurationDao.class).getString(CONFIG_APPLICATION_BASE_URL, "");
			}
			throw new RedirectToUrlException(url);
		}
	}
}
