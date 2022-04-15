package Talk_with.semogong.domain.auth;

import Talk_with.semogong.domain.Image;
import Talk_with.semogong.domain.Member;
import Talk_with.semogong.domain.StudyState;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class MyUserDetail implements UserDetails {
    private String email;
    private String password;
    private String auth;

    private String name;
    private String nickname;
    private String desiredJob;
    private Image image;
    private StudyState state;


    public MyUserDetail(Member user) {
        this.email = user.getLoginId();
        this.password = user.getPassword();
        this.auth = "ROLE_" + user.getRole();
        this.name = user.getName();
        this.nickname = user.getNickname();
        this.desiredJob = user.getDesiredJob();
        this.image = user.getImage();
        this.state = user.getState();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.auth));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
