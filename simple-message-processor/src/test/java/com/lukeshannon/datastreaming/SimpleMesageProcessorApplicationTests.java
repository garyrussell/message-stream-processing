package com.lukeshannon.datastreaming;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleMesageProcessorApplicationTests {
	
	@Autowired
    private Process process;

	@Test
	public void contextLoads() {
	}	

    @Test
    public void simpleTest() {
        String result = process.process("I'm Pickle Riiiick!");
        assertNotNull(result);
        assertNotNull(result.contains("routingKey"));
    }


}
