package com.magenta.guice.bootstrap.xml;

/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

import com.google.inject.Inject;
import com.magenta.guice.property.Property;

public class InImpl implements In {
    private final String property;

    @Inject(optional = true)
    @Property("my.pet.weight")
    private double weight;

    @Inject
    public InImpl(@Property("test") String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    public double getWeight() {
        return weight;
    }
}
