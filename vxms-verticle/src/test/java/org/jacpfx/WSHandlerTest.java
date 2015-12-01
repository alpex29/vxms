package org.jacpfx;

import org.jacpfx.common.util.Serializer;
import org.jacpfx.entity.MyTestObject;
import org.jacpfx.entity.decoder.ExampleByteDecoderMyTest;
import org.jacpfx.vertx.websocket.response.WebSocketHandler;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class WSHandlerTest

{
    @Test
    public void testPayloadDecoding() throws IOException {
        MyTestObject input = new MyTestObject("andy","M");
        byte[] b =Serializer.serialize(new MyTestObject("andy","M"));
        WebSocketHandler handler = new WebSocketHandler(null,null,b,null, null);

        Optional<MyTestObject> output = handler.payload().getObject(MyTestObject.class,new ExampleByteDecoderMyTest());
        assertTrue(input.equals(output.get()));
        System.out.println("sdsdf");
    }


}
