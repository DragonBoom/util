package indi.util;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import indi.bean.BeanUtils;
import indi.data.Result;
import indi.data.Results;
import indi.data.Wrapper.BooleanWrapper;
import indi.test.TestSeparateExtension;

@ExtendWith(TestSeparateExtension.class)
class BeanUtilsTest {

	@Test
	@Disabled
	void createMapTest() {
		Result<String> success = Results.success("ff");
		
		Map<String, Object> map = BeanUtils.createMap(success, true);
		
		System.out.println(map);
	}
	
	Result<String> result1 = Results.success("hello");
	Result<String> result2 = Results.error("bad");
	
	private void showResultBrother() {
	    System.out.println(new StringBuilder("--\n").append(result1).append("\n   ").append(result2).toString());
	}
	
	@Test
	@Disabled
	void copyPropertiesTest() {
	    BeanUtils.copySelectedProperties(result1, result2).copy("content");
	    
	    Assertions.assertEquals("hello", result2.getContent());
	    showResultBrother();
	}
	
	@Test
	@Disabled
	void cleanupTest() {
	    Result<String> result1 = Results.success("hello");
	    BeanUtils.cleanup(result1);
	    System.out.println(result1);
	}
	
	@Test
//	@Disabled
	void copyAllPropertiesTest() {
	    showResultBrother();
	    BeanUtils.copyProperties(result1, result2);
	    showResultBrother();
	    Assertions.assertEquals(result1, result2);
	}
	
	@Test
	@Disabled
	void setPropertyTest() {
	    BooleanWrapper wrapper = BooleanWrapper.of(false);
	    Assertions.assertDoesNotThrow(() -> {
	        BeanUtils.setProperty(wrapper, "value", true);
	        Assertions.assertTrue(wrapper.get());
	    });
	}

}
