package bus.loona.web;

import bus.loona.domain.ServiceReq;
import bus.loona.domain.ServiceRes;
import bus.loona.domain.base.BaseRes;
import bus.loona.domain.vo.User;
import bus.loona.service.RedisService;
import bus.loona.service.UserService;
import bus.loona.view.TestHtmlView;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
	@ApiOperation(value="Get html view")
	public ModelAndView retrieveView(@ModelAttribute @Valid final ServiceReq.ViewReq req) {
		return new ModelAndView(view, ImmutableMap.of("code", req.getShortCode()));
	}

	@PostMapping("/test/redis")
	@ApiOperation(value="put & search value")
	public BaseRes<ServiceRes.Res, Void> createValue(@RequestBody @Valid final ServiceReq.RedisReq req) {
		return BaseRes.success(redisService.create(ServiceReq.from(req)));
	}

	@GetMapping(value="/user")
	@ApiOperation(value="search users")
	@ApiImplicitParam(name="Authorization", required=true, paramType="header", dataTypeClass=String.class)
	public BaseRes<List<User>, Void> searchUser() {
		return BaseRes.success(userService.search());
	}

	@PostMapping(value="/user")
	@ApiOperation(value="create & retrieve user")
	@ApiImplicitParam(name="Authorization", required=true, paramType="header", dataTypeClass=String.class)
	public BaseRes<User, Void> createUser(@RequestBody @Valid final ServiceReq.UserReq req) {
		return BaseRes.success(userService.create(ServiceReq.from(req)));
	}
}
