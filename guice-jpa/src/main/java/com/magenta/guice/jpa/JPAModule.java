package com.magenta.guice.jpa;

import com.google.inject.AbstractModule;

import javax.persistence.EntityManager;
/*
* Project: Maxifier
* Author: Aleksey Didik
* Created: 23.05.2008 10:19:35
* 
* Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
* Magenta Technology proprietary and confidential.
* Use is subject to license terms.
*/

public class JPAModule extends AbstractModule {
    @Override
    protected void configure() {
        DBInterceptor.bind(binder());
        bind(EntityManager.class).toProvider(EntityManagerProvider.class);
    }
}
