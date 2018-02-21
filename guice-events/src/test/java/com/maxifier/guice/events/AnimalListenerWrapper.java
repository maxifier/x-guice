package com.maxifier.guice.events;

import com.google.inject.Singleton;

import static org.mockito.Mockito.mock;

@Singleton
public class AnimalListenerWrapper implements AnimalListener {
    final AnimalListener tracker;

    public AnimalListenerWrapper() {
        this.tracker = mock(AnimalListener.class);
    }

    public AnimalListenerWrapper(AnimalListener tracker) {
        this.tracker = tracker;
    }

    @Override
    public void animal(Animal e) {
        tracker.animal(e);
        System.out.println("I see " + e + ".");
    }

    @Override
    public void eatableAnimal(Animal e) {
        tracker.eatableAnimal(e);
        System.out.println("Well, I see " + e + " and I'm hungry. What about hunting?");
    }

    @Override
    public void dangerousAnimal(Animal e) {
        tracker.dangerousAnimal(e);
        System.out.println("Oh no! It's " + e + "! Let's call 911!");
    }
}
