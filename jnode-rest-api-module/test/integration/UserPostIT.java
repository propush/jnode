package integration;

import methods.Echomail;
import methods.User;
import net.minidev.json.JSONObject;
import org.jnode.rest.core.Http;
import org.junit.Test;
import rest.RestResult;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UserPostIT {
    @Test
    public void userPost() throws Exception {

        RestResult loginResult = User.login("2:5020/828.17", "111111");
        String token = (String) loginResult.getPayload().getResult();

        RestResult postResult = Echomail.post(token, "828.local", "субж", "бодя",
               "Kirill Temnenkov", "All++", "2:5020/828.117", "fff", "origggin");

        assertThat(postResult, is(notNullValue()));
        assertThat(postResult.getHttpCode(), is(Http.OK));
        assertThat(postResult.getPayload(), is(notNullValue()));
        assertThat(postResult.getPayload().getResult(), is(instanceOf(Long.class)));

        Long id = (Long) postResult.getPayload().getResult();

        RestResult getResult = Echomail.get(token, id);

        assertThat(getResult, is(notNullValue()));
        assertThat(getResult.getHttpCode(), is(Http.OK));
        assertThat(getResult.getPayload(), is(notNullValue()));
        assertThat(getResult.getPayload().getResult(), is(instanceOf(JSONObject.class)));

        JSONObject msg = (JSONObject) getResult.getPayload().getResult();

        assertThat(msg.get("area"), is(instanceOf(JSONObject.class)));
        assertThat(((JSONObject)msg.get("area")).get("name"), is(equalTo("828.local")));
        assertThat(msg.get("toName"), is(equalTo("All++")));
        assertThat(msg.get("subject"), is(equalTo("субж")));
        assertThat(msg.get("fromName"), is(equalTo("Kirill Temnenkov")));
        assertThat(msg.get("fromFTN"), is(equalTo("2:5020/828.17")));
        assertThat((String)msg.get("text"), is(containsString("бодя")));
        assertThat((String)msg.get("text"), is(containsString("fff")));
        assertThat((String)msg.get("text"), is(containsString("origggin")));

    }
}