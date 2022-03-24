package org.summerclouds.common.restree.impl;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.summerclouds.common.core.log.MLog;

@org.springframework.web.bind.annotation.RestController
@ConditionalOnProperty(name="org.summerclouds.restree.enabled",havingValue="true")
public class RestController extends MLog {

	private RestServlet restServlet;
	
	@PostConstruct
	public void setup() {
		restServlet = new RestServlet();
		try {
			restServlet.init();
		} catch (ServletException e) {
			log().e(e);
		}
	}
	
    @RequestMapping("/rest/*")
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            restServlet.service(request, response);
        } catch (Exception e) {
            log().e("Server Error occurred", e);
            throw new ServletException(e);
        }
    }

}
