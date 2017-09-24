package md.utm.pad.labs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import md.utm.pad.labs.broker.BrokerContextTest;
import md.utm.pad.labs.broker.ClientHandlerImplTest;
import md.utm.pad.labs.broker.MessageTest;
import md.utm.pad.labs.broker.ServerTest;
import md.utm.pad.labs.broker.executor.RequestExecutorFactoryTest;
import md.utm.pad.labs.broker.subscriber.SubscriberTest;

@RunWith(Suite.class)
@SuiteClasses({
	MessageTest.class,
	ClientHandlerImplTest.class,
	BrokerContextTest.class,
	SubscriberTest.class,
	ServerTest.class,
	RequestExecutorFactoryTest.class
})
public class UnitTests {

}
