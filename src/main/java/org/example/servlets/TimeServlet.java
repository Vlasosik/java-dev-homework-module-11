package org.example.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@WebServlet("/time")
public class TimeServlet extends HttpServlet {
    private static final String LAST_TIMEZONE = "lastTimezone";
    private static final String REAL_PATH_TO_FILE = "/WEB-INF/templates/";
    private static final String DATA_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String UTC = "UTC";
    private transient TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        super.init();
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(getServletContext().getRealPath(REAL_PATH_TO_FILE));
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=utf-8");
        try {
            String timezone = req.getParameter("timezone");
            timezone = lastCookies(req, timezone);
            ZoneId zoneId = (!timezone.isEmpty()) ? ZoneId.of(timezone) : ZoneId.of(UTC);
            String currentTime = OffsetDateTime.now(zoneId).format(DateTimeFormatter.ofPattern(DATA_TIME_FORMAT));
            Cookie cookie;
            cookie = new Cookie(LAST_TIMEZONE, timezone);
            cookie.setMaxAge(5);
            resp.addCookie(cookie);
            Context context = new Context();
            context.setVariable("currentTime", currentTime);
            context.setVariable("UTC", zoneId.getId());
            engine.process("time", context, resp.getWriter());
            resp.getWriter().close();
        } catch (IOException ex) {
            ex.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error!");
        }
    }

    @Override
    public void destroy() {
        engine = null;
        super.destroy();
    }

    private static String lastCookies(HttpServletRequest req, String timezone) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (LAST_TIMEZONE.equals(cookie.getName())) {
                    timezone = cookie.getValue();
                    break;
                }
            }
        }
        return (timezone != null && !timezone.isEmpty()) ? timezone : UTC;
    }
}

