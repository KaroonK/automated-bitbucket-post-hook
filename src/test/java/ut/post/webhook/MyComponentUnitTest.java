package ut.post.webhook;

import org.junit.Test;
import post.webhook.api.MyPluginComponent;
import post.webhook.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;

public class MyComponentUnitTest
{
	@Ignore
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}