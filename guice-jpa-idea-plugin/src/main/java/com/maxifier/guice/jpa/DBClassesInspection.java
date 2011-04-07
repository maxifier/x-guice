package com.maxifier.guice.jpa;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    @Override
    public String getID() {
        return "DBClassesInspection";
    }

    @Override
    public String getAlternativeID() {
        return "DBClassesInspection";
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return INSPECTIONS_GROUP_NAME;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Classes with methods annotated with @DB";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "db-classes-modifiers";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        List<ProblemDescriptor> problemDescriptors = new ArrayList<ProblemDescriptor>();
        PsiElement[] classes = PsiTreeUtil.collectElements(file, new PsiElementFilter() {
            @Override
            public boolean isAccepted(PsiElement psiElement) {
                if (!(psiElement instanceof PsiClass)) {
                    return false;
                }
                PsiClass psiClass = (PsiClass) psiElement;
                for (PsiMethod psiMethod : psiClass.getMethods()) {
                    if (getAnnotation(psiMethod, DB_NAME) != null) {
                        return true;
                    }
                }
                return false;
            }
        });
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
            //checkInject(psiClass, inspectionManager, problemDescriptors);
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

    private void checkInject(PsiClass psiClass, InspectionManager inspectionManager, List<ProblemDescriptor> problemDescriptors) {
        boolean haveInject = false;
        for (PsiMethod psiMethod : psiClass.getConstructors()) {
            if (getAnnotation(psiMethod, INJECT_NAME) != null || isEmptyConstructor(psiMethod)) {
                haveInject = true;
                break;
            }
        }
        if (!haveInject) {
            //noinspection ConstantConditions
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    psiClass.getNameIdentifier(),
                    "Class with methods annotated with @DB should be created by Injector" +
                            " and have either one constructor annotated with @Inject" +
                            " or empty non-private constructor.",
                    true,
                    LocalQuickFix.EMPTY_ARRAY,
                    ProblemHighlightType.GENERIC_ERROR));

        }
    }

    private boolean isEmptyConstructor(PsiMethod psiMethod) {
        return !psiMethod.getModifierList().hasModifierProperty(PRIVATE_MODIFIER)
                &&
                psiMethod.getParameterList().getParametersCount() == 0;
    }

    private static class DeleteAnnotationFixAction extends IntentionAndQuickFixAction {

        private final PsiClass psiClass;

        DeleteAnnotationFixAction(PsiClass psiClass) {
            this.psiClass = psiClass;
        }

        @NotNull
        @Override
        public String getName() {
            return "Delete @DB annotation from methods";
        }

        @NotNull
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

}
