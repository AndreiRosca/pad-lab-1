package md.utm.pad.labs.broker;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import md.utm.pad.labs.broker.subscriber.SubscriberTest;

@RunWith(Suite.class)
@SuiteClasses({
	MessageTest.class,
	ClientHandlerImplTest.class,
	BrokerContextTest.class,
	SubscriberTest.class
})
public class UnitTests {

}
