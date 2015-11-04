package com.maxifier.guice.jpa;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by: Aleksey Didik
 * Date: 3/16/11
 * Time: 10:58 AM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class GuiceJPAInspection implements InspectionToolProvider {

    public static final String INSPECTIONS_GROUP_NAME = "Guice JPA inspections";
    public static final String PRIVATE_MODIFIER = "private";
    public static final String ABSTRACT_MODIFIER = "abstract";
    public static final String FINAL_MODIFIER = "final";
    public static final String STATIC_MODIFIER = "static";
    public static final String INJECT_NAME = "com.google.inject.Inject";
    public static final String DB_NAME = "com.maxifier.guice.jpa.DB";


    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{DBMethodModifiersInspection.class, DBClassesInspection.class, EntityManagerInspection.class};
    }

    static PsiAnnotation getAnnotation(PsiMethod psiMethod, String name) {
        return psiMethod.getModifierList().findAnnotation(name);
    }

    static PsiElement getModifier(PsiModifierList psiModifierList, String modifier) {
        return PsiUtil.findModifierInList(psiModifierList, modifier);
    }

    static class DeleteModifierFixAction extends IntentionAndQuickFixAction {

        private final PsiElement modifier;

        DeleteModifierFixAction(PsiElement modifier) {
            this.modifier = modifier;
        }

        @NotNull
        @Override
        public String getName() {
            return "Delete disallowed modifier";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "@DB fix actions";
        }

        @Override
        public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
            modifier.delete();
        }
    }

    static class AddModifierFixAction extends IntentionAndQuickFixAction {

        private final PsiModifierList modifierList;
        private final String modifier;

        AddModifierFixAction(PsiModifierList modifierList, String modifier) {
            this.modifierList = modifierList;
            this.modifier = modifier;
        }

        @NotNull
        @Override
        public String getName() {
            return "Add required modifier";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return "@DB fix actions";
        }

        @Override
        public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
            modifierList.setModifierProperty(modifier, true);
        }
    }
}
