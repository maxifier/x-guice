package com.magenta.guice.scope;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.maxifier.guice.scope.UIProvider;
import org.testng.annotations.Test;

import javax.swing.*;

public class UIProviderTest {
    @Test
    public void testGet() throws Exception {
        JFrame frame = new JFrame();
        Provider<JFrame> jFrameProvider = Providers.of(frame);
        UIProvider<JFrame> provider = new UIProvider<JFrame>(jFrameProvider);
        provider.get();
    }
}
