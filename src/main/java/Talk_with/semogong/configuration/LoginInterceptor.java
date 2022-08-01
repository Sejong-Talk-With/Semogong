package Talk_with.semogong.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String focus = request.getParameter("focus");

        if (requestURI.equals("/")) {
            if (focus == null || !focus.equals("my-posts")) { // basic home -> / or parameters -> focus=all, today, all-members
                return true;
            }
        } // my-posts 만 밑으로 넘겨 옴

        HttpSession session = request.getSession();

        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER)  == null) {
            // 로그인 화면으로 redirect
            String location = "/members/login?redirectURL=" + requestURI;
            if (focus != null) location = location  +"&focus=" + focus;
            response.sendRedirect(location);
            return false; // 더 이상 진행되지 않음.
        }

        return true; // 다음 체인으로 진행
    }
}
