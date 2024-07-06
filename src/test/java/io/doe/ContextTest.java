package io.doe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <loonabus@gmail.com>
 * @version 1.0.0
 * @see ContextTest
 * started on 2017-05-?? ~
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=ApplicationMain.class)
class ContextTest {

	private static final Logger log = LoggerFactory.getLogger(ContextTest.class);

	private final ApplicationContext c;
	private final WebApplicationContext w;

	@Autowired
	ContextTest(final ApplicationContext c, final WebApplicationContext w) {
		this.c = c; this.w = w;
	}

	@Test
	void springContextTest() {
		assertThat(c).isNotNull(); assertThat(w).isNotNull();
	}

	@AfterEach
	void stopEmbeddedRedisIfPresent() {

		try {
			((DisposableBean)c.getBean("embeddedRedisServerManager")).destroy();
		} catch (final BeansException e) {
			log.info("EmbeddedRedisBuilder bean not found");
		} catch (final Exception e) {
			log.warn("EmbeddedRedisBuilder stop failed", e);
		}
	}
}
