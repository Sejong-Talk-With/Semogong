package Talk_with.semogong.configuration;

import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpSessionEvent;

import javax.servlet.http.HttpSessionListener;



public class SessionListener implements HttpSessionListener {

    @Value("${server.servlet.session.timeout}")
    private int timeOut;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        se.getSession().setMaxInactiveInterval(timeOut); //세션만료10시간
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
    }

}
