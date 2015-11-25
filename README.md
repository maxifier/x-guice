# eXtensions for Guice

Well, what is x-guice? It is a bunch of extensions to the google Guice library which helps us to deal with it.
You can use them all together or one by one.

### Guice Bootstrap

Helps to create application Injector from set of modules declared in property files. We use this approach to flexibly configure application for different environments without rebuilding application.

Sample:
```
# config.properties
mainModules=c.m.Module1, c.m.Module2
extraModules=c.m.Ext1, c.m.Ext2
```
```java
Injector injector = new InjectorBuilder()
  .withConfiguration(load("config.properties"))
  .withModuleBundle("mainModules")
  .withModuleBundle("extraModules")
  .buildApplicationInjector();
```

### Guice JPA

Annotation-driven `EntityManager` handling. Just annotate your method with `@DB` and interceptor will open and close connection for you and start transaction if needed. There is some basic database dead lock processing available: interceptor can retry method call in case of `SQLTransientException` automatically.

### Guice Property

Handles application configuration using `@Property` annotated constants. Module-specific configuration and extra TypeConvertes available.

### Guice Events

Introduce `EventDispatcher` service which dispatches events (POJO classes) to methods marked as `@Handle`. Some sophisticated filters available.

### Guice MBean

Lookup for `@MBean` annotated services in container and registers them as MBeans.

### Guice Livecycle

Provides `@PostConstruct` and `@ShutdownSafe` processing.

### Guice Scopes

Provides some extra scopes:
- `@LazySingleton` truly lazy singleton disregarding current `Stage`.
- `@ThreadLocalScope` classical thread-local scope implementation.
- `@UISingleton` singleton which initialized in Swing EventDispatchThread thread context.

### Guice Decorator

Gives ability to decorate service using other service.

### License
The MIT License (MIT)

Copyright (c) 2015 Maxifier Inc
