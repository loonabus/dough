package bus.loona;

import bus.loona.common.BaseResCode;
import bus.loona.config.AutoConfig;
import bus.loona.config.BaseConfig;
import bus.loona.domain.ServiceReq;
import bus.loona.domain.ServiceRes;
import bus.loona.domain.base.BaseRes;
import bus.loona.domain.vo.User;
import bus.loona.handler.AwsStsHandler;
import bus.loona.service.RedisService;
import bus.loona.service.UserService;
import bus.loona.web.TestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see TestControllerTest
 * started on 2017-05-?? ~
 */

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers= TestController.class)
@Import({BaseConfig.JsonViewConfig.class, AutoConfig.MessageAutoConfig.class})
class TestControllerTest {

	private static final String FORMAT = "/test/%s";
	private static final List<ServiceReq.IpAddress> PARAM = ImmutableList.of(ServiceReq.IpAddress.builder().address("1.2.3.4").cidr("/32").build());

	@MockBean private RedisService redisService;
	@MockBean @SuppressWarnings("unused") private AwsStsHandler handler;
	@MockBean @SuppressWarnings("unused") private UserService<User> userService;

	@Autowired private MockMvc mvc;
	@Autowired private ObjectMapper mapper;

	@Test
	void checkRedisRequestMappingBadRequest() throws Exception {

		final ResultActions req = mvc.perform(createRequestBuilder());
		final MvcResult res = req.andExpect(checkStatusCodeIsOk()).andExpect(checkContentTypeIsJson()).andReturn();
		final BaseRes<ServiceRes.Res, Void> parsed = parseFromContent(res);

		checkResCode(parsed, BaseResCode.BAD_REQUEST);
	}

	@Test
	void checkRedisRequestMappingResponseOk() throws Exception {

		final ServiceRes.Res expected = ServiceRes.Res.builder().value(PARAM).build();
		Mockito.when(redisService.create(Mockito.any(ServiceReq.RedisPrm.class))).thenReturn(expected);

		final MockHttpServletRequestBuilder builder = createRequestParams(createRequestBuilder());
		final MvcResult res = mvc.perform(builder).andExpect(checkStatusCodeIsOk()).andExpect(checkContentTypeIsJson()).andReturn();
		final BaseRes<ServiceRes.Res, Void> parsed = parseFromContent(res);

		checkResCode(parsed, BaseResCode.OK);

		ArgumentCaptor<ServiceReq.RedisPrm> captor = ArgumentCaptor.forClass(ServiceReq.RedisPrm.class);
		Mockito.verify(redisService, Mockito.times(1)).create(captor.capture());
	}

	private MockHttpServletRequestBuilder createRequestBuilder() {
		return MockMvcRequestBuilders.post(String.format(FORMAT, "redis"));
	}

	private ResultMatcher checkStatusCodeIsOk() {
		return MockMvcResultMatchers.status().isOk();
	}

	private ResultMatcher checkContentTypeIsJson() {
		return MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);
	}

	private <T> BaseRes<ServiceRes.Res, T> parseFromContent(final MvcResult source) throws IOException {
		return mapper.readValue(source.getResponse().getContentAsString(), mapper.getTypeFactory().constructParametricType(BaseRes.class, ServiceRes.Res.class, Void.class));
	}

	private void checkResCode(final BaseRes<ServiceRes.Res, ?> res, final BaseResCode code) {
		assertThat(Optional.ofNullable(res.getResCode()).orElse(999)).isEqualTo(code.getValue());
	}

	private MockHttpServletRequestBuilder createRequestParams(final MockHttpServletRequestBuilder builder) throws JsonProcessingException {
		return builder.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(ServiceReq.RedisReq.builder().ipAddresses(PARAM).build()));
	}
}
