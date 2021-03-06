/* 
 * Copyright 2011 Vancouver Ywebb Consulting Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * --------
 * This class contains modified sources from gwtmobile-ui project. 
 * 
 * Copyright (c) 2010 Zhihua (Dennis) Jiang
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package next.i.mobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import next.i.mobile.DragEvent.Type;
import next.i.util.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class DragController implements EventListener {

	private List<DragEventsHandler> _dragEventHandlers = new ArrayList<DragEventsHandler>();
	private List<SwipeEventsHandler> _swipeEventHandlers = new ArrayList<SwipeEventsHandler>();
	protected DragEventsHandler _capturingDragEventsHandler = null;
	protected SwipeEventsHandler _capturingSwipeEventsHandler = null;
	protected Widget _source;
	private boolean _isDown = false;
	private boolean _suppressNextClick = false;
	private long _lastDragTimeStamp = 0;
	private Point _lastDragPos = new Point(0, 0);
	private long _currDragTimeStamp = 0;
	private Point _currDragPos = new Point(0, 0);

	private JavaScriptObject _clickListener;
	protected JavaScriptObject _dragStartListener;
	protected JavaScriptObject _dragMoveListener;
	protected JavaScriptObject _dragEndListener;

	private Point _startDragPos = new Point(0, 0);
	// private boolean _hasMoveStarted = false;
	private DragEvent.Type movedirection;

	private int movecounter = 0;

	protected static DragController INSTANCE = GWT.create(DragController.class);

	DragController() {
		init();
	}

	public static DragController get() {
		return INSTANCE;
	}

	protected void init() {
		_source = RootLayoutPanel.get();
		registerEvents();
	}

	public void addDragEventsHandler(DragEventsHandler dragHandler) {
		_dragEventHandlers.add(dragHandler);
	}

	public void addSwipeEventsHandler(SwipeEventsHandler swipeHandler) {
		_swipeEventHandlers.add(swipeHandler);
	}

	public void removeDragEventsHandler(DragEventsHandler dragHandler) {
		_dragEventHandlers.remove(dragHandler);
	}

	public void removeSwipeEventHandler(SwipeEventsHandler swipeHandler) {
		_swipeEventHandlers.remove(swipeHandler);
	}

	@Override
	public void onBrowserEvent(Event e) {
		String type = e.getType();
		if (type.equals("click")) {
			onClick(e);
		}
	}

	private void onClick(Event e) {
		if (_suppressNextClick) {
			e.stopPropagation();
			_suppressNextClick = false;
			// XLog.info("click suppressed");
		}
	}

	// TODO: May need an onPreStart event to indicate that mouse is down, but no
	// movement yet,
	// so onStart event can actually mean drag is indeed started.
	protected void onStart(Event e, Point p) {
		_isDown = true;
		_suppressNextClick = false;
		Date currentDateTime = new Date();
		_lastDragTimeStamp = currentDateTime.getTime();
		_currDragTimeStamp = _lastDragTimeStamp;
		_lastDragPos.clone(p);
		_currDragPos.clone(p);
		_startDragPos.clone(p);

		// XLog.info("onStart curr=" + (int) p.X() + " : " + (int) p.Y());

		DragEvent dragEvent = new DragEvent(e, DragEvent.Type.Start, p.X(), p.Y(), p.X() - _currDragPos.X(), p.Y()
				- _currDragPos.Y());
		fireDragEvent(dragEvent);
	}

	protected void onMove(Event e, Point p) {
		if (_isDown) {
			if (p.equals(_currDragPos)) {
				// XLog.info("NO movement onMove");
				return;
			}
			_suppressNextClick = true;

			if (movedirection == null && movecounter > 0) {
				double vertDelta = Math.abs(_startDragPos.Y() - p.Y());
				double horizDelta = Math.abs(_startDragPos.X() - p.X());

				if (vertDelta > horizDelta) {
					movedirection = Type.MoveVertical;
				} else {
					movedirection = Type.MoveHorizontal;
				}
			}

			if (movedirection == null) {
				movecounter++;
			} else {
				DragEvent dragEvent = new DragEvent(e, movedirection, p.X(), p.Y(), p.X() - _currDragPos.X(), p.Y()
						- _currDragPos.Y());
				fireDragEvent(dragEvent);
				// XLog.info("moveDirection !!!! " + movedirection + " vertDelta=" +
				// vertDelta + " horizDelta=" +horizDelta);
			}

			// XLog.info("onMove _lastDragPos=" + (int) _lastDragPos.X() + " : " +
			// (int) _lastDragPos.Y() + " curr="
			// + (int) p.X() + " : " + (int) p.Y());

			DragEvent dragEvent = new DragEvent(e, Type.Move, p.X(), p.Y(), p.X() - _currDragPos.X(), p.Y()
					- _currDragPos.Y());
			fireDragEvent(dragEvent);
			_lastDragPos.clone(_currDragPos);
			_lastDragTimeStamp = _currDragTimeStamp;
			_currDragPos.clone(p);
			Date currentDateTime = new Date();
			_currDragTimeStamp = currentDateTime.getTime();
		}
	}

	protected void onEnd(Event e, Point p) {
		movedirection = null;
		movecounter = 0;

		if (_isDown) {
			_isDown = false;
			DragEvent dragEvent = new DragEvent(e, DragEvent.Type.End, p.X(), p.Y(), p.X() - _currDragPos.X(), p.Y()
					- _currDragPos.Y());
			fireDragEvent(dragEvent);
			double distanceX = p.X() - _lastDragPos.X();
			double distanceY = p.Y() - _lastDragPos.Y();
			double distance;
			SwipeEvent.Type swipeType;
			if (Math.abs(distanceX) > Math.abs(distanceY)) {
				distance = distanceX;
				swipeType = distance > 0 ? SwipeEvent.Type.HorizontalLeftRight : SwipeEvent.Type.HorizontalRightLeft;
			} else {
				distance = distanceY;
				swipeType = distance > 0 ? SwipeEvent.Type.VerticalTopBottom : SwipeEvent.Type.VerticalBottomTop;
			}
			Date currentDateTime = new Date();
			long time = currentDateTime.getTime() - _lastDragTimeStamp;
			double speed = distance / time;
			if (speed > 4) {
				speed = 4;
			} else if (speed < -4) {
				speed = -4;
			}
			// XLog.info("onEnd, speed is " + speed);

			if (Math.abs(speed) > 0.2) {
				// XLog.info("onEnd, before swipeEvent .... speed is " + speed);
				SwipeEvent swipeEvent = new SwipeEvent(e, swipeType, speed);
				fireSwipeEvent(swipeEvent);
			}
		}
	}

	protected void fireDragEvent(DragEvent e) {
		if (_capturingDragEventsHandler != null) {
			e.dispatch(_capturingDragEventsHandler);
			return;
		}
		EventTarget target = e.getNativeEvent().getEventTarget();
		Node node = Node.as(target);
		if (!Element.is(node)) {
			node = node.getParentNode(); // Text node
		}
		if (Element.is(node)) {
			Element ele = Element.as(target);
			int count = 0;
			while (ele != null) {
				for (DragEventsHandler handler : _dragEventHandlers) {
					if (ele.equals(handler.getElement())) {
						e.dispatch(handler);
						count++;
						if (e.getStopPropagation() || count == _dragEventHandlers.size()) {
							return;
						}
					}
				}
				ele = ele.getParentElement();
			}
		}
	}

	protected void fireSwipeEvent(SwipeEvent e) {
		if (_capturingSwipeEventsHandler != null) {
			e.dispatch(_capturingSwipeEventsHandler);
			return;
		}
		if (_capturingDragEventsHandler != null) {
			return;
		}
		EventTarget target = e.getNativeEvent().getEventTarget();
		Node node = Node.as(target);
		if (!Element.is(node)) {
			// Text node
			node = node.getParentNode();
		}
		if (Element.is(node)) {
			Element ele = Element.as(target);
			int count = 0;
			while (ele != null) {
				for (SwipeEventsHandler handler : _swipeEventHandlers) {
					if (ele.equals(handler.getElement())) {
						e.dispatch(handler);
						count++;
						if (e.getStopPropagation() || count == _swipeEventHandlers.size()) {
							return;
						}
					}
				}
				ele = ele.getParentElement();
			}
		}
	}

	public void suppressNextClick() {
		_suppressNextClick = true;
	}

	protected void registerEvents() {
		if (_clickListener == null) {
			_clickListener = Utils.addEventListener(_source.getElement(), "click", true, this);
		}
	}

	protected void unregisterEvents() {
		if (_clickListener != null) {
			Utils.removeEventListener(_source.getElement(), "click", true, _clickListener);
			_clickListener = null;
		}
	}

	public void suspend() {
		unregisterEvents();
		// XLog.info("drag events suspended.");
	}

	public void resume() {
		registerEvents();
		// XLog.info("drag events resumed.");
	}

	public boolean captureDragEvents(DragEventsHandler cachingHandler) {
		if (_capturingDragEventsHandler != null) {
			return false;
		}
		_capturingDragEventsHandler = cachingHandler;
		return true;
	}

	public boolean releaseDragCapture(DragEventsHandler cachingHandler) {
		if (_capturingDragEventsHandler == null) {
			return true;
		}
		if (_capturingDragEventsHandler != cachingHandler) {
			return false;
		}
		_capturingDragEventsHandler = null;
		return true;
	}

	public boolean captureSwipeEvents(SwipeEventsHandler cachingHandler) {
		if (_capturingSwipeEventsHandler != null) {
			return false;
		}
		_capturingSwipeEventsHandler = cachingHandler;
		return true;
	}

	public boolean releaseSwipeCapture(SwipeEventsHandler cachingHandler) {
		if (_capturingSwipeEventsHandler == null) {
			return true;
		}
		if (_capturingSwipeEventsHandler != cachingHandler) {
			return false;
		}
		_capturingSwipeEventsHandler = null;
		return true;
	}

}
