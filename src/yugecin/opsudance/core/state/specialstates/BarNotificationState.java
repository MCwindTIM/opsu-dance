/*
 * opsu!dance - fork of opsu! with cursordance auto
 * Copyright (C) 2017 yugecin
 *
 * opsu!dance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu!dance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!dance.  If not, see <http://www.gnu.org/licenses/>.
 */
package yugecin.opsudance.core.state.specialstates;

import itdelatrisu.opsu.ui.Fonts;
import itdelatrisu.opsu.ui.animations.AnimationEquation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import yugecin.opsudance.core.DisplayContainer;
import yugecin.opsudance.core.events.EventBus;
import yugecin.opsudance.core.events.EventListener;
import yugecin.opsudance.events.BarNotificationEvent;
import yugecin.opsudance.events.ResolutionChangedEvent;

import java.util.List;

public class BarNotificationState implements EventListener<BarNotificationEvent> {

	private final int IN_TIME = 200;
	private final int DISPLAY_TIME = 5700 + IN_TIME;
	private final int OUT_TIME = 200;
	private final int TOTAL_TIME = DISPLAY_TIME + OUT_TIME;

	private final DisplayContainer displayContainer;
	private final Color bgcol;
	private final Color textCol;

	private int timeShown;

	private String message;
	private List<String> lines;
	private int textY;

	private int barHalfTargetHeight;
	private int barHalfHeight;

	public BarNotificationState(DisplayContainer displayContainer, EventBus eventBus) {
		this.displayContainer = displayContainer;
		this.bgcol = new Color(Color.black);
		this.textCol = new Color(Color.white);
		this.timeShown = TOTAL_TIME;
		eventBus.subscribe(BarNotificationEvent.class, this);
		eventBus.subscribe(ResolutionChangedEvent.class, new EventListener<ResolutionChangedEvent>() {
			@Override
			public void onEvent(ResolutionChangedEvent event) {
				if (timeShown >= TOTAL_TIME) {
					return;
				}
				calculatePosition();
			}
		});
	}

	public void render(Graphics g, int delta) {
		if (timeShown >= TOTAL_TIME) {
			return;
		}
		timeShown += delta;
		processAnimations();
		g.setColor(bgcol);
		g.fillRect(0, displayContainer.height / 2 - barHalfHeight, displayContainer.width, barHalfHeight * 2);
		int y = textY;
		for (String line : lines) {
			Fonts.LARGE.drawString((displayContainer.width - Fonts.LARGE.getWidth(line)) / 2, y, line, textCol);
			y += Fonts.LARGE.getLineHeight();
		}
	}

	private void processAnimations() {
		if (timeShown < IN_TIME) {
			float progress = (float) timeShown / IN_TIME;
			barHalfHeight = (int) (barHalfTargetHeight * AnimationEquation.OUT_BACK.calc(progress));
			textCol.a = progress;
			bgcol.a = 0.4f * progress;
			return;
		}
		if (timeShown > DISPLAY_TIME) {
			float progress = 1f - (float) (timeShown - DISPLAY_TIME) / OUT_TIME;
			textCol.a = progress;
			bgcol.a = 0.4f * progress;
			return;
		}
		barHalfHeight = barHalfTargetHeight;
		textCol.a = 1f;
		bgcol.a = 0.4f;
	}

	private void calculatePosition() {
		this.lines = Fonts.wrap(Fonts.LARGE, message, (int) (displayContainer.width * 0.96f), true);
		int textHeight = (int) (Fonts.LARGE.getLineHeight() * (lines.size() + 0.5f));
		textY = (displayContainer.height - textHeight) / 2 + Fonts.LARGE.getLineHeight() / 8;
		barHalfTargetHeight = textHeight / 2 + Fonts.LARGE.getLineHeight() / 8;
	}

	@Override
	public void onEvent(BarNotificationEvent event) {
		this.message = event.message;
		calculatePosition();
		timeShown = 0;
	}

}
