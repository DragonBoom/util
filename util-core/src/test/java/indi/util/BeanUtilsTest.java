package indi.util;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.data.RestResult;

@ExtendWith(TestSeparateExtension.class)
class BeanUtilsTest {

	@Test
	@Disabled
	void createMapTest() {
		RestResult<String> success = RestResult.asSuccess("ff");
		
		Map<String, Object> map = BeanUtils.createMap(success, true);
		
		System.out.println(map);
	}
	
	@Test
	@Disabled
	void copyPropertiesTest() {
	    RestResult<String> result1 = RestResult.asSuccess("hello");
	    RestResult<String> result2 = RestResult.asSuccess("bad");
	    BeanUtils.copyProperties(result1, result2).copy("content");
	    
	    Assertions.assertEquals("hello", result2.getContent());
	    System.out.println(result2.getContent());
	}
	
	@Test
	void cleanupTest() {
	    RestResult<String> result1 = RestResult.asSuccess("hello");
	    BeanUtils.cleanup(result1);
	    System.out.println(result1);
	}

}
