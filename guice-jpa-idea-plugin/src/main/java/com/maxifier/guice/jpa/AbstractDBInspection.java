package com.maxifier.guice.jpa;

import com.intellij.codeInsight.daemon.HighlightDisplayKey;
import com.intellij.codeInspection.CustomSuppressableInspectionTool;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.SuppressIntentionAction;
import com.intellij.codeInspection.SuppressManager;
import com.intellij.psi.PsiElement;

/**
 * Created by: Aleksey Didik
 * Date: 3/28/11
 * Time: 7:47 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public abstract class AbstractDBInspection extends LocalInspectionTool implements CustomSuppressableInspectionTool {

    public SuppressIntentionAction[] getSuppressActions(final PsiElement element) {
        return SuppressManager.getInstance().createSuppressActions(HighlightDisplayKey.find(getShortName()));
    }

    public boolean isSuppressedFor(final PsiElement element) {
        return SuppressManager.getInstance().isSuppressedFor(element, getID()) || SuppressManager.getInstance().isSuppressedFor(element, getAlternativeID());
    }
}
