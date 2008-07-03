package hudson.plugins.twitter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Assert;
import org.junit.Test;

public class MiscTest {

    @Test
    public void testJSONCleaning() {
        String jsonString = "{\"default\":\"(Default)\",\"blank\":\"\",\"normal\":\"normal\",\"array\":[\"foo\",\"bar\"],\"object\":{\"foo\":\"bar\"}}";
        JSONObject obj = JSONObject.fromObject(jsonString);

        JSONObject cleaned = TwitterPublisher.DescriptorImpl.cleanJSON(obj);
        Assert.assertTrue(cleaned.containsKey("normal"));
        Assert.assertFalse(cleaned.containsKey("blank"));
        Assert.assertFalse(cleaned.containsKey("default"));
        Assert.assertTrue(cleaned.containsKey("array"));
        Assert.assertTrue(cleaned.containsKey("object"));
        Assert.assertEquals(String.class, cleaned.get("normal").getClass());
        Assert.assertEquals(JSONArray.class, cleaned.get("array").getClass());
        Assert.assertEquals(JSONObject.class, cleaned.get("object").getClass());
    }

}
