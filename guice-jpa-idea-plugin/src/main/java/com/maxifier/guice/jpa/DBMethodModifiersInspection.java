package com.maxifier.guice.jpa;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.maxifier.guice.jpa.GuiceJPAInspection.*;

/**
 * Created by: Aleksey Didik
 * Date: 3/16/11
 * Time: 11:20 AM
 * <p/>
 * Copyright (c) 1999-2011 Maxifier Ltd. All Rights Reserved.
 * Code proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author Aleksey Didik
 */
public class DBMethodModifiersInspection extends AbstractDBInspection {


    @NotNull
    @Override
    public String getID() {
        return "DBMethodModifiersInspection";
    }

    @Override
    public String getAlternativeID() {
        return "DBMethodModifiersInspection";
    }

    @NotNull
    @Override
    public String getGroupDisplayName() {
        return INSPECTIONS_GROUP_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "@DB annotated methods modifiers";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "db-methods-modifiers";
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
    public ProblemDescriptor[] checkFile(@NotNull PsiFile psiFile,
                                         @NotNull InspectionManager inspectionManager,
                                         boolean b) {
        List<ProblemDescriptor> problemDescriptors = new ArrayList<ProblemDescriptor>();
        PsiElement[] methods = PsiTreeUtil.collectElements(psiFile, new PsiElementFilter() {
            @Override
            public boolean isAccepted(PsiElement psiElement) {
                if (psiElement instanceof PsiMethod) {
                    PsiMethod psiMethod = (PsiMethod) psiElement;
                    return getAnnotation(psiMethod, DB_NAME) != null;
                }
                return false;
            }
        });

        for (PsiElement method : methods) {
            checkMethod((PsiMethod) method, inspectionManager, problemDescriptors);
        }
        return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
    }

    private void checkMethod(PsiMethod method, InspectionManager inspectionManager, List<ProblemDescriptor> problemDescriptors) {
        PsiModifierList modifierList = method.getModifierList();
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null || containingClass.isInterface() || containingClass.isEnum() || containingClass.isAnnotationType()) {
            return;
        }
        if (modifierList.hasModifierProperty(PRIVATE_MODIFIER)) {
            PsiElement privateModifier = getModifier(modifierList, PRIVATE_MODIFIER);
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    privateModifier,
                    "Method annotated with @DB should not be private", new DeleteModifierFixAction(privateModifier),
                    ProblemHighlightType.GENERIC_ERROR, true));
        }
        if (modifierList.hasModifierProperty(ABSTRACT_MODIFIER)) {
            PsiElement abstractModifier = getModifier(modifierList, ABSTRACT_MODIFIER);
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    abstractModifier,
                    "Method annotated with @DB should not  be abstract", new DeleteModifierFixAction(abstractModifier),
                    ProblemHighlightType.GENERIC_ERROR, true));
        }
        if (modifierList.hasModifierProperty(STATIC_MODIFIER)) {
            PsiElement abstractModifier = getModifier(modifierList, STATIC_MODIFIER);
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    abstractModifier,
                    "Method annotated with @DB should not  be static", new DeleteModifierFixAction(abstractModifier),
                    ProblemHighlightType.GENERIC_ERROR, true));
        }
        if (modifierList.hasModifierProperty(FINAL_MODIFIER)) {
            PsiElement finalModifier = getModifier(modifierList, FINAL_MODIFIER);
            problemDescriptors.add(inspectionManager.createProblemDescriptor(
                    finalModifier,
                    "Method annotated with @DB should not  be final", new DeleteModifierFixAction(finalModifier),
                    ProblemHighlightType.GENERIC_ERROR, true));
        }
    }
}
