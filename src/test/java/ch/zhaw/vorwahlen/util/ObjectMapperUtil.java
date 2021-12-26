package ch.zhaw.vorwahlen.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

public class ObjectMapperUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static byte[] toJson(Object object) throws Exception {
        return mapper.writeValueAsString(object).getBytes();
    }

    public static <T> T fromJsonResult(MvcResult result, Class<T> clazz) throws Exception {
        return mapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public static <T> T fromJsonResult(MvcResult result, TypeReference<T> typeReference) throws Exception {
        return mapper.readValue(result.getResponse().getContentAsString(), typeReference);
    }
}
