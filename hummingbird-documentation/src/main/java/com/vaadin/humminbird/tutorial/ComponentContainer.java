/*
 * Copyright 2000-2016 Vaadin Ltd.
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
package com.vaadin.humminbird.tutorial;

import com.vaadin.annotations.Tag;
import com.vaadin.humminbird.tutorial.annotations.CodeFor;
import com.vaadin.hummingbird.dom.Element;
import com.vaadin.hummingbird.dom.ElementFactory;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

@CodeFor("tutorial-component-container.asciidoc")
public class ComponentContainer {

    @Tag("div")
    // @formatter:off
    public class MyComponentContainer extends Component implements HasComponents {
    // @formatter:on
    }

    public class Bar {
        @Tag("div")
        public class MyComponentContainer extends Component {

            public void add(Component child) {
                getElement().appendChild(child.getElement());
            }
        }

    }

    public class Foo {
        @Tag("div")
        public class MyComponentContainer extends Component {

            public void add(Component child) {
                Element childWrapper = ElementFactory.createDiv();
                childWrapper.appendChild(child.getElement());
                getElement().appendChild(childWrapper);
            }

            public void remove(Component child) {
                Element wrapper = child.getElement().getParent();
                wrapper.removeFromParent();
            }
        }

    }
}
