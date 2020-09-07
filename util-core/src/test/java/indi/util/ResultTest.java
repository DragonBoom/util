package indi.util;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import indi.data.Result;
import indi.data.Results;

class ResultTest {

    @Test
    void test() throws JsonProcessingException {
        Result<String> result = Results.success("hello");
        System.out.println(result.toJson());
    }

}
