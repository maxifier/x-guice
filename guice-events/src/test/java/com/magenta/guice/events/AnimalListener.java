package com.magenta.guice.events;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.06.2009
 * Time: 10:25:42
 */
public interface AnimalListener {
    @Handler
    void animal(Animal e);

    @Handler
    @AnimalHandler({Animal.RABBIT /*, Animal.DOG  */})
    void eatableAnimal(Animal e);

    @Handler
    @AnimalHandler({Animal.CROCODILE, Animal.TIGER})
    void dangerousAnimal(Animal e);
}
