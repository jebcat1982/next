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
package next.i.util;

import next.i.XLog;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.RootLayoutPanel;

public class Utils {

	public static native JavaScriptObject addEventListener(Element ele, String event, boolean capture,
			EventListener listener) /*-{
		var callBack = function(e) {
			listener.@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)(e);
		};
		ele.addEventListener(event, callBack, capture);
		return callBack;
	}-*/;

//	public static native void addEventListenerOnce(Element ele, String event, boolean capture, EventListener listener) /*-{
//		var callBack = function(e) {
//			ele.removeEventListener(event, callBack, capture);
//			listener.@com.google.gwt.user.client.EventListener::onBrowserEvent(Lcom/google/gwt/user/client/Event;)(e);
//		};
//		ele.addEventListener(event, callBack, capture);
//	}-*/;

	public static native void removeEventListener(Element ele, String event, boolean capture, JavaScriptObject callBack) /*-{
		ele.removeEventListener(event, callBack, capture);
	}-*/;

	private static Element htmlNode = DOM.createElement("DIV");

	public static String unescapeHTML(String html) {
		htmlNode.setInnerHTML(html);
		return htmlNode.getInnerText();
	}

	// The url loaded by this method can be intercepted by
	// WebViewClient.shouldOverrideUrlLoading
	public static void loadUrl(String url) {
		// Window.open(url, "_tab", "");
		Anchor a = new Anchor("", url, "_tab");
		RootLayoutPanel.get().add(a);
		NativeEvent event = Document.get().createClickEvent(1, 1, 1, 1, 1, false, false, false, false);
		a.getElement().dispatchEvent(event);
		RootLayoutPanel.get().remove(a);
	}

	public static boolean isHtmlFormControl(com.google.gwt.dom.client.Element ele) {
		if (ele == null) {
			return false;
		}
		String nodeName = ele.getNodeName().toUpperCase();
		return "BUTTON INPUT SELECT TEXTAREA".contains(nodeName) || isHtmlFormControl(ele.getParentElement());
	}

	public native static Element getActiveElement() /*-{
		return $doc.activeElement;
	}-*/;

	public static boolean isWVGA() {
		return Document.get().getDocumentElement().getClassName().contains("WVGA");
	}


	public static int getTargetItemIndex(Element parent, EventTarget target) {
		Element div = Element.as(target);
		if (div == parent) {
			XLog.info("Is click on list working? " + target.toString());
			return -1;
		}
		while (div.getParentElement() != parent) {
			div = div.getParentElement();
			if (div == null) {
				return -1;
			}
		}
		int index = DOM
				.getChildIndex((com.google.gwt.user.client.Element) parent, (com.google.gwt.user.client.Element) div);
		return index;
	}

	public static boolean isAndroid() {
		return Window.Navigator.getUserAgent().contains("Android");
	}

	public static boolean isIOS() {
		return Window.Navigator.getUserAgent().contains("iPhone") || Window.Navigator.getUserAgent().contains("iPod")
				|| Window.Navigator.getUserAgent().contains("iPad");
	}

}
