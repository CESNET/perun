/*
 * Copyright 2011 Google Inc.
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

package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.impl.ImageResourcePrototype;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Given an {@link ImageResource}, renders an element to show it.
 */
public class ImageResourceAltRenderer {

  private static final Template TEMPLATE = GWT.create(Template.class);

  public SafeHtml render(ImageResource image, String alt) {
    if (image instanceof ImageResourcePrototype.Bundle) {
      return AbstractImagePrototype.create(image).getSafeHtml();
    } else {
      return TEMPLATE.image(image.getSafeUri(), image.getWidth(), image.getHeight(), alt);
    }
  }

  interface Template extends SafeHtmlTemplates {
    @SafeHtmlTemplates.Template("<img src='{0}' border='0' width='{1}' height='{2}' title='{3}'>")
    SafeHtml image(SafeUri imageUri, int width, int height, String alt);
  }

}
