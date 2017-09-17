package md.utm.pad.labs.broker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	MessageTest.class,
	ClientHandlerImplTest.class,
	BrokerContextTest.class
})
public class UnitTests {

}
