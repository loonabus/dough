package bus.loona.web;

import bus.loona.domain.ServiceReq;
import bus.loona.domain.ServiceRes;
import bus.loona.domain.base.BaseRes;
import bus.loona.domain.vo.User;
import bus.loona.service.RedisService;
import bus.loona.service.UserService;
import bus.loona.view.TestHtmlView;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
public class TestController {

	private final TestHtmlView view;
	private final RedisService redisService;
	private final UserService<User> userService;

	@Autowired
	public TestController(final RedisService redisService, final UserService<User> userService) {

		this.redisService = redisService;
		this.userService = userService;
		this.view = new TestHtmlView();
	}

	@GetMapping("/test/view")
	@Operation(description="Get html view")
	public ModelAndView retrieveView(@ModelAttribute @Valid final ServiceReq.ViewReq req) {
		return new ModelAndView(view, ImmutableMap.of("code", req.getShortCode()));
	}

	@PostMapping("/test/redis")
	@Operation(description="put & search value")
	public BaseRes<ServiceRes.Res, Void> createValue(@RequestBody @Valid final ServiceReq.RedisReq req) {
		return BaseRes.success(redisService.create(ServiceReq.from(req)));
	}

	@GetMapping(value="/user")
	@Operation(description="search users")
	@Parameter(name="Authorization", required=true, in=ParameterIn.HEADER, style= ParameterStyle.SIMPLE)
	public BaseRes<List<User>, Void> searchUser() {
		return BaseRes.success(userService.search());
	}

	@PostMapping(value="/user")
	@Operation(description="create & retrieve user")
	@Parameter(name="Authorization", required=true, in=ParameterIn.HEADER, style= ParameterStyle.SIMPLE)
	public BaseRes<User, Void> createUser(@RequestBody @Valid final ServiceReq.UserReq req) {
		return BaseRes.success(userService.create(ServiceReq.from(req)));
	}
}
