package com.magenta.guice.scope;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.testng.annotations.Test;

import javax.swing.*;/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class UIProviderTest {
    @Test
    public void testGet() throws Exception {
        JFrame frame = new JFrame();
        Provider<JFrame> jFrameProvider = Providers.of(frame);
        UIProvider<JFrame> provider = new UIProvider<JFrame>(jFrameProvider);
        provider.get();
    }
}
