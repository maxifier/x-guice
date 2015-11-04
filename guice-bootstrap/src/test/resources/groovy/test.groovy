import com.google.inject.Scopes
import com.google.inject.name.Names
import com.maxifier.guice.override.OverrideModule
import com.maxifier.guice.bootstrap.xml.*

install(new FooModule())
bind(TestInterface).to(First)
bind(TestInterface).annotatedWith(TestAnnotation).to(Second)
bind(Alone).asEagerSingleton()
bindConstant().annotatedWith(Constant).to('Hello world!')
bind(AsEager).asEagerSingleton()
binder.bind(In).to(InImpl).in(Scopes.SINGLETON)
bindConstant().annotatedWith(Names.named('test.name')).to('Hello world!')

def petWeight = 5.01
if (System.getProperty("os.name").toLowerCase().contains("mac")) {
    petWeight = 6.54;
}
if (client == 'forbes') {
    petWeight = 523.23
}
def properties = [:]
properties.'test' = 'testValue'
properties.'my.pet.weight' = petWeight
bindProperties(properties)

install(new OverrideModule() {

    @Override
    protected void configure() {
        override(TestInterface).to(Second)
    }
})

