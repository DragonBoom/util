package indi.core.rest.result;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

class RestResultTest {

    @Test
    void test() throws JsonParseException, JsonMappingException, IOException {
        RestResult<?> fromCommonJson = RestResult.fromCommonJson("{\"ww\":\"ff\"}", RestResult.CommonTypeRef.Map);
        System.out.println(fromCommonJson);
    }

}
