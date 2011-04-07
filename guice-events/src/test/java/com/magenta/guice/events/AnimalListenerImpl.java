package com.magenta.guice.events;

import com.google.inject.Singleton;

@Singleton
public class AnimalListenerImpl implements AnimalListener {
    @Override
    public void animal(Animal e) {
        // этот обработчик обрабатывает всех увиденных животных.
        System.out.println("I see " + e + ".");
    }

    @Override
    public void eatableAnimal(Animal e) {
        // а этот - только съедобных
        System.out.println("Well, I see " + e + " and I'm hungry. What about hunting?");
    }

    @Override
    public void dangerousAnimal(Animal e) {
        // а этот - только опасных (крокодила и тигра)
        System.out.println("Oh no! It's " + e + "! Let's call 911!");
    }
}
