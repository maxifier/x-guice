package com.maxifier.guice.jpa;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.maxifier.guice.jpa.GuiceJPAInspection.*;

/**
 * Created by: Aleksey Didik
 * Date: 3/16/11
 * Time: 4:30 PM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class DBClassesInspection extends AbstractDBInspection {

    @Nonnull
    @Override
    public String getID() {
        return "DBClassesInspection";
    }

    @Override
    public String getAlternativeID() {
        return "DBClassesInspection";
    }

    @Nonnull
    @Override
    public String getGroupDisplayName() {
        return INSPECTIONS_GROUP_NAME;
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "Classes with methods annotated with @DB";
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "db-classes-modifiers";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, boolean isOnTheFly) {
        List<ProblemDescriptor> problemDescriptors = new ArrayList<ProblemDescriptor>();
        PsiElement[] classes = PsiTreeUtil.collectElements(file, new AnnotatedPsiMethodFilter(DB_NAME));
        for (PsiElement psiClass : classes) {
            checkClass((PsiClass) psiClass, manager, problemDescriptors);
        }
        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    private void checkClass(PsiClass psiClass, InspectionManager inspectionManager, List<ProblemDescriptor> problemDescriptors) {
        PsiIdentifier nameIdentifier = psiClass.getNameIdentifier();
        if (nameIdentifier == null) {
            return;
        }
        if (psiClass.isInterface() || psiClass.isEnum() || psiClass.isAnnotationType()) {
            problemDescriptors.add(
                    inspectionManager.createProblemDescriptor(
                            nameIdentifier,
                            "Only classes could have methods with @DB annotation",
                            new DeleteAnnotationFixAction(psiClass),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            true
                    )
            );
        } else {
            checkModifiers(psiClass, inspectionManager, problemDescriptors);
        }
    }

    private void checkModifiers(PsiClass psiClass, InspectionManager inspectionManager, List<ProblemDescriptor> problemDescriptors) {
        PsiModifierList classModifierList = psiClass.getModifierList();
        if (classModifierList != null && classModifierList.hasModifierProperty(FINAL_MODIFIER)) {
            PsiElement finalModifier = getModifier(classModifierList, FINAL_MODIFIER);
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    finalModifier,
                    "Class with methods annotated with @DB should not be final",
                    new DeleteModifierFixAction(finalModifier),
                    ProblemHighlightType.GENERIC_ERROR, true));
        }
        if (classModifierList != null && classModifierList.hasModifierProperty(PRIVATE_MODIFIER)) {
            PsiElement privateModifier = getModifier(classModifierList, PRIVATE_MODIFIER);
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    privateModifier,
                    "Class with methods annotated with @DB should not be private",
                    new DeleteModifierFixAction(privateModifier),
                    ProblemHighlightType.GENERIC_ERROR, true));
        }
    }

    private static class DeleteAnnotationFixAction extends IntentionAndQuickFixAction {

        private final PsiClass psiClass;

        DeleteAnnotationFixAction(PsiClass psiClass) {
            this.psiClass = psiClass;
        }

        @Nonnull
        @Override
        public String getName() {
            return "Delete @DB annotation from methods";
        }

        @Nonnull
        @Override
        public String getFamilyName() {
            return "@DB fix actions";
        }

        @Override
        public void applyFix(Project project, PsiFile psiFile, @Nullable Editor editor) {
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                PsiAnnotation annotation = getAnnotation(psiMethod, DB_NAME);
                if (annotation != null) {
                    annotation.delete();
                }

            }
        }
    }

    private static class AnnotatedPsiMethodFilter implements PsiElementFilter {

        private String name;

        private AnnotatedPsiMethodFilter(String name) {
            this.name = name;
        }

        @Override
        public boolean isAccepted(PsiElement psiElement) {
            if (!(psiElement instanceof PsiClass)) {
                return false;
            }
            PsiClass psiClass = (PsiClass) psiElement;
            for (PsiMethod psiMethod : psiClass.getMethods()) {
                if (getAnnotation(psiMethod, name) != null) {
                    return true;
                }
            }
            return false;
        }
    }
}
