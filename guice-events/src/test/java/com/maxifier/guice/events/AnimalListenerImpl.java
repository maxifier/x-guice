package com.maxifier.guice.events;

import com.google.inject.Singleton;

@Singleton
public class AnimalListenerImpl implements AnimalListener {
    @Override
    public void animal(Animal e) {
        System.out.println("I see " + e + ".");
    }

    @Override
    public void eatableAnimal(Animal e) {
        System.out.println("Well, I see " + e + " and I'm hungry. What about hunting?");
    }

    @Override
    public void dangerousAnimal(Animal e) {
        System.out.println("Oh no! It's " + e + "! Let's call 911!");
    }
}
