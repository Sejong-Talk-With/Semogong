package Talk_with.semogong.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Image {

    private String imgName;
    private String imgSrc;

    public Image(String imgName, String imgSrc) {
        this.imgName = imgName;
        this.imgSrc = imgSrc;
    }

    protected Image() { } // JPA 기본 스펙에 맞추기 위해 사용되지 않는 기본 생성자 설정. public으로 하면 외부에서 접근이 가능하므로 protected로 설정.
}
